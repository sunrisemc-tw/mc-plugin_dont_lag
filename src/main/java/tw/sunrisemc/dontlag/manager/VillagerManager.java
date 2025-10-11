package tw.sunrisemc.dontlag.manager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerManager {
    
    private final Plugin plugin;
    private final Map<UUID, Boolean> optimizedVillagers = new HashMap<>();
    
    public VillagerManager(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 切換村民的優化狀態
     */
    public boolean toggleOptimization(Entity entity) {
        if (!(entity instanceof Villager)) {
            return false;
        }
        
        Villager villager = (Villager) entity;
        UUID uuid = entity.getUniqueId();
        
        boolean currentlyOptimized = isOptimized(uuid);
        
        if (currentlyOptimized) {
            // 恢復村民功能
            restoreVillager(villager);
            optimizedVillagers.remove(uuid);
            return false;
        } else {
            // 優化村民
            optimizeVillager(villager);
            optimizedVillagers.put(uuid, true);
            return true;
        }
    }
    
    /**
     * 優化村民 - 只保留交易和補貨功能
     */
    private void optimizeVillager(Villager villager) {
        // 關閉 AI（但村民仍可交易和補貨）
        villager.setAware(false);
        
        // 設置村民不會四處遊蕩
        villager.setCollidable(true); // 保持碰撞以便互動
        
        // 移除尋路目標
        villager.setAI(false);
        
        // 注意：Gossip 和 Memory 清除需要 Paper API 或 NMS
        // 在標準 Spigot 中，setAware(false) 已經足夠停止大部分 AI 行為
    }
    
    /**
     * 恢復村民的正常功能
     */
    private void restoreVillager(Villager villager) {
        // 恢復 AI
        villager.setAware(true);
        villager.setAI(true);
        
        // 恢復碰撞
        villager.setCollidable(true);
    }
    
    /**
     * 檢查村民是否被優化
     */
    public boolean isOptimized(UUID villagerUUID) {
        return optimizedVillagers.getOrDefault(villagerUUID, false);
    }
    
    /**
     * 檢查實體是否為優化過的村民
     */
    public boolean isOptimized(Entity entity) {
        if (!(entity instanceof Villager)) {
            return false;
        }
        
        return isOptimized(entity.getUniqueId());
    }
    
    /**
     * 恢復所有村民
     */
    public void restoreAll() {
        for (UUID uuid : optimizedVillagers.keySet()) {
            plugin.getServer().getWorlds().forEach(world -> {
                world.getEntities().stream()
                    .filter(e -> e.getUniqueId().equals(uuid))
                    .filter(e -> e instanceof Villager)
                    .forEach(e -> restoreVillager((Villager) e));
            });
        }
        optimizedVillagers.clear();
    }
    
    /**
     * 獲取被優化的村民數量
     */
    public int getOptimizedCount() {
        return optimizedVillagers.size();
    }
}

