package hu.mktiti.tulkas.api;

public final class ConsoleBotLogger implements GameBotLogger {

    @Override
    public void log(final String message) {
        System.out.println(message);
    }

}