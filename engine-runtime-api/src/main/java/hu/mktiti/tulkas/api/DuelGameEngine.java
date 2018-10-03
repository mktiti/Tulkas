package hu.mktiti.tulkas.api;

import java.util.Optional;

import static hu.mktiti.tulkas.api.GameResult.*;

public abstract class DuelGameEngine<T> implements GameEngine<T> {

    protected final T botA;
    protected final T botB;
    protected final GameEngineLogger logger;

    private boolean isCurrentlyBotA = true;
    private T currentBot;

    public DuelGameEngine(final T botA, final T botB) {
        this(botA, botB, EngineLoggerFactory.getLogger());
    }

    public DuelGameEngine(final T botA, final T botB, final GameEngineLogger engineLogger) {
        this.botA = botA;
        this.botB = botB;
        logger = engineLogger;
        currentBot = botA;
    }

    private T bot(final boolean isA) {
        return isA ? botA : botB;
    }

    protected abstract Optional<GameResult> playTurn();

    private void nextTurn() {
        isCurrentlyBotA = !isCurrentlyBotA;
        currentBot = bot(isCurrentlyBotA);
    }

    public final GameResult playGame() {
        try {
            while (true) {
                final Optional<GameResult> turnResult = playTurn();
                if (turnResult.isPresent()) {
                    return turnResult.get();
                }

                nextTurn();
            }
        } catch (final Exception e) {
            return crash(Actor.ENGINE);
        }
    }

    protected final void log(final String message) {
        if (logger != null) {
            logger.log(message);
        }
    }

    protected final void logForCurrent(final String message) {
        if (logger != null) {
            logger.logFor((isCurrentlyBotA() ? LogTarget.BOT_A : LogTarget.BOT_B), message);
        }
    }

    protected final void logForBots(final String message) {
        if (logger != null) {
            logger.logFor(LogTarget.BOTS, message);
        }
    }

    protected final void logForAll(final String message) {
        if (logger != null) {
            logger.logFor(LogTarget.ALL, message);
        }
    }

    protected final GameResult wins() {
        return win(currentBotActor());
    }

    protected final GameResult loses() {
        return win(botToActor(!isCurrentlyBotA()));
    }

    protected final GameResult error() {
        return GameResult.error(botToActor(!isCurrentlyBotA()));
    }

    protected Actor currentBotActor() {
        return botToActor(isCurrentlyBotA());
    }

    protected Actor botToActor(final boolean isBotA) {
        return isBotA ? Actor.BOT_A : Actor.BOT_B;
    }

    protected boolean isCurrentlyBotA() {
        return isCurrentlyBotA;
    }

    protected T getCurrentBot() {
        return currentBot;
    }
}