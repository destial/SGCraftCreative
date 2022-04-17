package xyz.destiall.sgcraftcreative.rplace;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RPlaceHandler implements Listener {
    private final SGCraftCreative plugin;
    private final Map<UUID, Long> placers = new ConcurrentHashMap<>();
    private World world;
    private BoundingBox bounds;
    private long delay;
    private String outOfBoundsMessage;
    private String stillTimerMessage;

    public RPlaceHandler(SGCraftCreative plugin) {
        this.plugin = plugin;
        if (plugin.getDataFolder().mkdir()) {
            plugin.saveDefaultConfig();
        }

        reload();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<UUID> remove = new ArrayList<>();
            for (Map.Entry<UUID, Long> entry : placers.entrySet()) {
                if (entry.getValue() <= System.currentTimeMillis()) {
                    remove.add(entry.getKey());
                } else {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || player.getWorld() != world) continue;
                    long left = entry.getValue() - System.currentTimeMillis();
                    left /= 1000;
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(color(stillTimerMessage.replace("{seconds}", "" + left))));
                }
            }

            for (UUID uuid : remove) {
                placers.remove(uuid);
            }
        }, 0L, 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (world == null) return;
        ItemStack hand = e.getItem();
        if (hand == null) return;
        if (!hand.getType().isBlock()) return;

        Block target = e.getClickedBlock();
        if (target == null) return;
        if (target.getWorld() != world) return;

        if (!e.getPlayer().hasPermission("sgcraft.admin")) {
            e.setCancelled(true);
        }

        if (placers.containsKey(e.getPlayer().getUniqueId())) {
            long left = placers.get(e.getPlayer().getUniqueId()) - System.currentTimeMillis();
            left /= 1000;
            e.getPlayer().sendMessage(stillTimerMessage.replace("{seconds}", "" + left));
            return;
        }

        if (target.getLocation().toVector().isInAABB(bounds.getMin(), bounds.getMax())) {
            target.setType(hand.getType());
            placers.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (delay * 1000));
            return;
        }

        e.getPlayer().sendMessage(outOfBoundsMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getWorld() != world) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (world == null) return;
        Block broken = e.getBlock();
        if (broken.getWorld() != world) return;
        e.setCancelled(true);
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        world = Bukkit.getWorld(config.getString("r-place.world", "rPlace"));
        Vector minBounds = new Vector(config.getInt("r-place.min-x", -1000), config.getInt("r-place.build-y", -60), config.getInt("r-place.min-z", -1000));
        Vector maxBounds = new Vector(config.getInt("r-place.max-x", 1000), minBounds.getY() + 1, config.getInt("r-place.max-z", 1000));
        bounds = new BoundingBox(minBounds.getX(), minBounds.getY(), minBounds.getZ(), maxBounds.getX(), maxBounds.getY(), maxBounds.getZ());
        delay = config.getLong("r-place.delay", 300);
        outOfBoundsMessage = color(config.getString("messages.r-place.out-of-bounds", "&cYou are out of bounds!"));
        stillTimerMessage = color(config.getString("messages.r-place.still-timer", "&cYou have {seconds} seconds left before you can place another block!"));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
