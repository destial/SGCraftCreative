package xyz.destiall.sgcraftcreative.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler implements CommandExecutor, TabExecutor {
    private final Map<String, AbstractCommand> commands;

    public CommandHandler(SGCraftCreative plugin) {
        commands = new HashMap<>();

        commands.put("homeorfind", new HomeOrFindPlot(plugin));
        commands.put("reload", new ReloadConfig(plugin));
        commands.put("rplace", new RPlaceCmd(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Commands: /sgcraftcreative " + commands.keySet());
            return false;
        }
        AbstractCommand cmd = commands.get(args[0].toLowerCase());
        if (cmd == null) {
            sender.sendMessage(ChatColor.RED + "Invalid Command: /sgcraftcreative " + commands.keySet());
            return false;
        }
        cmd.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return args == null || args.length == 0 ? new ArrayList<>(commands.keySet()) :
                commands.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(args[0].toLowerCase())).findFirst()
                        .map(entry -> entry.getValue().tabComplete(sender, Arrays.copyOfRange(args, 1, args.length)))
                        .orElse(new ArrayList<>());
    }
}
