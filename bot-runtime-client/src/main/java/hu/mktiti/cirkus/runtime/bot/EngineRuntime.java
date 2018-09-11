package hu.mktiti.cirkus.runtime.bot;

import hu.mktiti.cirkus.api.BotInterface;
import hu.mktiti.cirkus.api.GameEngine;
import hu.mktiti.cirkus.api.GameResult;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public final class EngineRuntime {

    private Optional<Constructor<?>> findBot(final Reflections reflections) {
        final Set<Class<? extends BotInterface>> botImpls = reflections.getSubTypesOf(BotInterface.class);
        return botImpls.stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()) && Modifier.isPublic(c.getModifiers()))
                .flatMap(c -> Arrays.stream(c.getConstructors()))
                .filter(c -> c.getParameterCount() == 0)
                .findAny();
    }

    private Optional<Constructor<?>> findEngine(final Class<? extends BotInterface> botClass, final Reflections reflections) {
        final Set<Class<? extends GameEngine>> engineImpls = reflections.getSubTypesOf(GameEngine.class);
        return engineImpls.stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()) && Modifier.isPublic(c.getModifiers()))
                .flatMap(c -> Arrays.stream(c.getConstructors()))
                .filter(c -> c.getParameterCount() == 2 &&
                            Arrays.stream(c.getParameterTypes()).allMatch(p -> p.isAssignableFrom(botClass)))
                .findAny();
    }

    private Optional<GameEngine<?>> findAndCreateEngine() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Configuration reflectionsConfig = new ConfigurationBuilder().setScanners(new SubTypesScanner()).setUrls(ClasspathHelper.forJavaClassPath());
        final Reflections reflections = new Reflections(reflectionsConfig);

        final Optional<Constructor<?>> botConstructor = findBot(reflections);
        if (!botConstructor.isPresent()) {
            System.out.println("No bot found");
            return Optional.empty();
        }
        final BotInterface botA = (BotInterface)botConstructor.get().newInstance();
        final BotInterface botB = (BotInterface)botConstructor.get().newInstance();

        final Optional<Constructor<?>> engineConstructor = findEngine(botA.getClass(), reflections);
        if (!engineConstructor.isPresent()) {
            System.out.println("No engine found");
            return Optional.empty();
        }
        return Optional.of((GameEngine<?>)engineConstructor.get().newInstance(botA, botB));
    }

    public void play() {
        try {
            final Optional<GameEngine<?>> engineOpt = findAndCreateEngine();
            if (!engineOpt.isPresent()) {
                return;
            }

            final GameEngine<?> engine = engineOpt.get();
            System.out.println("Starting game");
            final GameResult result = engine.playGame();
            System.out.println("Game done");
            System.out.println("Did A win? " + result.doAWins());
            System.out.println("Did B win? " + result.doBWins());

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new EngineRuntime().play();
    }

}