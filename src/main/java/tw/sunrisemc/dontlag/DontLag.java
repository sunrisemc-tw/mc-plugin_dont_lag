package tw.sunrisemc.dontlag;

import org.bukkit.plugin.java.JavaPlugin;
import tw.sunrisemc.dontlag.command.DelagCommand;
import tw.sunrisemc.dontlag.listener.EntityInteractListener;
import tw.sunrisemc.dontlag.listener.VillagerTrackingListener;
import tw.sunrisemc.dontlag.manager.AIManager;
import tw.sunrisemc.dontlag.manager.AutoVillagerOptimizer;
import tw.sunrisemc.dontlag.manager.ToolManager;
import tw.sunrisemc.dontlag.manager.VillagerManager;
import tw.sunrisemc.dontlag.util.DiscordWebhook;

public class DontLag extends JavaPlugin {
    
    private static DontLag instance;
    private ToolManager toolManager;
    private AIManager aiManager;
    private VillagerManager villagerManager;
    private AutoVillagerOptimizer autoVillagerOptimizer;
    private DiscordWebhook discordWebhook;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默認配置
        saveDefaultConfig();
        
        // 初始化管理器
        toolManager = new ToolManager();
        aiManager = new AIManager(this);
        villagerManager = new VillagerManager(this);
        autoVillagerOptimizer = new AutoVillagerOptimizer(this, villagerManager);
        discordWebhook = new DiscordWebhook(this);
        
        // 載入配置並啟動自動優化
        loadAutoOptimizerConfig();
        loadWebhookConfig();
        
        // 註冊指令
        getCommand("delag").setExecutor(new DelagCommand(this));
        
        // 註冊事件監聽器
        getServer().getPluginManager().registerEvents(new EntityInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerTrackingListener(this), this);
        
        getLogger().info("DontLag 插件已啟用！");
    }
    
    @Override
    public void onDisable() {
        // 停止自動優化系統
        if (autoVillagerOptimizer != null) {
            autoVillagerOptimizer.stop();
        }
        
        // 恢復所有生物的 AI（手動優化的才會恢復）
        if (aiManager != null) {
            aiManager.restoreAll();
        }
        
        // 恢復所有村民（手動優化的才會恢復，自動優化的不會恢復）
        if (villagerManager != null) {
            villagerManager.restoreAll();
        }
        
        getLogger().info("DontLag 插件已關閉！");
    }
    
    public static DontLag getInstance() {
        return instance;
    }
    
    public ToolManager getToolManager() {
        return toolManager;
    }
    
    public AIManager getAIManager() {
        return aiManager;
    }
    
    public VillagerManager getVillagerManager() {
        return villagerManager;
    }
    
    public AutoVillagerOptimizer getAutoVillagerOptimizer() {
        return autoVillagerOptimizer;
    }
    
    /**
     * 載入自動優化配置
     */
    private void loadAutoOptimizerConfig() {
        boolean enabled = getConfig().getBoolean("auto-optimize.enabled", true);
        int threshold = getConfig().getInt("auto-optimize.threshold", 5);
        int interval = getConfig().getInt("auto-optimize.check-interval", 30);
        
        autoVillagerOptimizer.setAutoOptimizeEnabled(enabled);
        autoVillagerOptimizer.setThreshold(threshold);
        autoVillagerOptimizer.setCheckInterval(interval);
        
        if (enabled) {
            autoVillagerOptimizer.start();
        }
    }
    
    /**
     * 載入 Webhook 配置
     */
    public void loadWebhookConfig() {
        String webhookUrl = getConfig().getString("discord.webhook-url", "");
        discordWebhook.setWebhookUrl(webhookUrl);
        autoVillagerOptimizer.setDiscordWebhook(discordWebhook);
        
        if (discordWebhook.isConfigured()) {
            getLogger().info("Discord Webhook 已配置");
        }
    }
    
    /**
     * 重新載入配置
     */
    public void reloadPluginConfig() {
        reloadConfig();
        loadAutoOptimizerConfig();
        loadWebhookConfig();
    }
}


