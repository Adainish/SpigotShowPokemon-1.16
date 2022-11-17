package io.github.adainish.spigotshowpokemon;

import io.github.adainish.spigotshowpokemon.config.Config;
import io.github.adainish.spigotshowpokemon.events.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class SpigotShowPokemon extends JavaPlugin {

    public static Plugin plugin = null;

    public static final Logger log = LogManager.getLogManager().getLogger("SpigotShowPokemon");
    public ChatListener chatListener;

    public String author = "Winglet";
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        plugin.getDataFolder().mkdirs();
        Config.getConfig().setup();
        Config.getConfig().load();
        chatListener = new ChatListener();
        Bukkit.getServer().getPluginManager().registerEvents(chatListener, this);
        log.log(Level.WARNING, "Loaded up SpigotShowPokemon by" + author);
    }

    @Override
    public void onDisable() {
        plugin = null;
        log.log(Level.WARNING, "Disabled SpigotShowPokemon");
        HandlerList.unregisterAll(chatListener);
    }
}
