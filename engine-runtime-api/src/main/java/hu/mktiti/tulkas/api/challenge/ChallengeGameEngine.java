package hu.mktiti.tulkas.api.challenge;

import hu.mktiti.tulkas.api.GameEngine;
import hu.mktiti.tulkas.api.GameResult;
import hu.mktiti.tulkas.api.log.ConsoleEngineLogger;
import hu.mktiti.tulkas.api.log.EngineLoggerFactory;
import hu.mktiti.tulkas.api.log.GameEngineLogger;
import hu.mktiti.tulkas.api.log.LogTarget;

public abstract class ChallengeGameEngine<T> implements GameEngine<T> {

    protected final T bot;
    protected final GameEngineLogger logger;

    public ChallengeGameEngine(final T bot) {
        this(bot, EngineLoggerFactory.getLogger());
    }

    public ChallengeGameEngine(final T bot, final GameEngineLogger engineLogger) {
        this.bot = bot;
        logger = engineLogger == null ? new ConsoleEngineLogger() : engineLogger;
    }

    public abstract ChallengeResult playChallenge();

    @Override
    public final GameResult playGame() {
        return playChallenge();
    }

    protected final ChallengeResult crash() {
        return ChallengeResult.crash();
    }

    protected final ChallengeResult result(final long points) {
        return ChallengeResult.result(points);
    }

    protected final ChallengeResult result(final long points, final long maxPoints) {
        return ChallengeResult.result(points, maxPoints);
    }

    protected final void log(final String message) {
        logger.log(message);
    }

    protected final void logForBot(final String message) {
        logger.logFor(LogTarget.BOT_A, message);
    }

    protected final void logForAll(final String message) {
        logger.logFor(LogTarget.ALL, message);
    }
}