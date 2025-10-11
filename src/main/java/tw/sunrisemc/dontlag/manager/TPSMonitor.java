package tw.sunrisemc.dontlag.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TPS 監控系統
 * 監控伺服器 TPS，當 TPS 過低時自動停止自動優化系統
 */
public class TPSMonitor {
    
    private final Plugin plugin;
    private final AutoVillagerOptimizer autoVillagerOptimizer;
    
    private final Queue<Double> tpsHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 10; // 保留最近 10 次的 TPS 記錄
    
    private double tpsThreshold = 15.0; // TPS 閾值
    private boolean autoStopEnabled = true;
    private boolean hasStoppedDueToLag = false;
    
    private BukkitRunnable monitorTask;
    private long lastTickTime = System.currentTimeMillis();
    
    public TPSMonitor(Plugin plugin, AutoVillagerOptimizer autoVillagerOptimizer) {
        this.plugin = plugin;
        this.autoVillagerOptimizer = autoVillagerOptimizer;
    }
    
    /**
     * 啟動 TPS 監控
     */
    public void start() {
        if (!autoStopEnabled) {
            return;
        }
        
        monitorTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTPS();
            }
        };
        
        // 每秒檢查一次 TPS
        monitorTask.runTaskTimer(plugin, 20L, 20L);
        
        plugin.getLogger().info("TPS 監控系統已啟動（閾值: " + tpsThreshold + " TPS）");
    }
    
    /**
     * 停止 TPS 監控
     */
    public void stop() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }
    
    /**
     * 檢查 TPS
     */
    private void checkTPS() {
        double currentTPS = getTPS();
        
        // 添加到歷史記錄
        tpsHistory.offer(currentTPS);
        if (tpsHistory.size() > HISTORY_SIZE) {
            tpsHistory.poll();
        }
        
        // 計算平均 TPS
        double averageTPS = calculateAverageTPS();
        
        // 檢查是否需要停止自動優化
        if (averageTPS < tpsThreshold && !hasStoppedDueToLag) {
            stopAutoOptimizeDueToLag(averageTPS);
        }
    }
    
    /**
     * 獲取當前 TPS
     */
    private double getTPS() {
        try {
            // 嘗試使用反射獲取 TPS（Paper 專屬）
            Object server = Bukkit.getServer();
            java.lang.reflect.Method getTpsMethod = server.getClass().getMethod("getTPS");
            double[] tpsArray = (double[]) getTpsMethod.invoke(server);
            return tpsArray[0]; // 返回 1 分鐘平均 TPS
        } catch (Exception e) {
            // 如果不支援反射，使用簡單的 tick 計數估算
            // 這個方法不太精確，但在 Spigot 上可用
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastTickTime;
            lastTickTime = currentTime;
            
            if (timeDiff == 0 || timeDiff > 100) {
                return 20.0; // 假設正常
            }
            
            // 計算 TPS：1000ms / timeDiff = 每秒 ticks
            double tps = 1000.0 / timeDiff;
            return Math.min(20.0, tps);
        }
    }
    
    /**
     * 計算平均 TPS
     */
    private double calculateAverageTPS() {
        if (tpsHistory.isEmpty()) {
            return 20.0;
        }
        
        double sum = 0;
        for (double tps : tpsHistory) {
            sum += tps;
        }
        
        return sum / tpsHistory.size();
    }
    
    /**
     * 因為卡頓停止自動優化
     */
    private void stopAutoOptimizeDueToLag(double averageTPS) {
        hasStoppedDueToLag = true;
        
        // 停止自動優化系統
        autoVillagerOptimizer.stop();
        
        // 廣播訊息到所有玩家
        String message = ChatColor.RED + "" + ChatColor.BOLD + "[DontLag] " + 
                        ChatColor.YELLOW + "伺服器卡頓（TPS: " + String.format("%.1f", averageTPS) + "）" +
                        ChatColor.GRAY + " - 已停止村民自動優化計算";
        
        Bukkit.getServer().broadcastMessage(message);
        
        // 記錄到日誌
        plugin.getLogger().warning("伺服器 TPS 過低（" + String.format("%.1f", averageTPS) + 
                                  "），已自動停止村民優化計算");
        
        // 提示管理員
        Bukkit.getServer().broadcastMessage(
            ChatColor.GRAY + "[DontLag] 管理員可使用 " + 
            ChatColor.YELLOW + "/delag reload " + 
            ChatColor.GRAY + "重新啟動自動優化"
        );
    }
    
    /**
     * 重置停止狀態
     */
    public void reset() {
        hasStoppedDueToLag = false;
        tpsHistory.clear();
    }
    
    /**
     * 設置 TPS 閾值
     */
    public void setTpsThreshold(double threshold) {
        this.tpsThreshold = threshold;
    }
    
    /**
     * 設置是否啟用自動停止
     */
    public void setAutoStopEnabled(boolean enabled) {
        this.autoStopEnabled = enabled;
        
        if (enabled && monitorTask == null) {
            start();
        } else if (!enabled && monitorTask != null) {
            stop();
        }
    }
    
    /**
     * 檢查是否已因卡頓停止
     */
    public boolean hasStoppedDueToLag() {
        return hasStoppedDueToLag;
    }
    
    /**
     * 獲取當前平均 TPS
     */
    public double getAverageTPS() {
        return calculateAverageTPS();
    }
}

