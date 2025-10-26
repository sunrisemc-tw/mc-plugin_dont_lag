package tw.sunrisemc.dontlag.listener;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import tw.sunrisemc.dontlag.DontLag;

/**
 * 雞追蹤監聽器
 * 追蹤雞的生成、移除和區塊加載
 */
public class ChickenTrackingListener implements Listener {
    
    private final DontLag plugin;
    
    public ChickenTrackingListener(DontLag plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 監聽生物生成事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.CHICKEN) {
            Chicken chicken = (Chicken) event.getEntity();
            
            // 添加到追蹤系統
            plugin.getAutoChickenOptimizer().addChicken(chicken);
        }
    }
    
    /**
     * 監聽實體死亡事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.CHICKEN) {
            Chicken chicken = (Chicken) event.getEntity();
            
            // 從追蹤系統移除
            plugin.getAutoChickenOptimizer().removeChicken(chicken);
        }
    }
    
    /**
     * 監聽區塊加載事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // 掃描區塊中的雞
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Chicken) {
                plugin.getAutoChickenOptimizer().addChicken((Chicken) entity);
            }
        }
    }
    
    /**
     * 監聽區塊卸載事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        // 從追蹤系統移除該區塊的雞
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Chicken) {
                plugin.getAutoChickenOptimizer().removeChicken((Chicken) entity);
            }
        }
    }
}
