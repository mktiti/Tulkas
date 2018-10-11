package hu.mktiti.tulkas.api.match;

import hu.mktiti.tulkas.api.GameEngine;
import hu.mktiti.tulkas.api.GameResult;
import hu.mktiti.tulkas.api.exception.BotTimeoutException;
import hu.mktiti.tulkas.api.exception.GameEngineException;
import hu.mktiti.tulkas.api.log.EngineLoggerFactory;
import hu.mktiti.tulkas.api.log.GameEngineLogger;
import hu.mktiti.tulkas.api.log.LogTarget;

import static hu.mktiti.tulkas.api.match.MatchResult.*;

public abstract class DuelGameEngine<T> implements GameEngine<T> {

    public enum TurnResult {
        WIN, LOSE, DRAW, ERROR, CONTINUE
    }

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

    protected abstract TurnResult playTurn();

    private void nextTurn() {
        isCurrentlyBotA = !isCurrentlyBotA;
        currentBot = bot(isCurrentlyBotA);
    }

    public final GameResult playGame() {
        try {
            while (true) {
                final TurnResult turnResult;
                try {
                    turnResult = playTurn();
                } catch (final BotTimeoutException bte) {
                    return error();
                }

                if (turnResult != null) {
                    switch (turnResult) {
                        case WIN:   return wins();
                        case DRAW:  return draws();
                        case LOSE:  return loses();
                        case ERROR: return error();
                        default: break;
                    }
                }

                nextTurn();
            }
        } catch (final Exception e) {
            throw new GameEngineException("Exception while playing game: " + e.getMessage());
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

    protected final MatchResult wins() {
        return win(currentBotActor());
    }

    protected final MatchResult loses() {
        return win(botToActor(!isCurrentlyBotA()));
    }

    protected final MatchResult draws() {
        return draw();
    }

    protected final MatchResult error() {
        return MatchResult.error(botToActor(!isCurrentlyBotA()));
    }

    protected BotActor currentBotActor() {
        return botToActor(isCurrentlyBotA());
    }

    protected BotActor botToActor(final boolean isBotA) {
        return isBotA ? BotActor.BOT_A : BotActor.BOT_B;
    }

    protected boolean isCurrentlyBotA() {
        return isCurrentlyBotA;
    }

    protected T getCurrentBot() {
        return currentBot;
    }
}