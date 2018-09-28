package hu.mktiti.cirkus.api;

public final class BotLoggerFactory {

    private static GameBotLogger logger = new ConsoleBotLogger();

    public static void setDefaultLogger(final GameBotLogger logger) {
        if (logger != null) {
            BotLoggerFactory.logger = logger;
        }
    }

    public static GameBotLogger getLogger() {
        return logger;
    }

}