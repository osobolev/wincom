package wincom;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

import java.lang.reflect.Constructor;

public abstract class ComContainer<WT extends Wrapper> {

    private final ComLogger logger;

    private ExecutionThread thread = null;
    private WT rootObject = null;

    protected ComContainer(ComLogger logger) {
        this.logger = logger;
    }

    private synchronized ExecutionThread getExecutionThread() {
        if (thread != null)
            return thread;
        thread = new ExecutionThread(logger);
        thread.createExecutionThread();
        return thread;
    }

    private synchronized void destroy() {
        if (thread != null) {
            thread.release();
            thread = null;
        }
    }

    private void initRootObject() throws ComException {
        ExecutionThread thread = getExecutionThread();
        rootObject = createComObject(thread);
    }

    protected abstract WT createComObject(ExecutionThread thread) throws ComException;

    public final synchronized WT getRootObject(boolean require) throws ComException {
        if (rootObject == null) {
            if (require) {
                initRootObject();
            }
        } else {
            try {
                testComObject(rootObject);
            } catch (ComException cex) {
                try {
                    destroy();
                } catch (Exception ex) {
                    // ignore
                }
                rootObject = null;
                if (require) {
                    initRootObject();
                }
            }
        }
        return rootObject;
    }

    protected abstract void testComObject(WT comObject) throws ComException;

    public final synchronized void release() {
        try {
            WT rootObject = getRootObject(false);
            if (rootObject != null) {
                quitComObject(rootObject);
                destroy();
            }
        } catch (ComException ex) {
            logger.error(ex);
        }
        this.rootObject = null;
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
