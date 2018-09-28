package hu.mktiti.cirkus.api;

public final class ConsoleEngineLogger implements GameEngineLogger {

    @Override
    public void log(final String message) {
        logFor(LogTarget.SELF, message);
    }

    @Override
    public void logFor(final LogTarget logTarget, final String message) {
        System.out.println(logPrefix(logTarget) + message);
    }

    private static String logPrefix(final LogTarget target) {
        switch (target) {
            case ALL:   return "[For everyone] ";
            case BOTS:  return "[For bots] ";
            case BOT_A: return "[For Bot A] ";
            case BOT_B: return "[For Bot B] ";
            default:    return "";
        }
    }
}