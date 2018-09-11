package hu.mktiti.cirkus.api;

public interface GameEngine<T extends BotInterface> {

    GameResult playGame();

}