package hu.mktiti.tulkas.api.log;

public final class EngineLoggerFactory {

    private static GameEngineLogger logger = new ConsoleEngineLogger();

    public static void setDefaultLogger(final GameEngineLogger logger) {
        if (logger != null) {
            EngineLoggerFactory.logger = logger;
        }
    }

    public static GameEngineLogger getLogger() {
        return logger;
    }

}