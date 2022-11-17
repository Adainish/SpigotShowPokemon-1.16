package io.github.adainish.spigotshowpokemon.config;

import io.github.adainish.configuratelib.shade.spongepowered.configurate.serialize.SerializationException;
import io.github.adainish.spigotshowpokemon.SpigotShowPokemon;

public class Config extends Configurable{
    private static Config config;
    public static Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    public void setup() {
        super.setup();
    }

    public void load() {
        super.load();
    }

    public void save() {super.save();}
    @Override
    public void populate() {
        try {
            this.get().node("Messages", "Display").set("&b(&e!&b) &e%p% &7is trying to show you their &b%pokemon%&7.");
        } catch (SerializationException e) {
            SpigotShowPokemon.plugin.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public String getConfigName() {
        return "config.hocon";
    }
}

