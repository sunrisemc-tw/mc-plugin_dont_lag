package tw.sunrisemc.dontlag.manager;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import tw.sunrisemc.dontlag.util.DiscordWebhook;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自動雞優化器
 * 當某個區域的雞數量超過閾值時，自動優化所有雞
 */
public class AutoChickenOptimizer {
    
    private final Plugin plugin;
    private final ChickenManager chickenManager;
    private DiscordWebhook discordWebhook;
    
    // 使用 ConcurrentHashMap 以支援多線程
    private final Map<String, Set<UUID>> chunkChickens = new ConcurrentHashMap<>();
    private final Set<UUID> permanentlyOptimized = ConcurrentHashMap.newKeySet();
    
    // 追蹤雞的成年時間和最後互動時間
    private final Map<UUID, Long> chickenAdultTime = new ConcurrentHashMap<>(); // 雞成年時間戳
    private final Map<UUID, Long> lastPlayerInteraction = new ConcurrentHashMap<>(); // 最後玩家互動時間
    
    // 強制鎖定系統（密集雞檢測）
    private final Set<UUID> forceLocked = ConcurrentHashMap.newKeySet(); // 強制鎖定的雞
    
    private int threshold = 10; // 雞數量閾值（比村民高一些）
    private int checkInterval = 600; // 檢查間隔（ticks，30秒）
    private boolean autoOptimizeEnabled = true;
    
    // 密集檢測設定
    private int densityThreshold = 8; // 1格內超過此數量視為密集
    private static final double DENSITY_RADIUS = 1.0; // 密集檢測半徑
    
    // 時間常數（毫秒）
    private static final long ADULT_GRACE_PERIOD = 20 * 1000; // 成年後20秒寬限期
    private static final long INTERACTION_GRACE_PERIOD = 5 * 60 * 1000; // 互動後5分鐘寬限期
    
    private BukkitRunnable checkTask;
    
    public AutoChickenOptimizer(Plugin plugin, ChickenManager chickenManager) {
        this.plugin = plugin;
        this.chickenManager = chickenManager;
    }
    
    /**
     * 啟動自動優化系統
     */
    public void start() {
        if (!autoOptimizeEnabled) {
            return;
        }
        
        // 初始掃描所有已加載的雞
        scanAllLoadedChickens();
        
        // 延遲 2 秒後執行初始密集檢測
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            performInitialDensityCheck();
        }, 40L); // 40 ticks = 2 秒
        
        // 啟動定時檢查任務
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAndOptimizeChickens();
            }
        };
        
        // 延遲 20 ticks（1秒）後開始，每 checkInterval ticks 執行一次
        checkTask.runTaskTimer(plugin, 20L, checkInterval);
        
        plugin.getLogger().info("自動雞優化系統已啟動（閾值: " + threshold + " 隻）");
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
     * 初始掃描所有已加載的雞
     */
    private void scanAllLoadedChickens() {
        int count = 0;
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Chicken) {
                        Chicken chicken = (Chicken) entity;
                        String chunkKey = getChunkKey(chicken.getLocation().getChunk());
                        UUID chickenUUID = chicken.getUniqueId();
                        
                        chunkChickens.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(chickenUUID);
                        
                        // 記錄成年雞的時間戳（小雞不記錄）
                        if (chicken.isAdult()) {
                            chickenAdultTime.putIfAbsent(chickenUUID, System.currentTimeMillis());
                        }
                        count++;
                    }
                }
            }
        }
        plugin.getLogger().info("初始掃描完成，找到 " + count + " 隻雞");
    }
    
    /**
     * 執行初始密集檢測
     */
    private void performInitialDensityCheck() {
        plugin.getLogger().info("開始執行初始雞密集檢測...");
        int totalChecked = 0;
        
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Chicken) {
                        checkChickenDensityImmediate((Chicken) entity);
                        totalChecked++;
                    }
                }
            }
        }
        
        plugin.getLogger().info("初始雞密集檢測完成，檢查了 " + totalChecked + " 隻雞");
    }
    
    /**
     * 添加雞到追蹤系統
     */
    public void addChicken(Chicken chicken) {
        String chunkKey = getChunkKey(chicken.getLocation().getChunk());
        UUID chickenUUID = chicken.getUniqueId();
        
        chunkChickens.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(chickenUUID);
        
        // 記錄成年雞的時間戳（小雞不記錄）
        if (chicken.isAdult()) {
            chickenAdultTime.putIfAbsent(chickenUUID, System.currentTimeMillis());
        }
        
        // 立即檢查該雞位置的密集度
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            checkChickenDensityImmediate(chicken);
        });
    }
    
    /**
     * 移除雞從追蹤系統
     */
    public void removeChicken(Chicken chicken) {
        String chunkKey = getChunkKey(chicken.getLocation().getChunk());
        UUID chickenUUID = chicken.getUniqueId();
        
        Set<UUID> chickens = chunkChickens.get(chunkKey);
        if (chickens != null) {
            chickens.remove(chickenUUID);
            
            // 如果該區塊沒有雞了，移除記錄
            if (chickens.isEmpty()) {
                chunkChickens.remove(chunkKey);
            }
        }
        
        // 清理所有相關追蹤數據
        permanentlyOptimized.remove(chickenUUID);
        chickenAdultTime.remove(chickenUUID);
        lastPlayerInteraction.remove(chickenUUID);
        forceLocked.remove(chickenUUID);
    }
    
    /**
     * 檢查並優化所有區塊的雞
     */
    private void checkAndOptimizeChickens() {
        int optimizedCount = 0;
        
        // 創建副本以避免併發修改
        Map<String, Set<UUID>> chunksCopy = new HashMap<>(chunkChickens);
        
        for (Map.Entry<String, Set<UUID>> entry : chunksCopy.entrySet()) {
            String chunkKey = entry.getKey();
            Set<UUID> chickenUUIDs = new HashSet<>(entry.getValue()); // 創建副本
            
            // 清理已不存在的雞
            chickenUUIDs.removeIf(uuid -> !isChickenExists(uuid));
            
            // 檢查密集度
            checkChickenDensity(chickenUUIDs);
            
            // 檢查雞數量
            if (chickenUUIDs.size() >= threshold) {
                int count = optimizeChunkChickens(chunkKey, chickenUUIDs);
                optimizedCount += count;
            }
        }
        
        if (optimizedCount > 0) {
            plugin.getLogger().info("自動雞優化系統: 優化了 " + optimizedCount + " 隻雞");
        }
    }
    
    /**
     * 立即檢查單個雞的密集度
     */
    private void checkChickenDensityImmediate(Chicken chicken) {
        if (chicken == null || !chicken.isValid()) {
            return;
        }
        
        UUID uuid = chicken.getUniqueId();
        
        // 跳過已經強制鎖定的
        if (forceLocked.contains(uuid)) {
            return;
        }
        
        // 計算 1 格內的雞數量
        Location loc = chicken.getLocation();
        int nearbyCount = 0;
        
        for (Entity entity : chicken.getNearbyEntities(DENSITY_RADIUS, DENSITY_RADIUS, DENSITY_RADIUS)) {
            if (entity instanceof Chicken) {
                nearbyCount++;
            }
        }
        
        // 如果密集度超過閾值，強制鎖定
        if (nearbyCount >= densityThreshold) {
            forceLockChickens(loc, nearbyCount + 1); // +1 包含自己
        }
    }
    
    /**
     * 檢查雞密集度 - 批量檢查
     */
    private void checkChickenDensity(Set<UUID> chickenUUIDs) {
        for (UUID uuid : chickenUUIDs) {
            // 跳過已經強制鎖定的
            if (forceLocked.contains(uuid)) {
                continue;
            }
            
            // 查找雞實體
            Chicken chicken = findChickenByUUID(uuid);
            if (chicken == null) {
                continue;
            }
            
            // 計算 1 格內的雞數量
            Location loc = chicken.getLocation();
            int nearbyCount = 0;
            
            for (Entity entity : chicken.getNearbyEntities(DENSITY_RADIUS, DENSITY_RADIUS, DENSITY_RADIUS)) {
                if (entity instanceof Chicken) {
                    nearbyCount++;
                }
            }
            
            // 如果密集度超過閾值，強制鎖定
            if (nearbyCount >= densityThreshold) {
                forceLockChickens(loc, nearbyCount + 1); // +1 包含自己
            }
        }
    }
    
    /**
     * 強制鎖定指定位置附近的所有雞
     */
    private void forceLockChickens(Location location, int count) {
        int lockedCount = 0;
        
        // 找出附近所有雞
        for (Entity entity : location.getWorld().getNearbyEntities(location, DENSITY_RADIUS, DENSITY_RADIUS, DENSITY_RADIUS)) {
            if (entity instanceof Chicken) {
                Chicken chicken = (Chicken) entity;
                UUID uuid = chicken.getUniqueId();
                
                // 如果尚未強制鎖定，進行鎖定
                if (!forceLocked.contains(uuid)) {
                    // 優化雞
                    optimizeChickenPermanently(chicken);
                    
                    // 標記為強制鎖定
                    forceLocked.add(uuid);
                    permanentlyOptimized.add(uuid);
                    lockedCount++;
                }
            }
        }
        
        if (lockedCount > 0) {
            plugin.getLogger().warning(String.format(
                "檢測到密集雞群！位置: %s (%.1f, %.1f, %.1f)，已強制鎖定 %d 隻雞",
                location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                lockedCount
            ));
            
            // 發送 Discord 通知
            sendDiscordAlert(location, count);
        }
    }
    
    /**
     * 發送 Discord 警告
     */
    private void sendDiscordAlert(Location location, int chickenCount) {
        if (discordWebhook != null && discordWebhook.isConfigured()) {
            // 這裡可以添加雞專用的 Discord 通知方法
            discordWebhook.sendVillagerDensityAlert(location, chickenCount);
        }
    }
    
    /**
     * 優化區塊中的所有雞
     */
    private int optimizeChunkChickens(String chunkKey, Set<UUID> chickenUUIDs) {
        int count = 0;
        long currentTime = System.currentTimeMillis();
        
        for (UUID uuid : chickenUUIDs) {
            // 跳過已經永久優化的雞
            if (permanentlyOptimized.contains(uuid)) {
                continue;
            }
            
            // 查找雞實體
            for (World world : plugin.getServer().getWorlds()) {
                Entity entity = getEntityByUUID(world, uuid);
                if (entity instanceof Chicken) {
                    Chicken chicken = (Chicken) entity;
                    
                    // 檢查是否可以優化
                    if (canOptimizeChicken(chicken, currentTime)) {
                        // 優化雞
                        optimizeChickenPermanently(chicken);
                        permanentlyOptimized.add(uuid);
                        count++;
                    }
                    break;
                }
            }
        }
        
        if (count > 0) {
            plugin.getLogger().info("區塊 " + chunkKey + " 的雞數量超過 " + threshold + 
                                  " 隻，已自動優化 " + count + " 隻雞（永久）");
        }
        
        return count;
    }
    
    /**
     * 檢查雞是否可以被優化
     */
    private boolean canOptimizeChicken(Chicken chicken, long currentTime) {
        UUID uuid = chicken.getUniqueId();
        
        // 1. 小雞不優化
        if (!chicken.isAdult()) {
            return false;
        }
        
        // 2. 檢查成年時間是否超過20秒
        Long adultTime = chickenAdultTime.get(uuid);
        if (adultTime != null) {
            if (currentTime - adultTime < ADULT_GRACE_PERIOD) {
                return false; // 成年不到20秒，不優化
            }
        } else {
            // 如果沒有記錄，現在記錄並給予寬限期
            chickenAdultTime.put(uuid, currentTime);
            return false;
        }
        
        // 3. 檢查最後互動時間（5分鐘內有互動則不優化）
        Long lastInteraction = lastPlayerInteraction.get(uuid);
        if (lastInteraction != null) {
            if (currentTime - lastInteraction < INTERACTION_GRACE_PERIOD) {
                return false; // 5分鐘內有互動，不優化
            }
        }
        
        // 通過所有檢查，可以優化
        return true;
    }
    
    /**
     * 永久優化雞 - 只保留成長和下蛋功能
     */
    private void optimizeChickenPermanently(Chicken chicken) {
        // 關閉大部分 AI，但保留成長和下蛋
        chicken.setAware(false);
        chicken.setAI(false);
        chicken.setCollidable(true);
        
        // 雞的特殊處理：保持成長能力
        if (!chicken.isAdult()) {
            chicken.setAgeLock(false); // 確保不鎖定年齡，允許成長
        }
        
        // 下蛋功能會自動保留，不需要特殊設置
    }
    
    /**
     * 獲取區塊鍵值
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    
    /**
     * 檢查雞是否存在
     */
    private boolean isChickenExists(UUID uuid) {
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
     * 通過 UUID 查找雞實體
     */
    private Chicken findChickenByUUID(UUID uuid) {
        for (World world : plugin.getServer().getWorlds()) {
            Entity entity = getEntityByUUID(world, uuid);
            if (entity instanceof Chicken) {
                return (Chicken) entity;
            }
        }
        return null;
    }
    
    /**
     * 玩家與雞互動時調用
     */
    public void onPlayerInteractChicken(Chicken chicken) {
        UUID uuid = chicken.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // 記錄互動時間
        lastPlayerInteraction.put(uuid, currentTime);
    }
    
    /**
     * 解除雞的永久優化
     */
    public boolean unlockChicken(Chicken chicken) {
        UUID uuid = chicken.getUniqueId();
        
        if (!permanentlyOptimized.contains(uuid)) {
            return false; // 不是永久優化的雞
        }
        
        // 恢復雞功能
        chicken.setAware(true);
        chicken.setAI(true);
        chicken.setCollidable(true);
        
        // 從永久優化列表移除
        permanentlyOptimized.remove(uuid);
        
        // 從強制鎖定列表移除（如果有的話）
        boolean wasForceLocked = forceLocked.remove(uuid);
        
        if (wasForceLocked) {
            plugin.getLogger().info("管理員解除了雞 " + uuid + " 的強制鎖定");
        } else {
            plugin.getLogger().info("管理員解除了雞 " + uuid + " 的永久優化");
        }
        return true;
    }
    
    /**
     * 設置 Discord Webhook
     */
    public void setDiscordWebhook(DiscordWebhook webhook) {
        this.discordWebhook = webhook;
    }
    
    /**
     * 設置密集檢測閾值
     */
    public void setDensityThreshold(int threshold) {
        this.densityThreshold = threshold;
    }
    
    /**
     * 設置雞數量閾值
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
        stats.put("totalChunks", chunkChickens.size());
        stats.put("totalChickens", chunkChickens.values().stream().mapToInt(Set::size).sum());
        stats.put("permanentlyOptimized", permanentlyOptimized.size());
        stats.put("threshold", threshold);
        stats.put("enabled", autoOptimizeEnabled);
        return stats;
    }
    
    /**
     * 清除所有追蹤數據
     */
    public void clear() {
        chunkChickens.clear();
        permanentlyOptimized.clear();
        chickenAdultTime.clear();
        lastPlayerInteraction.clear();
        forceLocked.clear();
    }
}
