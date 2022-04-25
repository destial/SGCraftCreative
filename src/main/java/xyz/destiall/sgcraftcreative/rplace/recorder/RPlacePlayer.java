package xyz.destiall.sgcraftcreative.rplace.recorder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.sgcraftcreative.rplace.RPlaceHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RPlacePlayer {
    private final RPlaceHandler handler;

    private boolean playing;
    private BukkitTask playingTask;

    public RPlacePlayer(RPlaceHandler handler) {
        this.handler = handler;
        playing = false;
    }

    public boolean play(int length) {
        if (playing) return false;
        playing = true;

        Bukkit.getScheduler().runTask(handler.getPlugin(), () -> {
            for (RPlaceRecorder.BlockPlace place : handler.getRecorder().getBlocks()) {
                handler.getWorld().getBlockAt(place.x, place.y, place.z).setType(Material.WHITE_CONCRETE);
            }

            AtomicLong currentTicks = new AtomicLong();
            int speed = (int) ((handler.getRecorder().getLastTick() * 20) / length);
            if (speed <= 0) speed = 1;

            int finalSpeed = speed;
            playingTask = Bukkit.getScheduler().runTaskTimer(handler.getPlugin(), () -> {
                long nextTick = currentTicks.get() + finalSpeed;
                for (long tick = currentTicks.get(); tick < nextTick; tick++) {
                    List<RPlaceRecorder.BlockPlace> placements = handler.getRecorder().getPlacements(tick);
                    for (RPlaceRecorder.BlockPlace place : placements) {
                        handler.getWorld().getBlockAt(place.x, place.y, place.z).setType(place.material);
                    }
                }
                currentTicks.set(nextTick);
                if (currentTicks.get() >= handler.getRecorder().getLastTick()) {
                    stop();
                }
            }, 1L, 1L);
        });
        return true;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean stop() {
        if (!playing || playingTask == null) return false;
        playingTask.cancel();
        playingTask = null;
        playing = false;
        Bukkit.getScheduler().runTask(handler.getPlugin(), () -> {
            List<RPlaceRecorder.BlockPlace> placements = handler.getRecorder().getBlocks();
            for (RPlaceRecorder.BlockPlace place : placements) {
                handler.getWorld().getBlockAt(place.x, place.y, place.z).setType(place.material);
            }
        });
        return true;
    }
}
