package wincom;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

import java.lang.reflect.Constructor;

public abstract class ComContainer<WT extends Wrapper> {

    private final ComLogger logger;

    private ExecutionThread thread = null;
    private final Object threadLock = new Object();
    private WT rootObject = null;
    private final Object rootLock = new Object();

    protected ComContainer(ComLogger logger) {
        this.logger = logger;
    }

    private ExecutionThread getExecutionThread() {
        synchronized (threadLock) {
            if (thread != null)
                return thread;
            thread = new ExecutionThread(logger);
            thread.createExecutionThread();
            return thread;
        }
    }

    private void destroy() {
        try {
            synchronized (threadLock) {
                if (thread != null) {
                    thread.release();
                    thread = null;
                }
            }
        } catch (Error | RuntimeException ex) {
            // ignore
        }
    }

    protected abstract WT createComObject(ExecutionThread thread) throws ComException;

    public final WT getRootObject(boolean require) throws ComException {
        synchronized (rootLock) {
            if (rootObject != null) {
                // If rootObject already exists, test it; if test is OK, then return tested instance
                try {
                    testComObject(rootObject);
                    return rootObject;
                } catch (ComException cex) {
                    // ignore
                }
            }
            if (rootObject != null) {
                // rootObject exists, but test failed - destroy bad rootObject (with its associated thread)
                rootObject = null;
                destroy();
            }
            if (require) {
                // Create new rootObject (here rootObject is always null)
                ExecutionThread thread = getExecutionThread();
                rootObject = createComObject(thread);
            }
            return rootObject;
        }
    }

    protected abstract void testComObject(WT comObject) throws ComException;

    public final void release() {
        synchronized (rootLock) {
            try {
                WT rootObject = getRootObject(false);
                if (rootObject != null) {
                    quitComObject(rootObject);
                }
            } catch (ComException ex) {
                logger.error(ex);
            }
            this.rootObject = null;
            destroy();
        }
    }

    protected abstract void quitComObject(WT comObject) throws ComException;

    private VariantWrapper runConstructorInThread(Constructor<?> constructor, Object[] args) throws ComException {
        ExecutionThread thread = getExecutionThread();
        Variant variant = (Variant) thread.runConstructorInThread(constructor, args);
        return new VariantWrapper(thread, variant);
    }

    public final VariantWrapper variant() throws ComException {
        return runConstructorInThread(Wrapper.getConstructor(Variant.class), new Object[0]);
    }

    public final VariantWrapper variant(int i) throws ComException {
        return runConstructorInThread(Wrapper.getConstructor(Variant.class, Integer.TYPE), new Object[] {i});
    }

    public final VariantWrapper variant(boolean b) throws ComException {
        return runConstructorInThread(Wrapper.getConstructor(Variant.class, Boolean.TYPE), new Object[] {b});
    }

    public final VariantWrapper variant(Object o) throws ComException {
        return runConstructorInThread(Wrapper.getConstructor(Variant.class, Object.class), new Object[] {o});
    }

    public final VariantWrapper none() throws ComException {
        VariantWrapper none = variant();
        none.putNoParam();
        return none;
    }

    public final SafeArrayWrapper array(int len) throws ComException {
        ExecutionThread thread = getExecutionThread();
        Constructor<SafeArray> constructor = Wrapper.getConstructor(SafeArray.class, Integer.TYPE, Integer.TYPE);
        SafeArray array = thread.runConstructorInThread(constructor, new Object[] {Variant.VariantString, len});
        return new SafeArrayWrapper(thread, array);
    }

    public final VariantWrapper array(String[] ja) throws ComException {
        SafeArrayWrapper array = array(ja.length);
        array.fromStringArray(ja);
        return variant(array.instance);
    }

    public final Object newWrapperFromVariantDispatch(Class<?> cls, Variant variant) throws ComException {
        ExecutionThread thread = getExecutionThread();
        Dispatch dispatch = (Dispatch) thread.runMethodInThread(Wrapper.getMethod(Variant.class, "getDispatch"), variant, new Object[0]);
        Constructor<?> constructor = Wrapper.getConstructor(cls, Dispatch.class);
        return thread.runConstructorInThread(constructor, new Object[] {dispatch});
    }
}
