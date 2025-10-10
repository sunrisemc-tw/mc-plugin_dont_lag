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
        
        // 檢查玩家是否為工具使用者
        if (!plugin.getToolManager().isToolUser(player.getUniqueId())) {
            return;
        }
        
        // 檢查是否持有 AI 控制工具
        if (!plugin.getToolManager().isAITool(item)) {
            return;
        }
        
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 只處理左鍵點擊空氣或方塊
        if (event.getAction().toString().contains("LEFT_CLICK")) {
            // 檢查玩家是否為工具使用者
            if (!plugin.getToolManager().isToolUser(player.getUniqueId())) {
                return;
            }
            
            // 檢查是否持有 AI 控制工具
            if (!plugin.getToolManager().isAITool(item)) {
                return;
            }
            
            // 檢查權限
            if (!player.hasPermission("dontlag.use")) {
                player.sendMessage(ChatColor.RED + "你沒有權限使用此功能！");
                return;
            }
            
            // 取消原本的互動
            event.setCancelled(true);
            
            // 顯示使用說明
            player.sendMessage(ChatColor.GOLD + "========== AI 控制工具 ==========");
            player.sendMessage(ChatColor.YELLOW + "右鍵點擊生物: " + ChatColor.WHITE + "切換 AI 狀態");
            player.sendMessage(ChatColor.YELLOW + "左鍵點擊: " + ChatColor.WHITE + "顯示此說明");
            player.sendMessage(ChatColor.GRAY + "目前有 " + ChatColor.WHITE + plugin.getAIManager().getDisabledCount() + 
                             ChatColor.GRAY + " 個生物的 AI 被禁用");
            player.sendMessage(ChatColor.GOLD + "================================");
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


