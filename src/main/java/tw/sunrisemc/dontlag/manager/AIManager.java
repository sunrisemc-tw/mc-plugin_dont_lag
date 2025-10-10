package tw.sunrisemc.dontlag.manager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AIManager {
    
    private final Plugin plugin;
    private final Map<UUID, Boolean> disabledAIEntities = new HashMap<>();
    
    public AIManager(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 切換生物的 AI 狀態
     */
    public boolean toggleAI(Entity entity) {
        if (!(entity instanceof Mob)) {
            return false;
        }
        
        Mob mob = (Mob) entity;
        UUID uuid = entity.getUniqueId();
        
        boolean currentlyDisabled = isAIDisabled(uuid);
        
        if (currentlyDisabled) {
            // 啟用 AI
            mob.setAware(true);
            disabledAIEntities.remove(uuid);
            return false;
        } else {
            // 禁用 AI
            mob.setAware(false);
            disabledAIEntities.put(uuid, true);
            return true;
        }
    }
    
    /**
     * 檢查生物的 AI 是否被禁用
     */
    public boolean isAIDisabled(UUID entityUUID) {
        return disabledAIEntities.getOrDefault(entityUUID, false);
    }
    
    /**
     * 檢查實體的 AI 狀態
     */
    public boolean isAIDisabled(Entity entity) {
        if (!(entity instanceof Mob)) {
            return false;
        }
        
        Mob mob = (Mob) entity;
        return !mob.isAware();
    }
    
    /**
     * 恢復所有生物的 AI
     */
    public void restoreAll() {
        for (UUID uuid : disabledAIEntities.keySet()) {
            plugin.getServer().getWorlds().forEach(world -> {
                world.getEntities().stream()
                    .filter(e -> e.getUniqueId().equals(uuid))
                    .filter(e -> e instanceof Mob)
                    .forEach(e -> ((Mob) e).setAware(true));
            });
        }
        disabledAIEntities.clear();
    }
    
    /**
     * 獲取被禁用 AI 的生物數量
     */
    public int getDisabledCount() {
        return disabledAIEntities.size();
    }
}


