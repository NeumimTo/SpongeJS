//([a-zA-Z0-9]{3,})|(\.)
var imports = new JavaImporter(java.util, java.nio.file);
/*java */
var HashSet = Java.type('java.util.HashSet');
var HashMap = Java.type('java.util.HashMap');
var File = Java.type("java.io.File");
/* sponge */
var Texts = Java.type("org.spongepowered.api.text.Text");
var Keys = Java.type("org.spongepowered.api.data.key.Keys");
/* libs */

/* https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions */

function registerEventListener(clazz,consumer) {
    plugin.registerEventListener(clazz,consumer);
}
/* Define functions here */

/*  */
with (imports) {
    var stream = Files.newDirectoryStream(new File("./mods/SpongeJS").toPath(), "*.js");
    stream.forEach(function (p) {
        var name = p.toFile().absolutePath;
        if (!name.endsWith("Main.js")) {
            load(name);
        }
    });
}
/* Should be in another file with .js extension*/
registerEventListener(DamageEntityEvent.static, new (Java.extend(Consumer.static, {
    accept: function (event) {
        System.out.println("Im Javascript event listener");
    }
})));