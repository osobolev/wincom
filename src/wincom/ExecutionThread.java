package wincom;

import com.jacob.com.ComThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ExecutionThread implements Runnable {

    private final ComLogger logger;

    private final Object initLock = new Object();
    private final Object queryWaitLock = new Object();
    private final Object replyWaitLock = new Object();

    private final Thread thread = new Thread(this);

    private Method method;
    private Constructor<?> constructor;
    private Object obj;
    private Object[] args;
    private Object result;
    private Throwable error;

    private boolean inited = false;
    private boolean replyReady = false;
    private boolean running = false;

    ExecutionThread(ComLogger logger) {
        this.logger = logger;
    }

    private Object runSomethingInThread(Object[] args) throws ComException {
        this.args = args;
        this.result = null;
        this.error = null;
        synchronized (replyWaitLock) {
            replyReady = false;
            synchronized (queryWaitLock) {
                if (!running)
                    throw new IllegalStateException("COM object is not initialized properly");
                queryWaitLock.notifyAll();
            }
            while (true) {
                try {
                    replyWaitLock.wait();
                } catch (InterruptedException ex) {
                    // ignore
                }
                if (replyReady)
                    break;
            }
        }
        if (error != null) {
            throw new ComException(error);
        }
        return result;
    }

    public synchronized Object runMethodInThread(Method method, Object obj, Object[] args) throws ComException {
        this.method = method;
        this.constructor = null;
        this.obj = obj;
        return runSomethingInThread(args);
    }

    public synchronized <T> T runConstructorInThread(Constructor<T> constructor, Object[] args) throws ComException {
        this.method = null;
        this.constructor = constructor;
        this.obj = null;
        Class<T> cls = constructor.getDeclaringClass();
        return cls.cast(runSomethingInThread(args));
    }

    public synchronized void createExecutionThread() {
        synchronized (initLock) {
            thread.start();
            while (true) {
                try {
                    initLock.wait();
                } catch (InterruptedException ex) {
                    //
                }
                if (inited)
                    break;
            }
        }
    }

    private void startFailed(Throwable th) {
        logger.error(th);
        synchronized (queryWaitLock) {
            initFinished();
        }
    }

    private void initFinished() {
        synchronized (initLock) {
            inited = true;
            initLock.notifyAll();
        }
    }

    public void run() {
        try {
            ComThread.InitMTA();
        } catch (Error | RuntimeException ex) {
            startFailed(ex);
            throw ex;
        }
        synchronized (queryWaitLock) {
            running = true;
            initFinished();
            while (true) {
                try {
                    queryWaitLock.wait();
                } catch (InterruptedException ex) {
                    //
                }
                if (!running)
                    break;
                try {
                    if (method != null) {
                        result = method.invoke(obj, args);
                    } else if (constructor != null) {
                        result = constructor.newInstance(args);
                    } else {
                        // todo: add Dispatch.invoke/call
                        result = null;
                    }
                } catch (InvocationTargetException itex) {
                    error = itex.getTargetException();
                } catch (Throwable ex) {
                    error = ex;
                } finally {
                    synchronized (replyWaitLock) {
                        replyReady = true;
                        replyWaitLock.notifyAll();
                    }
                }
            }
        }
        ComThread.Release();
    }

    public synchronized void release() {
        synchronized (queryWaitLock) {
            running = false;
            queryWaitLock.notifyAll();
        }
        try {
            thread.join();
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
