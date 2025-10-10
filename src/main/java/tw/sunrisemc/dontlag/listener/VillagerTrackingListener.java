package tw.sunrisemc.dontlag.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import tw.sunrisemc.dontlag.DontLag;

/**
 * 村民追蹤監聽器
 * 追蹤村民的生成、移除和區塊加載
 */
public class VillagerTrackingListener implements Listener {
    
    private final DontLag plugin;
    
    public VillagerTrackingListener(DontLag plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 監聽生物生成事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            Villager villager = (Villager) event.getEntity();
            
            // 添加到追蹤系統
            plugin.getAutoVillagerOptimizer().addVillager(villager);
        }
    }
    
    /**
     * 監聽實體死亡事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            Villager villager = (Villager) event.getEntity();
            
            // 從追蹤系統移除
            plugin.getAutoVillagerOptimizer().removeVillager(villager);
        }
    }
    
    /**
     * 監聽區塊加載事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // 掃描區塊中的村民
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Villager) {
                plugin.getAutoVillagerOptimizer().addVillager((Villager) entity);
            }
        }
    }
    
    /**
     * 監聽區塊卸載事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        // 從追蹤系統移除該區塊的村民
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Villager) {
                plugin.getAutoVillagerOptimizer().removeVillager((Villager) entity);
            }
        }
    }
}

