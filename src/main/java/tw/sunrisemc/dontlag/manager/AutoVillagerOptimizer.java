package tw.sunrisemc.dontlag.manager;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自動村民優化器
 * 當某個區域的村民數量超過閾值時，自動優化所有村民
 */
public class AutoVillagerOptimizer {
    
    private final Plugin plugin;
    private final VillagerManager villagerManager;
    
    // 使用 ConcurrentHashMap 以支援多線程
    private final Map<String, Set<UUID>> chunkVillagers = new ConcurrentHashMap<>();
    private final Set<UUID> permanentlyOptimized = ConcurrentHashMap.newKeySet();
    
    private int threshold = 5; // 村民數量閾值
    private int checkInterval = 600; // 檢查間隔（ticks，30秒）
    private boolean autoOptimizeEnabled = true;
    
    private BukkitRunnable checkTask;
    
    public AutoVillagerOptimizer(Plugin plugin, VillagerManager villagerManager) {
        this.plugin = plugin;
        this.villagerManager = villagerManager;
    }
    
    /**
     * 啟動自動優化系統
     */
    public void start() {
        if (!autoOptimizeEnabled) {
            return;
        }
        
        // 初始掃描所有已加載的村民
        scanAllLoadedVillagers();
        
        // 啟動定時檢查任務（異步）
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAndOptimizeVillagers();
            }
        };
        
        // 延遲 20 ticks（1秒）後開始，每 checkInterval ticks 執行一次
        checkTask.runTaskTimerAsynchronously(plugin, 20L, checkInterval);
        
        plugin.getLogger().info("自動村民優化系統已啟動（閾值: " + threshold + " 隻）");
    }
    
    /**
     * 停止自動優化系統
     */
    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }
    
    /**
     * 初始掃描所有已加載的村民
     */
    private void scanAllLoadedVillagers() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Villager) {
                        addVillager((Villager) entity);
                    }
                }
            }
        }
    }
    
    /**
     * 添加村民到追蹤系統
     */
    public void addVillager(Villager villager) {
        String chunkKey = getChunkKey(villager.getLocation().getChunk());
        UUID villagerUUID = villager.getUniqueId();
        
        chunkVillagers.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(villagerUUID);
        
        // 檢查該區塊是否需要優化
        checkChunk(chunkKey, villager.getWorld());
    }
    
    /**
     * 移除村民從追蹤系統
     */
    public void removeVillager(Villager villager) {
        String chunkKey = getChunkKey(villager.getLocation().getChunk());
        UUID villagerUUID = villager.getUniqueId();
        
        Set<UUID> villagers = chunkVillagers.get(chunkKey);
        if (villagers != null) {
            villagers.remove(villagerUUID);
            
            // 如果該區塊沒有村民了，移除記錄
            if (villagers.isEmpty()) {
                chunkVillagers.remove(chunkKey);
            }
        }
        
        permanentlyOptimized.remove(villagerUUID);
    }
    
    /**
     * 檢查並優化所有區塊的村民
     */
    private void checkAndOptimizeVillagers() {
        int optimizedCount = 0;
        
        // 創建副本以避免併發修改
        Map<String, Set<UUID>> chunksCopy = new HashMap<>(chunkVillagers);
        
        for (Map.Entry<String, Set<UUID>> entry : chunksCopy.entrySet()) {
            String chunkKey = entry.getKey();
            Set<UUID> villagerUUIDs = entry.getValue();
            
            // 清理已不存在的村民
            villagerUUIDs.removeIf(uuid -> !isVillagerExists(uuid));
            
            // 檢查村民數量
            if (villagerUUIDs.size() >= threshold) {
                // 需要在主線程中執行優化
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    optimizeChunkVillagers(chunkKey, villagerUUIDs);
                });
                optimizedCount += villagerUUIDs.size();
            }
        }
        
        if (optimizedCount > 0) {
            plugin.getLogger().info("自動優化系統: 優化了 " + optimizedCount + " 隻村民");
        }
    }
    
    /**
     * 檢查特定區塊
     */
    private void checkChunk(String chunkKey, World world) {
        Set<UUID> villagerUUIDs = chunkVillagers.get(chunkKey);
        
        if (villagerUUIDs != null && villagerUUIDs.size() >= threshold) {
            // 在主線程執行優化
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                optimizeChunkVillagers(chunkKey, villagerUUIDs);
            });
        }
    }
    
    /**
     * 優化區塊中的所有村民
     */
    private void optimizeChunkVillagers(String chunkKey, Set<UUID> villagerUUIDs) {
        int count = 0;
        
        for (UUID uuid : villagerUUIDs) {
            // 跳過已經永久優化的村民
            if (permanentlyOptimized.contains(uuid)) {
                continue;
            }
            
            // 查找村民實體
            for (World world : plugin.getServer().getWorlds()) {
                Entity entity = getEntityByUUID(world, uuid);
                if (entity instanceof Villager) {
                    Villager villager = (Villager) entity;
                    
                    // 優化村民
                    optimizeVillagerPermanently(villager);
                    permanentlyOptimized.add(uuid);
                    count++;
                    break;
                }
            }
        }
        
        if (count > 0) {
            plugin.getLogger().info("區塊 " + chunkKey + " 的村民數量超過 " + threshold + 
                                  " 隻，已自動優化 " + count + " 隻村民（永久）");
        }
    }
    
    /**
     * 永久優化村民
     */
    private void optimizeVillagerPermanently(Villager villager) {
        // 關閉 AI
        villager.setAware(false);
        
        // 清除代理
        villager.getGossips().forEach((uuid, gossip) -> {
            villager.getGossips().remove(uuid);
        });
        
        // 清除記憶
        villager.getMemory().clear();
        
        // 設置為可碰撞（以便交易）
        villager.setCollidable(true);
        
        // 設置為靜音（可選，減少聲音負擔）
        villager.setSilent(false); // 保留聲音以便玩家知道村民存在
    }
    
    /**
     * 獲取區塊鍵值
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    
    /**
     * 檢查村民是否存在
     */
    private boolean isVillagerExists(UUID uuid) {
        for (World world : plugin.getServer().getWorlds()) {
            if (getEntityByUUID(world, uuid) != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 通過 UUID 獲取實體
     */
    private Entity getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUniqueId().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }
    
    /**
     * 檢查村民是否被永久優化
     */
    public boolean isPermanentlyOptimized(UUID uuid) {
        return permanentlyOptimized.contains(uuid);
    }
    
    /**
     * 解除村民的永久優化（OP 管理員棒專用）
     */
    public boolean unlockVillager(Villager villager) {
        UUID uuid = villager.getUniqueId();
        
        if (!permanentlyOptimized.contains(uuid)) {
            return false; // 不是永久優化的村民
        }
        
        // 恢復村民功能
        villager.setAware(true);
        villager.setCollidable(true);
        
        // 從永久優化列表移除
        permanentlyOptimized.remove(uuid);
        
        // 從區塊追蹤中移除（避免再次被優化）
        String chunkKey = getChunkKey(villager.getLocation().getChunk());
        Set<UUID> villagers = chunkVillagers.get(chunkKey);
        if (villagers != null) {
            villagers.remove(uuid);
        }
        
        plugin.getLogger().info("管理員解除了村民 " + uuid + " 的永久優化");
        return true;
    }
    
    /**
     * 設置村民數量閾值
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    
    /**
     * 設置檢查間隔（秒）
     */
    public void setCheckInterval(int seconds) {
        this.checkInterval = seconds * 20; // 轉換為 ticks
        
        // 重啟任務
        if (checkTask != null) {
            stop();
            start();
        }
    }
    
    /**
     * 設置是否啟用自動優化
     */
    public void setAutoOptimizeEnabled(boolean enabled) {
        this.autoOptimizeEnabled = enabled;
        
        if (enabled) {
            start();
        } else {
            stop();
        }
    }
    
    /**
     * 獲取統計資訊
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChunks", chunkVillagers.size());
        stats.put("totalVillagers", chunkVillagers.values().stream().mapToInt(Set::size).sum());
        stats.put("permanentlyOptimized", permanentlyOptimized.size());
        stats.put("threshold", threshold);
        stats.put("enabled", autoOptimizeEnabled);
        return stats;
    }
    
    /**
     * 清除所有追蹤數據
     */
    public void clear() {
        chunkVillagers.clear();
        permanentlyOptimized.clear();
    }
}

