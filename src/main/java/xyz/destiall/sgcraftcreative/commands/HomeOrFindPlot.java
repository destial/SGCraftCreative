package xyz.destiall.sgcraftcreative.commands;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.query.PlotQuery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

public class HomeOrFindPlot extends AbstractCommand {
    public HomeOrFindPlot(SGCraftCreative plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args == null || args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /sgcraftcreative homeorfind <player> <world>");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Unable to find player " + args[0]);
            return;
        }
        String world = args[1];
        Plot plot;
        try {
            plot = PlotQuery.newQuery().inWorld(world).whereBasePlot().ownersInclude(player.getUniqueId()).asList().get(0);
        } catch (Exception e) {
            try {
                plot = PlotSquared.get().getPlotAreaManager().getPlotAreasSet(world).stream().findFirst().get().getNextFreePlot(PlotPlayer.from(player), null);
            } catch (Exception ex) {
                plot = null;
            }
        }
        if (plot == null) {
            sender.sendMessage(ChatColor.RED + "Unable to find home or available plot for " + player.getName());
            return;
        }
        plot.teleportPlayer(PlotPlayer.from(player), TeleportCause.COMMAND_AREA_TELEPORT ,result -> {});
    }
}
