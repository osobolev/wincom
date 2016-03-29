package wincom;

import com.jacob.com.ComFailException;

public final class ComException extends Exception {

    private final boolean comFail;

    public ComException(Throwable cause) {
        super(cause);
        comFail = cause instanceof ComFailException;
    }

    public boolean isComFail() {
        return comFail;
    }
}
