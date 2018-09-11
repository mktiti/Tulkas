package hu.mktiti.cirkus.api;

import java.util.Optional;

import static hu.mktiti.cirkus.api.GameResult.*;

public abstract class DuelGameEngine<T extends BotInterface> implements GameEngine<T> {

    protected final T botA;
    protected final T botB;

    private boolean isCurrentlyBotA = true;
    private T currentBot;

    public DuelGameEngine(final T botA, final T botB) {
        this.botA = botA;
        this.botB = botB;
        currentBot = botA;
    }

    private T bot(final boolean isA) {
        return isA ? botA : botB;
    }

    protected abstract Optional<GameResult> playTurn();

    private final void nextTurn() {
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