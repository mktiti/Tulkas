package hu.mktiti.tulkas.api.log;

public interface GameEngineLogger {

    default void log(final String message) {
        logFor(LogTarget.SELF, message);
    }

    void logFor(final LogTarget logTarget, final String message);

}