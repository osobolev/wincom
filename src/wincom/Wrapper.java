package wincom;

import com.jacob.com.Dispatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Wrapper {

    protected final ExecutionThread thread;
    public final Dispatch instance;

    public Wrapper(ExecutionThread thread) {
        this.thread = thread;
        this.instance = null;
    }

    protected Wrapper(Wrapper wrapper, Dispatch instance) {
        this.thread = wrapper.thread;
        this.instance = instance;
    }

    public static Method getMethod(Class<?> cls, String name, Class<?>... parameterTypes) throws ComException {
        try {
            return cls.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new ComException(ex);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> cls, Class<?>... parameterTypes) throws ComException {
        try {
            return cls.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new ComException(ex);
        }
    }

    public final Object runMethodInThread(Method method, Object obj, Object[] args) throws ComException {
        return thread.runMethodInThread(method, obj, args);
    }

    public final Object runConstructorInThread(Constructor<?> constructor, Object[] args) throws ComException {
        return thread.runConstructorInThread(constructor, args);
    }

    public final void safeRelease() throws ComException {
        if (instance == null)
            return;
        Method method = getMethod(Dispatch.class, "safeRelease");
        thread.runMethodInThread(method, instance, new Object[0]);
    }

    public final String toString() {
        return instance == null ? null : "Dispatch{" + Long.toHexString(instance.m_pDispatch) + "}";
    }
}
