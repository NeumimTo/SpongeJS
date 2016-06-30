package cz.neumimto.spongejs;

import com.google.inject.Inject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.script.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * Created by NeumimTo on 30.6.16.
 */
@Plugin(version = "1.0", authors = {"NeumimTo"}, id = "cz.neumimto.spongejs")
public class ScriptLoader {

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    private Object listener;

    private List<Path> files = new ArrayList<>();
    private Path main;
    private ScriptEngine scriptEngine;
    private Game game;

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        this.game = Sponge.getGame();
        scriptEngine = new NashornScriptEngineFactory().getScriptEngine("--optimistic-types=true");
        try {
            reload();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bindings bindings = new SimpleBindings();
        bindings.put("plugin", this);
        bindings.put("Consumer", Consumer.class);
        bindings.put("Sponge", Sponge.class);
        bindings.put("game", game);
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }

    private void reload() throws IOException {
        if (listener != null) {
            Sponge.getGame().getEventManager().unregisterListeners(listener);
        }

        Files.walkFileTree(defaultConfig, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isDirectory() && attrs.size() > 0) {
                    if (file.endsWith(".js")) {
                        String nameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(file.toString());
                        if (nameWithoutExtension.equalsIgnoreCase("main")) {
                            main = file;
                        }
                    }

                }
                return FileVisitResult.CONTINUE;
            }
        });

        if (main == null) {
            saveResource(defaultConfig.toFile(), "Main.js", true);
        }

        try (FileReader reader = new FileReader(main.toFile())){
            scriptEngine.eval(new FileReader(main.toFile()));
        } catch (IOException | ScriptException ex) {
            ex.printStackTrace();
        }

        if (listener == null) {
            throw new ListenerNotCreatedException();
        }
        Sponge.getGame().getEventManager().registerListeners(this, listener);


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

    private void parseIdeaOutput(File file) {
        int i = 4;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentClass;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] split = line.split(" ");
                if (split.length > 2) {

                } else {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
