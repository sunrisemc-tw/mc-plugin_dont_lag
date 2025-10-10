package tw.sunrisemc.dontlag;

import org.bukkit.plugin.java.JavaPlugin;
import tw.sunrisemc.dontlag.command.DelagCommand;
import tw.sunrisemc.dontlag.listener.EntityInteractListener;
import tw.sunrisemc.dontlag.manager.AIManager;
import tw.sunrisemc.dontlag.manager.ToolManager;

public class DontLag extends JavaPlugin {
    
    private static DontLag instance;
    private ToolManager toolManager;
    private AIManager aiManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默認配置
        saveDefaultConfig();
        
        // 初始化管理器
        toolManager = new ToolManager();
        aiManager = new AIManager(this);
        
        // 註冊指令
        getCommand("delag").setExecutor(new DelagCommand(this));
        
        // 註冊事件監聽器
        getServer().getPluginManager().registerEvents(new EntityInteractListener(this), this);
        
        getLogger().info("DontLag 插件已啟用！");
    }
    
    @Override
    public void onDisable() {
        // 恢復所有生物的 AI
        if (aiManager != null) {
            aiManager.restoreAll();
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
}


