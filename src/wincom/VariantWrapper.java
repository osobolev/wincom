package wincom;

import com.jacob.com.Variant;

import java.lang.reflect.Method;

public final class VariantWrapper {

    private final ExecutionThread thread;
    public final Variant instance;

    public VariantWrapper(ExecutionThread thread, Variant instance) {
        this.thread = thread;
        this.instance = instance;
    }

    public VariantWrapper(Wrapper wrapper, Variant instance) {
        this.thread = wrapper.thread;
        this.instance = instance;
    }

    private Object runMethodInThread(Method method, Object obj, Object[] args) throws ComException {
        return thread.runMethodInThread(method, obj, args);
    }

    public void putShortRef(short s) throws ComException {
        runMethodInThread(Wrapper.getMethod(Variant.class, "putShortRef", Short.TYPE), instance, new Object[] {s});
    }

    public short getShortRef() throws ComException {
        Short s = (Short) runMethodInThread(Wrapper.getMethod(Variant.class, "getShortRef"), instance, new Object[0]);
        return s.shortValue();
    }

    public void putStringRef(String s) throws ComException {
        runMethodInThread(Wrapper.getMethod(Variant.class, "putStringRef", String.class), instance, new Object[] {s});
    }

    public String getStringRef() throws ComException {
        return (String) runMethodInThread(Wrapper.getMethod(Variant.class, "getStringRef"), instance, new Object[0]);
    }

    public String getString() throws ComException {
        return (String) runMethodInThread(Wrapper.getMethod(Variant.class, "getString"), instance, new Object[0]);
    }

    public void putNoParam() throws ComException {
        runMethodInThread(Wrapper.getMethod(Variant.class, "putNoParam"), instance, new Object[0]);
    }

    public String toString() {
        try {
            return (String) runMethodInThread(Wrapper.getMethod(Variant.class, "toString"), instance, new Object[0]);
        } catch (ComException th) {
            return "Error: " + th.getMessage();
        }
    }
}
