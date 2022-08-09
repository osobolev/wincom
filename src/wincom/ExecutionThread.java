package wincom;

import com.jacob.com.ComThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.*;

public final class ExecutionThread {

    private final ComLogger logger;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private Boolean initOk = null;
    private final Object lock = new Object();

    ExecutionThread(ComLogger logger) {
        this.logger = logger;
    }

    private boolean runInternal(Runnable action) {
        Future<?> future = service.submit(action);
        try {
            future.get();
            return true;
        } catch (ExecutionException ex) {
            logger.error(ex.getCause());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private <T> T runSomethingInThread(Callable<T> action) throws ComException {
        synchronized (lock) {
            if (initOk == null)
                throw new IllegalStateException("Thread is not initialized: call createExecutionThread");
            if (!initOk.booleanValue())
                throw new IllegalStateException("COM object is not initialized properly");
            Future<T> future = service.submit(action);
            try {
                return future.get();
            } catch (ExecutionException ex) {
                throw new ComException(ex.getCause());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ComException(ex);
            }
        }
    }

    public Object runMethodInThread(Method method, Object obj, Object[] args) throws ComException {
        Callable<Object> action = () -> method.invoke(obj, args);
        return runSomethingInThread(action);
    }

    public <T> T runConstructorInThread(Constructor<T> constructor, Object[] args) throws ComException {
        Callable<T> action = () -> constructor.newInstance(args);
        return runSomethingInThread(action);
    }

    public void createExecutionThread() {
        synchronized (lock) {
            if (initOk != null)
                throw new IllegalStateException("Thread is already created");
            initOk = runInternal(ComThread::InitMTA);
        }
    }

    public void release() {
        synchronized (lock) {
            if (service.isShutdown())
                throw new IllegalStateException("Thread is already released");
            if (initOk != null && initOk.booleanValue()) {
                runInternal(ComThread::Release);
            }
            service.shutdown();
            try {
                while (true) {
                    if (service.awaitTermination(1, TimeUnit.HOURS))
                        break;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
