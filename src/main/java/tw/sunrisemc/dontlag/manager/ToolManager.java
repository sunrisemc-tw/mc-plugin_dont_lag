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
    private static final String TOOL_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "AI 控制工具";
    
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
            lore.add(ChatColor.YELLOW + "左鍵點擊查看生物 AI 狀態");
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
}


