package xyz.destiall.sgcraftcreative;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.sgcraftcreative.commands.CommandHandler;
import xyz.destiall.sgcraftcreative.rplace.RPlaceHandler;

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
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }
}
