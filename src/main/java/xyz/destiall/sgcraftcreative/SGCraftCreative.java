package xyz.destiall.sgcraftcreative;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.destiall.sgcraftcreative.commands.CommandHandler;
import xyz.destiall.sgcraftcreative.rplace.RPlaceHandler;
import xyz.destiall.sgcraftcreative.rplace.generator.CanvasBiomeProvider;
import xyz.destiall.sgcraftcreative.rplace.generator.CanvasGenerator;

public final class SGCraftCreative extends JavaPlugin {

    private RPlaceHandler rPlaceHandler;

    @Override
    public void onEnable() {
        getServer().getPluginCommand("sgcraftcreative").setExecutor(new CommandHandler(this));
        getServer().getPluginManager().registerEvents(rPlaceHandler = new RPlaceHandler(this), this);
    }

    public RPlaceHandler getrPlaceHandler() {
        return rPlaceHandler;
    }

    @Override
    public void onDisable() {
        if (rPlaceHandler != null) rPlaceHandler.getRecorder().save();
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new CanvasGenerator();
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull String worldName, @Nullable String id) {
        return new CanvasBiomeProvider();
    }
}
