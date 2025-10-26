package tw.sunrisemc.dontlag.manager;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChickenManager {
    
    private final Plugin plugin;
    private final Map<UUID, Boolean> optimizedChickens = new HashMap<>();
    
    public ChickenManager(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 切換雞的優化狀態
     */
    public boolean toggleOptimization(Entity entity) {
        if (!(entity instanceof Chicken)) {
            return false;
        }
        
        Chicken chicken = (Chicken) entity;
        UUID uuid = entity.getUniqueId();
        
        boolean currentlyOptimized = isOptimized(uuid);
        
        if (currentlyOptimized) {
            // 恢復雞的功能
            restoreChicken(chicken);
            optimizedChickens.remove(uuid);
            return false;
        } else {
            // 優化雞
            optimizeChicken(chicken);
            optimizedChickens.put(uuid, true);
            return true;
        }
    }
    
    /**
     * 優化雞 - 只保留成長和下蛋功能
     */
    private void optimizeChicken(Chicken chicken) {
        // 關閉大部分 AI，但保留成長和下蛋相關的行為
        chicken.setAware(false);
        
        // 設置雞不會四處遊蕩，但仍可下蛋
        chicken.setCollidable(true); // 保持碰撞
        
        // 移除尋路目標，但雞仍可成長和下蛋
        chicken.setAI(false);
        
        // 雞的特殊處理：保持成長能力
        // 如果是小雞，允許繼續成長
        if (!chicken.isAdult()) {
            chicken.setAgeLock(false); // 確保不鎖定年齡，允許成長
        }
        
        // 保持下蛋能力 - 這是雞的自然行為，不需要特殊設置
        // 下蛋是基於時間的自然行為，不依賴於 AI 系統
    }
    
    /**
     * 恢復雞的正常功能
     */
    private void restoreChicken(Chicken chicken) {
        // 恢復 AI
        chicken.setAware(true);
        chicken.setAI(true);
        
        // 恢復碰撞
        chicken.setCollidable(true);
        
        // 恢復年齡鎖定狀態
        chicken.setAgeLock(false);
    }
    
    /**
     * 檢查雞是否被優化
     */
    public boolean isOptimized(UUID chickenUUID) {
        return optimizedChickens.getOrDefault(chickenUUID, false);
    }
    
    /**
     * 檢查實體是否為優化過的雞
     */
    public boolean isOptimized(Entity entity) {
        if (!(entity instanceof Chicken)) {
            return false;
        }
        
        return isOptimized(entity.getUniqueId());
    }
    
    /**
     * 恢復所有雞
     */
    public void restoreAll() {
        for (UUID uuid : optimizedChickens.keySet()) {
            plugin.getServer().getWorlds().forEach(world -> {
                world.getEntities().stream()
                    .filter(e -> e.getUniqueId().equals(uuid))
                    .filter(e -> e instanceof Chicken)
                    .forEach(e -> restoreChicken((Chicken) e));
            });
        }
        optimizedChickens.clear();
    }
    
    /**
     * 獲取被優化的雞數量
     */
    public int getOptimizedCount() {
        return optimizedChickens.size();
    }
}
