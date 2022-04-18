package xyz.destiall.sgcraftcreative.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftcreative.SGCraftCreative;
import xyz.destiall.sgcraftcreative.rplace.generator.CanvasBiomeProvider;
import xyz.destiall.sgcraftcreative.rplace.generator.CanvasGenerator;

public class RPlaceCmd extends AbstractCommand {
    public RPlaceCmd(SGCraftCreative plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /sgcraftcreative rplace [setup,tp]");
            return;
        }
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "setup" -> {
                World world = Bukkit.getWorld("rPlace");
                if (world != null) {
                    sender.sendMessage(ChatColor.RED + "rPlace world already exists!");
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    World rWorld = new WorldCreator("rPlace")
                            .generator(new CanvasGenerator())
                            .biomeProvider(new CanvasBiomeProvider())
                            .environment(World.Environment.NORMAL)
                            .createWorld();

                    if (rWorld == null) {
                        sender.sendMessage(ChatColor.RED + "Unable to create rPlace world!");
                        return;
                    }
                    rWorld.getWorldBorder().setCenter(0, 0);
                    rWorld.getWorldBorder().setSize(1000);
                    rWorld.setDifficulty(Difficulty.PEACEFUL);
                    rWorld.setThundering(false);
                    rWorld.setAutoSave(true);
                    rWorld.setChunkForceLoaded(0, 0, true);
                    rWorld.setPVP(false);
                    rWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                    rWorld.setHardcore(false);
                    rWorld.setStorm(false);
                    rWorld.setSpawnFlags(false, false);
                    rWorld.setSpawnLocation(0, 30, 0);

                    sender.sendMessage(ChatColor.GREEN + "Successfully created rPlace world!");
                    if (sender instanceof Player) {
                        ((Player) sender).teleport(rWorld.getSpawnLocation());
                    }
                });
            }
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
                    world = Bukkit.getWorld("rPlace");
                }
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "rPlace world does not exist!");
                    sender.sendMessage(ChatColor.RED + "You can set it up by doing /sgcraftcreative rplace setup");
                    return;
                }
                player.teleport(world.getSpawnLocation());
            }
            default -> sender.sendMessage(ChatColor.RED + "Usage: /sgcraftcreative rplace [setup,tp]");
        }
    }
}
