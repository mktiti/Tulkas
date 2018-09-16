package hu.mktiti.cirkus.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public final class GameResult implements Serializable {

    enum ResultType implements Serializable { CRASH, ERROR, WIN, DRAW }
    enum Actor implements Serializable { BOT_A, BOT_B, ENGINE }

    private final ResultType type;
    private final Actor actor;

    @JsonCreator
    private GameResult(@JsonProperty("type") final ResultType type, @JsonProperty("actor") final Actor actor) {
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

    public ResultType getType() {
        return type;
    }

    public Actor getActor() {
        return actor;
    }
}