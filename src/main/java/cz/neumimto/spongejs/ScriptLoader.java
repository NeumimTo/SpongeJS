package cz.neumimto.spongejs;

import com.google.inject.Inject;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.script.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static cz.neumimto.spongejs.ClassGenerator.it;


/**
 * Created by NeumimTo on 30.6.16.
 */
@Plugin(version = "1.0", authors = {"NeumimTo"}, id = "cz.neumimto.spongejs", name = "spongejs")
public class ScriptLoader {

    static {
        try {
            Launch.classLoader.addURL(new File("./mods/nashorn.jar").toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Map<StaticClass, List<Consumer<? extends Event>>> eventHandlers = new HashMap<>();
    protected Path defaultConfig;
    @Inject
    private Logger logger;
    private Object listener;
    private Path main;
    private ScriptEngine scriptEngine;
    private Game game;
    private Map<String, Class> events = new HashMap<>();

    public ScriptLoader() {
        File file = new File("./mods/SpongeJs");
        file.mkdirs();
        defaultConfig = file.toPath();
    }

    public void processCSV(String line) {
        String[] split = line.split(" ");
        String s = "";
        String n = "";
        if (split.length == 3) {
            s = split[2] + "." + split[1] + "$" + split[0];
            n = split[1] + split[0];
        } else if (split.length == 2) {
            s = split[1] + "." + split[0];
            n = split[0];
        } else {
            System.err.println(line);
        }
        try {
            Class<?> aClass = Class.forName(s);
            events.put(n, aClass);
        } catch (ClassNotFoundException e) {
            System.err.println(line);
            System.err.println("Class not found");
        }

    }

    public void registerEventListener(StaticClass cl, Consumer<? extends Event> a) {
        List<Consumer<? extends Event>> consumers = eventHandlers.get(cl);
        if (consumers == null) {
            consumers = new ArrayList<>();
            eventHandlers.put(cl, consumers);
        }
        consumers.add(a);
    }

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        this.game = Sponge.getGame();

        File file = new File(defaultConfig.toFile(), "events.csv");
        if (!file.exists()) {
            try {
                saveResource(defaultConfig.toFile(), "/events.csv", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {
            lines.forEachOrdered(this::processCSV);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scriptEngine = new NashornScriptEngineFactory().getScriptEngine("--optimistic-types=true");


        Bindings bindings = new SimpleBindings();
        bindings.put("plugin", this);
        bindings.put("Consumer", Consumer.class);
        bindings.put("Sponge", Sponge.class);
        bindings.put("game", game);
        bindings.put("console", logger);
        bindings.put("logger", logger);
        for (Map.Entry<String, Class> a : events.entrySet()) {
            bindings.put(a.getKey(), a.getValue());
        }
        scriptEngine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        try {
            reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() throws IOException {
        it++;
        System.out.println("Reloading ... " + "Iteration: " + it);
        if (listener != null) {
            Sponge.getGame().getEventManager().unregisterListeners(listener);
            System.out.println("Unregistered dynamic listener");
        }

        main = new File(defaultConfig.toFile(), "Main.js").toPath();
        if (!Files.exists(main, LinkOption.NOFOLLOW_LINKS)) {
            saveResource(defaultConfig.toFile(), "/Main.js", true);
        }

        try (FileReader reader = new FileReader(main.toFile())) {
            System.out.println("Evaluating Main.js");
            scriptEngine.eval(reader);
        } catch (IOException | ScriptException ex) {
            ex.printStackTrace();
        }
        System.out.println("Creating Listener class");
        listener = ClassGenerator.generateDynamicListener(eventHandlers);

        if (listener == null) {
            throw new ListenerNotCreatedException();
        }
        Sponge.getGame().getEventManager().registerListeners(this, listener);

        eventHandlers.clear();
    }

    public void log(String s) {
        System.out.println(s);
    }

    public void warn(String s) {
        logger.log(Level.WARNING, s);
    }

    public void fatal(String s) {
        logger.log(Level.SEVERE, s);
    }

    //too lazy to think - http://stackoverflow.com/questions/10308221/how-to-copy-file-inside-jar-to-outside-the-jar
    public File saveResource(File outputDirectory, String name, boolean replace)
            throws IOException {
        File out = new File(outputDirectory, name);
        if (!replace && out.exists())
            return out;
        InputStream resource = this.getClass().getResourceAsStream(name);
        if (resource == null)
            throw new FileNotFoundException(name + " (resource not found)");
        try (InputStream in = resource;
             OutputStream writer = new BufferedOutputStream(
                     new FileOutputStream(out))) {
            byte[] buffer = new byte[1024 * 4];
            int length;
            while ((length = in.read(buffer)) >= 0) {
                writer.write(buffer, 0, length);
            }
        }
        return out;
    }

}
