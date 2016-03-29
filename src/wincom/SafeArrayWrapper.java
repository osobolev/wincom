package wincom;

import com.jacob.com.SafeArray;

import java.lang.reflect.Method;

public final class SafeArrayWrapper {

    private final ExecutionThread thread;
    public final SafeArray instance;

    public SafeArrayWrapper(ExecutionThread thread, SafeArray instance) {
        this.thread = thread;
        this.instance = instance;
    }

    public SafeArrayWrapper(Wrapper wrapper, SafeArray instance) {
        this.thread = wrapper.thread;
        this.instance = instance;
    }

    private Object runMethodInThread(Method method, Object obj, Object[] args) throws ComException {
        return thread.runMethodInThread(method, obj, args);
    }

    public void fromStringArray(String[] ja) throws ComException {
        runMethodInThread(Wrapper.getMethod(SafeArray.class, "fromStringArray", String[].class), instance, new Object[] {ja});
    }
}
