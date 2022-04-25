package xyz.destiall.sgcraftcreative.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

import java.util.Arrays;
import java.util.List;

public class RPlaceCmd extends AbstractCommand {
    public RPlaceCmd(SGCraftCreative plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /sgcraftcreative rplace [tp,play,stop,reset]");
            return;
        }
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "tp", "teleport" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sgcraftcreative rplace teleport [player]");
                    return;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid player!");
                    return;
                }
                World world = plugin.getrPlaceHandler().getWorld();
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "rPlace world does not exist!");
                    return;
                }
                player.teleport(world.getSpawnLocation());
            }
            case "play", "p" -> {
                int length = args.length > 1 ? Integer.parseInt(args[1]) : 60;
                if (!plugin.getrPlaceHandler().getPlayer().play(length)) {
                    sender.sendMessage(ChatColor.RED + "r/Place is already playing!");
                }
            }
            case "stop", "s" -> {
                if (!plugin.getrPlaceHandler().getPlayer().stop()) {
                    sender.sendMessage(ChatColor.RED + "r/Place is not playing!");
                }
            }
            case "reset" -> {
                plugin.getrPlaceHandler().getRecorder().reset();
            }
            default -> sender.sendMessage(ChatColor.RED + "Usage: /sgcraftcreative rplace [tp,play,stop,reset]");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Arrays.asList("teleport", "play", "stop", "reset");
    }
}
