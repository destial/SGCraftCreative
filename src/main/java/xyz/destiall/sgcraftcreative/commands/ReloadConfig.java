package xyz.destiall.sgcraftcreative.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

public class ReloadConfig extends AbstractCommand {
    public ReloadConfig(SGCraftCreative plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        plugin.getrPlaceHandler().reload();
        sender.sendMessage(ChatColor.GREEN + "Reloaded configuration...");
    }
}
