package tw.sunrisemc.dontlag.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ToolManager {
    
    private final HashMap<UUID, Boolean> toolUsers = new HashMap<>();
    private final HashMap<UUID, Boolean> villagerToolUsers = new HashMap<>();
    private final HashMap<UUID, Boolean> opToolUsers = new HashMap<>();
    private static final String TOOL_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "AI 控制工具";
    private static final String VILLAGER_TOOL_NAME = ChatColor.AQUA + "" + ChatColor.BOLD + "村民優化工具";
    private static final String OP_TOOL_NAME = ChatColor.RED + "" + ChatColor.BOLD + "OP 管理員棒";
    
    /**
     * 設定玩家是否持有 AI 控制工具
     */
    public void setToolUser(Player player, boolean enabled) {
        if (enabled) {
            toolUsers.put(player.getUniqueId(), true);
            giveAITool(player);
        } else {
            toolUsers.remove(player.getUniqueId());
        }
    }
    
    /**
     * 檢查玩家是否為工具使用者
     */
    public boolean isToolUser(UUID uuid) {
        return toolUsers.getOrDefault(uuid, false);
    }
    
    /**
     * 設定玩家是否持有村民優化工具
     */
    public void setVillagerToolUser(Player player, boolean enabled) {
        if (enabled) {
            villagerToolUsers.put(player.getUniqueId(), true);
            giveVillagerTool(player);
        } else {
            villagerToolUsers.remove(player.getUniqueId());
        }
    }
    
    /**
     * 檢查玩家是否為村民工具使用者
     */
    public boolean isVillagerToolUser(UUID uuid) {
        return villagerToolUsers.getOrDefault(uuid, false);
    }
    
    /**
     * 給予玩家村民優化工具
     */
    private void giveVillagerTool(Player player) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(VILLAGER_TOOL_NAME);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "右鍵點擊村民來優化其功能");
            lore.add(ChatColor.GRAY + "只保留交易和補貨功能");
            lore.add(ChatColor.GRAY + "移除代理(gossip)、工作站尋找等");
            lore.add(ChatColor.YELLOW + "左鍵點擊查看使用說明");
            meta.setLore(lore);
            
            stick.setItemMeta(meta);
        }
        
        player.getInventory().addItem(stick);
    }
    
    /**
     * 給予玩家 AI 控制工具
     */
    private void giveAITool(Player player) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(TOOL_NAME);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "右鍵點擊生物來切換 AI 狀態");
            lore.add(ChatColor.GRAY + "關閉 AI 可減輕伺服器負擔");
            lore.add(ChatColor.YELLOW + "左鍵點擊查看使用說明");
            meta.setLore(lore);
            
            stick.setItemMeta(meta);
        }
        
        player.getInventory().addItem(stick);
    }
    
    /**
     * 檢查物品是否為 AI 控制工具
     */
    public boolean isAITool(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(TOOL_NAME);
    }
    
    /**
     * 檢查物品是否為村民優化工具
     */
    public boolean isVillagerTool(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(VILLAGER_TOOL_NAME);
    }
    
    /**
     * 設定玩家是否持有 OP 管理員棒
     */
    public void setOpToolUser(Player player, boolean enabled) {
        if (enabled) {
            opToolUsers.put(player.getUniqueId(), true);
            giveOpTool(player);
        } else {
            opToolUsers.remove(player.getUniqueId());
        }
    }
    
    /**
     * 檢查玩家是否為 OP 工具使用者
     */
    public boolean isOpToolUser(UUID uuid) {
        return opToolUsers.getOrDefault(uuid, false);
    }
    
    /**
     * 給予玩家 OP 管理員棒
     */
    private void giveOpTool(Player player) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(OP_TOOL_NAME);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "右鍵點擊村民來解除永久優化");
            lore.add(ChatColor.GRAY + "用於解除自動優化系統鎖定的村民");
            lore.add(ChatColor.RED + "管理員專用工具");
            lore.add(ChatColor.YELLOW + "左鍵點擊查看使用說明");
            meta.setLore(lore);
            
            stick.setItemMeta(meta);
        }
        
        player.getInventory().addItem(stick);
    }
    
    /**
     * 檢查物品是否為 OP 管理員棒
     */
    public boolean isOpTool(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(OP_TOOL_NAME);
    }
}


