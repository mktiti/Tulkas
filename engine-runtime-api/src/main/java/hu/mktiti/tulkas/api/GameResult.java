package hu.mktiti.tulkas.api;

import java.io.Serializable;

public final class GameResult implements Serializable {

    public enum ResultType implements Serializable { CRASH, ERROR, WIN, DRAW }
    public enum Actor implements Serializable { BOT_A, BOT_B, ENGINE }

    public final ResultType type;
    public final Actor actor;

    public GameResult(final ResultType type, final Actor actor) {
        this.type = type;
        this.actor = actor;
    }

    public static GameResult crash(final Actor actor) {
        return new GameResult(ResultType.CRASH, actor);
    }

    public static GameResult error(final Actor bot) {
        if (bot != Actor.BOT_A && bot != Actor.BOT_B) {
            throw new IllegalArgumentException("Only Bot A or Bot B can make bad moves (error)");
        }

        return new GameResult(ResultType.ERROR, bot);
    }

    public static GameResult win(final Actor bot) {
        if (bot != Actor.BOT_A && bot != Actor.BOT_B) {
            throw new IllegalArgumentException("Winner can only be Bot A or Bot B");
        }

        return new GameResult(ResultType.WIN, bot);
    }

    public static GameResult draw() {
        return new GameResult(ResultType.DRAW, null);
    }

    private Actor opponent(final Actor actor) {
        return actor == Actor.BOT_A ? Actor.BOT_B : Actor.BOT_A;
    }

    public boolean doBotWins(final Actor bot) {
        return (type == ResultType.WIN && actor == bot) ||
                (actor == opponent(bot) && (type == ResultType.CRASH || type == ResultType.ERROR));
    }

    public boolean doAWins() {
        return doBotWins(Actor.BOT_A);
    }

    public boolean doBWins() {
        return doBotWins(Actor.BOT_B);
    }
}