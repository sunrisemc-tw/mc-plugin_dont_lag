package tw.sunrisemc.dontlag.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import tw.sunrisemc.dontlag.DontLag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityInteractListener implements Listener {
    
    private final DontLag plugin;
    private final Map<UUID, Long> lastInteraction = new HashMap<>();
    private static final long COOLDOWN = 100; // 100ms 冷卻時間
    
    public EntityInteractListener(DontLag plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 處理 OP 管理員棒
        if (plugin.getToolManager().isOpToolUser(player.getUniqueId()) && 
            plugin.getToolManager().isOpTool(item)) {
            handleOpTool(player, entity, event);
            return;
        }
        
        // 處理村民優化工具
        if (plugin.getToolManager().isVillagerToolUser(player.getUniqueId()) && 
            plugin.getToolManager().isVillagerTool(item)) {
            handleVillagerOptimization(player, entity, event);
            return;
        }
        
        // 處理 AI 控制工具
        if (plugin.getToolManager().isToolUser(player.getUniqueId()) && 
            plugin.getToolManager().isAITool(item)) {
            handleAIControl(player, entity, event);
            return;
        }
    }
    
    /**
     * 處理 AI 控制
     */
    private void handleAIControl(Player player, Entity entity, PlayerInteractEntityEvent event) {
        // 檢查權限
        if (!player.hasPermission("dontlag.use")) {
            player.sendMessage(ChatColor.RED + "你沒有權限使用此功能！");
            return;
        }
        
        // 防止重複觸發
        UUID entityUUID = entity.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastInteraction.get(entityUUID);
        
        if (lastTime != null && (currentTime - lastTime) < COOLDOWN) {
            return;
        }
        
        lastInteraction.put(entityUUID, currentTime);
        
        // 取消原本的互動
        event.setCancelled(true);
        
        // 切換 AI 狀態
        boolean aiDisabled = plugin.getAIManager().toggleAI(entity);
        
        String entityName = getEntityDisplayName(entity);
        
        if (aiDisabled) {
            player.sendMessage(ChatColor.GREEN + "已關閉 " + ChatColor.YELLOW + entityName + 
                             ChatColor.GREEN + " 的 AI（保留基本功能）");
            player.sendMessage(ChatColor.GRAY + "該生物現在只能執行基本互動，不會移動或尋路");
        } else {
            player.sendMessage(ChatColor.YELLOW + "已恢復 " + ChatColor.YELLOW + entityName + 
                             ChatColor.YELLOW + " 的 AI");
            player.sendMessage(ChatColor.GRAY + "該生物現在恢復正常行為");
        }
    }
    
    /**
     * 處理 OP 管理員棒
     */
    private void handleOpTool(Player player, Entity entity, PlayerInteractEntityEvent event) {
        // 檢查權限
        if (!player.hasPermission("dontlag.admin")) {
            player.sendMessage(ChatColor.RED + "你沒有權限使用此功能！");
            return;
        }
        
        // 檢查是否為村民
        if (!(entity instanceof org.bukkit.entity.Villager)) {
            player.sendMessage(ChatColor.RED + "此工具只能用於村民！");
            return;
        }
        
        org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) entity;
        
        // 防止重複觸發
        UUID entityUUID = entity.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastInteraction.get(entityUUID);
        
        if (lastTime != null && (currentTime - lastTime) < COOLDOWN) {
            return;
        }
        
        lastInteraction.put(entityUUID, currentTime);
        
        // 取消原本的互動
        event.setCancelled(true);
        
        // 檢查是否為永久優化的村民
        if (!plugin.getAutoVillagerOptimizer().isPermanentlyOptimized(entityUUID)) {
            player.sendMessage(ChatColor.YELLOW + "此村民未被自動優化系統鎖定");
            player.sendMessage(ChatColor.GRAY + "只能解除自動優化的村民");
            return;
        }
        
        // 解除永久優化
        boolean unlocked = plugin.getAutoVillagerOptimizer().unlockVillager(villager);
        
        if (unlocked) {
            String entityName = getEntityDisplayName(entity);
            player.sendMessage(ChatColor.GREEN + "已解除村民 " + ChatColor.YELLOW + entityName + 
                             ChatColor.GREEN + " 的永久優化");
            player.sendMessage(ChatColor.GRAY + "該村民現在恢復正常行為");
            player.sendMessage(ChatColor.RED + "注意：如果該區域村民仍超過閾值，可能會再次被自動優化");
        } else {
            player.sendMessage(ChatColor.RED + "解除優化失敗！");
        }
    }
    
    /**
     * 處理村民優化
     */
    private void handleVillagerOptimization(Player player, Entity entity, PlayerInteractEntityEvent event) {
        // 檢查權限
        if (!player.hasPermission("dontlag.use")) {
            player.sendMessage(ChatColor.RED + "你沒有權限使用此功能！");
            return;
        }
        
        // 檢查是否為村民
        if (!(entity instanceof org.bukkit.entity.Villager)) {
            player.sendMessage(ChatColor.RED + "此工具只能用於村民！");
            return;
        }
        
        // 防止重複觸發
        UUID entityUUID = entity.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastInteraction.get(entityUUID);
        
        if (lastTime != null && (currentTime - lastTime) < COOLDOWN) {
            return;
        }
        
        lastInteraction.put(entityUUID, currentTime);
        
        // 取消原本的互動
        event.setCancelled(true);
        
        // 切換優化狀態
        boolean optimized = plugin.getVillagerManager().toggleOptimization(entity);
        
        String entityName = getEntityDisplayName(entity);
        
        if (optimized) {
            player.sendMessage(ChatColor.GREEN + "已優化村民 " + ChatColor.YELLOW + entityName);
            player.sendMessage(ChatColor.GRAY + "該村民現在只保留交易和補貨功能");
            player.sendMessage(ChatColor.GRAY + "已移除: 代理(gossip)、尋路、工作站尋找等");
        } else {
            player.sendMessage(ChatColor.YELLOW + "已恢復村民 " + ChatColor.YELLOW + entityName + 
                             ChatColor.YELLOW + " 的所有功能");
            player.sendMessage(ChatColor.GRAY + "該村民現在恢復正常行為");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 只處理左鍵點擊空氣或方塊
        if (!event.getAction().toString().contains("LEFT_CLICK")) {
            return;
        }
        
        // 檢查權限
        if (!player.hasPermission("dontlag.use")) {
            return;
        }
        
        // 處理 AI 控制工具
        if (plugin.getToolManager().isToolUser(player.getUniqueId()) && 
            plugin.getToolManager().isAITool(item)) {
            
            event.setCancelled(true);
            
            player.sendMessage(ChatColor.GOLD + "========== AI 控制工具 ==========");
            player.sendMessage(ChatColor.YELLOW + "右鍵點擊生物: " + ChatColor.WHITE + "切換 AI 狀態");
            player.sendMessage(ChatColor.YELLOW + "左鍵點擊: " + ChatColor.WHITE + "顯示此說明");
            player.sendMessage(ChatColor.GRAY + "目前有 " + ChatColor.WHITE + plugin.getAIManager().getDisabledCount() + 
                             ChatColor.GRAY + " 個生物的 AI 被禁用");
            player.sendMessage(ChatColor.GOLD + "================================");
            return;
        }
        
        // 處理村民優化工具
        if (plugin.getToolManager().isVillagerToolUser(player.getUniqueId()) && 
            plugin.getToolManager().isVillagerTool(item)) {
            
            event.setCancelled(true);
            
            player.sendMessage(ChatColor.GOLD + "========== 村民優化工具 ==========");
            player.sendMessage(ChatColor.YELLOW + "右鍵點擊村民: " + ChatColor.WHITE + "優化村民功能");
            player.sendMessage(ChatColor.YELLOW + "左鍵點擊: " + ChatColor.WHITE + "顯示此說明");
            player.sendMessage(ChatColor.GRAY + "優化內容: " + ChatColor.WHITE + "只保留交易和補貨");
            player.sendMessage(ChatColor.GRAY + "移除內容: " + ChatColor.WHITE + "代理、尋路、工作站尋找");
            player.sendMessage(ChatColor.GRAY + "目前有 " + ChatColor.WHITE + plugin.getVillagerManager().getOptimizedCount() + 
                             ChatColor.GRAY + " 個村民被優化");
            player.sendMessage(ChatColor.GOLD + "=================================");
            return;
        }
        
        // 處理 OP 管理員棒
        if (plugin.getToolManager().isOpToolUser(player.getUniqueId()) && 
            plugin.getToolManager().isOpTool(item)) {
            
            event.setCancelled(true);
            
            player.sendMessage(ChatColor.GOLD + "========== OP 管理員棒 ==========");
            player.sendMessage(ChatColor.YELLOW + "右鍵點擊村民: " + ChatColor.WHITE + "解除永久優化");
            player.sendMessage(ChatColor.YELLOW + "左鍵點擊: " + ChatColor.WHITE + "顯示此說明");
            player.sendMessage(ChatColor.RED + "專用於解除自動優化系統鎖定的村民");
            player.sendMessage(ChatColor.GRAY + "目前有 " + ChatColor.WHITE + 
                             plugin.getAutoVillagerOptimizer().getStats().get("permanentlyOptimized") + 
                             ChatColor.GRAY + " 個村民被永久優化");
            player.sendMessage(ChatColor.GOLD + "===============================");
        }
    }
    
    /**
     * 獲取生物的顯示名稱
     */
    private String getEntityDisplayName(Entity entity) {
        if (entity.getCustomName() != null) {
            return entity.getCustomName();
        }
        
        // 簡單的中文名稱對照
        EntityType type = entity.getType();
        switch (type) {
            case VILLAGER: return "村民";
            case ZOMBIE: return "殭屍";
            case SKELETON: return "骷髏";
            case CREEPER: return "苦力怕";
            case COW: return "牛";
            case PIG: return "豬";
            case SHEEP: return "羊";
            case CHICKEN: return "雞";
            case HORSE: return "馬";
            case WOLF: return "狼";
            case CAT: return "貓";
            case IRON_GOLEM: return "鐵魔像";
            case ENDERMAN: return "終界使者";
            default: return type.name();
        }
    }
}


