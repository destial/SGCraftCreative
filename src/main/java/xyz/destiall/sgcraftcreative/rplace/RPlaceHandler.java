package xyz.destiall.sgcraftcreative.rplace;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.dynmap.bukkit.DynmapPlugin;
import xyz.destiall.sgcraftcreative.SGCraftCreative;
import xyz.destiall.sgcraftcreative.rplace.recorder.RPlacePlayer;
import xyz.destiall.sgcraftcreative.rplace.recorder.RPlaceRecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RPlaceHandler implements Listener {
    private final Map<UUID, Long> placers = new ConcurrentHashMap<>();
    private final Map<String, Long> delays = new HashMap<>();
    private final SGCraftCreative plugin;
    private final RPlaceRecorder recorder;
    private final RPlacePlayer player;
    private long currentTick;

    private World world;
    private BoundingBox bounds;
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
                Player player = Bukkit.getPlayer(entry.getKey());
                if (entry.getValue() <= System.currentTimeMillis()) {
                    remove.add(entry.getKey());
                    if (player == null || player.getWorld() != world) continue;
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
                } else {
                    if (player == null || player.getWorld() != world) continue;
                    long left = entry.getValue() - System.currentTimeMillis();
                    left /= 1000;
                    TextComponent component = new TextComponent(color(stillTimerMessage.replace("{seconds}", "" + left)));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                }
            }

            for (UUID uuid : remove) {
                placers.remove(uuid);
            }
            currentTick++;
        }, 1L, 1L);

        recorder = new RPlaceRecorder(plugin);
        recorder.load();

        player = new RPlacePlayer(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (world == null && getWorld() == null) return;
        Block target = e.getClickedBlock();
        if (target == null) return;
        if (target.getWorld() != world) return;
        ItemStack hand = e.getItem();
        if (hand == null) return;

        e.setCancelled(true);

        if (getPlayer().isPlaying()) {
            e.getPlayer().sendMessage(ChatColor.RED + "r/place is currently replaying!");
            return;
        }

        if (!hand.getType().isOccluding() || target.getType() == hand.getType()) return;

        if (placers.containsKey(e.getPlayer().getUniqueId()))
            return;

        if (target.getLocation().toVector().isInAABB(bounds.getMin(), bounds.getMax())) {

            target.setType(hand.getType());
            long delay = delays.get("default");
            for (Map.Entry<String, Long> entry : delays.entrySet()) {
                if (e.getPlayer().hasPermission("sgcraft.delay." + entry.getKey())) {
                    if (entry.getValue() < delay) delay = entry.getValue();
                }
            }
            if (delay != 0) {
                placers.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (delay * 1000));
            }
            recorder.record(target, e.getPlayer().getUniqueId(), currentTick);
            DynmapPlugin.plugin.triggerRenderOfBlock(world.getName(), target.getX(), target.getY(), target.getZ());
            return;
        }

        e.getPlayer().sendMessage(outOfBoundsMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (world == null && getWorld() == null) return;
        if (e.getBlockPlaced().getWorld() != world) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (world == null && getWorld() == null) return;
        Block broken = e.getBlock();
        if (broken.getWorld() != world) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onLoadWorld(WorldLoadEvent e) {
        if (world != null) return;
        if (e.getWorld().getName().equals("rPlace")) {
            world = e.getWorld();
        }
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        world = Bukkit.getWorld(config.getString("r-place.world", "rPlace"));
        Vector minBounds = new Vector(config.getInt("r-place.min-x", -1000), config.getInt("r-place.build-y", -60), config.getInt("r-place.min-z", -1000));
        Vector maxBounds = new Vector(config.getInt("r-place.max-x", 1000), minBounds.getY(), config.getInt("r-place.max-z", 1000));
        bounds = new BoundingBox(minBounds.getX(), minBounds.getY(), minBounds.getZ(), maxBounds.getX(), maxBounds.getY(), maxBounds.getZ());
        outOfBoundsMessage = color(config.getString("messages.r-place.out-of-bounds", "&cYou are out of bounds!"));
        stillTimerMessage = color(config.getString("messages.r-place.still-timer", "&cYou have {seconds} seconds left before you can place another block!"));
        delays.clear();
        for (String key : config.getConfigurationSection("r-place.delay-groups").getKeys(false)) {
            delays.put(key, config.getLong("r-place.delay-groups." + key));
        }
    }

    public RPlaceRecorder getRecorder() {
        return recorder;
    }

    public RPlacePlayer getPlayer() {
        return player;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public SGCraftCreative getPlugin() {
        return plugin;
    }

    public World getWorld() {
        return world = world == null ? Bukkit.getWorld("rPlace") : world;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
