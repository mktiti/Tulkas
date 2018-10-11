package hu.mktiti.tulkas.api.match;

import hu.mktiti.tulkas.api.GameResult;

import java.io.Serializable;
import java.util.Objects;

public final class MatchResult implements GameResult {

    public enum BotActor { BOT_A, BOT_B }

    public enum ResultType implements Serializable {
        BOT_A_WIN, DRAW, BOT_B_WIN, BOT_A_ERROR, BOT_B_ERROR
    }

    public final ResultType type;

    public MatchResult(final ResultType type) {
        this.type = type;
    }

    public static MatchResult win(final BotActor bot) {
        Objects.requireNonNull(bot);

        final ResultType type = (bot == BotActor.BOT_A) ? ResultType.BOT_A_WIN : ResultType.BOT_B_WIN;
        return new MatchResult(type);
    }

    public static MatchResult error(final BotActor bot) {
        Objects.requireNonNull(bot);

        final ResultType type = (bot == BotActor.BOT_A) ? ResultType.BOT_A_ERROR : ResultType.BOT_B_ERROR;
        return new MatchResult(type);
    }

    public static MatchResult draw() {
        return new MatchResult(ResultType.DRAW);
    }

    private BotActor opponent(final BotActor bot) {
        Objects.requireNonNull(bot);

        return bot == BotActor.BOT_A ? BotActor.BOT_B : BotActor.BOT_A;
    }

    public boolean botWon(final BotActor bot) {
        Objects.requireNonNull(bot);

        return (bot == BotActor.BOT_A) ? botAWon() : botBWon();
    }

    public boolean botAWon() {
        return type == ResultType.BOT_A_WIN || type == ResultType.BOT_B_ERROR;
    }

    public boolean botBWon() {
        return type == ResultType.BOT_B_WIN || type == ResultType.BOT_A_ERROR;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}