package xyz.destiall.sgcraftcreative.rplace.recorder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.destiall.sgcraftcreative.SGCraftCreative;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RPlaceRecorder {
    private final List<BlockPlace> blocks = new LinkedList<>();
    private final SGCraftCreative plugin;
    private final AtomicInteger integer;
    private File file;
    private long lastTick;

    public RPlaceRecorder(SGCraftCreative plugin) {
        this.plugin = plugin;
        integer = new AtomicInteger();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (integer.get() == blocks.size()) return;
            save();
            plugin.getLogger().info("Saved r/place blocks to database.");
        }, 100L, 20L * 10);
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "blocks.yml");
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map<String, Object> map : (List<Map<String, Object>>) yaml.getList("blocks", new ArrayList<>())) {
            BlockPlace blockPlace = new BlockPlace();
            blockPlace.material = Material.getMaterial((String) map.get("material"));
            blockPlace.uuid = UUID.fromString((String) map.get("uuid"));
            blockPlace.tick = (int) map.get("tick");
            blockPlace.x = (int) map.get("x");
            blockPlace.y = (int) map.get("y");
            blockPlace.z = (int) map.get("z");
            if (blockPlace.tick > lastTick) {
                lastTick = blockPlace.tick;
            }
            blocks.add(blockPlace);
        }
        blocks.sort(Comparator.comparingLong(b -> b.tick));
        integer.set(blocks.size());
    }

    public void reset() {
        file.delete();
        blocks.clear();
        lastTick = 0;
        integer.set(0);
    }

    public void save() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<Map<String, Object>> list = new ArrayList<>(blocks.size());
        for (BlockPlace place : blocks) {
            Map<String, Object> map = new HashMap<>();
            map.put("x", place.x);
            map.put("y", place.y);
            map.put("z", place.z);
            map.put("material", place.material.name());
            map.put("uuid", place.uuid.toString());
            map.put("tick", place.tick);
            list.add(map);
        }
        yaml.set("blocks", list);
        try {
            yaml.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        integer.set(blocks.size());
    }

    public List<BlockPlace> getPlacements(long tick) {
        return blocks.stream().filter(bp -> bp.tick == tick).collect(Collectors.toList());
    }

    public List<BlockPlace> getBlocks() {
        return blocks;
    }

    public void record(Block block, UUID placer, long tick) {
        BlockPlace blockPlace = new BlockPlace();
        blockPlace.material = block.getType();
        blockPlace.tick = tick;
        blockPlace.x = block.getX();
        blockPlace.y = block.getY();
        blockPlace.z = block.getZ();
        blockPlace.uuid = placer;
        lastTick = tick;
        blocks.add(blockPlace);
    }

    public long getLastTick() {
        return lastTick;
    }

    public static class BlockPlace {

        protected int x;
        protected int y;
        protected int z;
        protected Material material;
        protected UUID uuid;
        protected long tick;
    }
}
