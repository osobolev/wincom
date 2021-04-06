package wincom;

public interface ComLogger {

    void error(Throwable error);

    class Simple implements ComLogger {

        public void error(Throwable error) {
            error.printStackTrace();
        }
    }
}
