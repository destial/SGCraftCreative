package xyz.destiall.sgcraftcreative.commands;

import org.bukkit.command.CommandSender;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand {
    protected final SGCraftCreative plugin;

    public AbstractCommand(SGCraftCreative plugin) {
        this.plugin = plugin;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
