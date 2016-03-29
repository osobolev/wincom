package wincom;

public abstract class ComLogger {

    public abstract void error(Throwable error);

    public static class Simple extends ComLogger {

        public void error(Throwable error) {
            error.printStackTrace();
        }
    }
}
