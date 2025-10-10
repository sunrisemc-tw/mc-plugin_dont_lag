package tw.sunrisemc.dontlag.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tw.sunrisemc.dontlag.DontLag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DelagCommand implements CommandExecutor, TabCompleter {
    
    private final DontLag plugin;
    
    public DelagCommand(DontLag plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "ai":
                return handleAI(sender, args);
                
            case "villager":
                return handleVillager(sender, args);
                
            case "info":
                return handleInfo(sender);
                
            case "reload":
                return handleReload(sender);
                
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * 處理 /delag ai 指令
     */
    private boolean handleAI(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /delag ai set");
            return true;
        }
        
        if (args[1].equalsIgnoreCase("set")) {
            return handleAISet(sender);
        }
        
        sender.sendMessage(ChatColor.RED + "用法: /delag ai set");
        return true;
    }
    
    /**
     * 處理 /delag ai set 指令
     */
    private boolean handleAISet(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行！");
            return true;
        }
        
        if (!sender.hasPermission("dontlag.admin")) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令！");
            return true;
        }
        
        Player player = (Player) sender;
        boolean isToolUser = plugin.getToolManager().isToolUser(player.getUniqueId());
        
        if (isToolUser) {
            plugin.getToolManager().setToolUser(player, false);
            player.sendMessage(ChatColor.YELLOW + "已關閉 AI 控制工具模式");
        } else {
            plugin.getToolManager().setToolUser(player, true);
            player.sendMessage(ChatColor.GREEN + "已啟用 AI 控制工具模式！");
            player.sendMessage(ChatColor.GRAY + "使用木棒右鍵點擊生物來切換其 AI 狀態");
        }
        
        return true;
    }
    
    /**
     * 處理 /delag villager 指令
     */
    private boolean handleVillager(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /delag villager set");
            return true;
        }
        
        if (args[1].equalsIgnoreCase("set")) {
            return handleVillagerSet(sender);
        }
        
        sender.sendMessage(ChatColor.RED + "用法: /delag villager set");
        return true;
    }
    
    /**
     * 處理 /delag villager set 指令
     */
    private boolean handleVillagerSet(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行！");
            return true;
        }
        
        if (!sender.hasPermission("dontlag.admin")) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令！");
            return true;
        }
        
        Player player = (Player) sender;
        boolean isVillagerToolUser = plugin.getToolManager().isVillagerToolUser(player.getUniqueId());
        
        if (isVillagerToolUser) {
            plugin.getToolManager().setVillagerToolUser(player, false);
            player.sendMessage(ChatColor.YELLOW + "已關閉村民優化工具模式");
        } else {
            plugin.getToolManager().setVillagerToolUser(player, true);
            player.sendMessage(ChatColor.GREEN + "已啟用村民優化工具模式！");
            player.sendMessage(ChatColor.GRAY + "使用木棒右鍵點擊村民來優化其功能");
            player.sendMessage(ChatColor.GRAY + "優化後村民只保留交易和補貨功能");
        }
        
        return true;
    }
    
    /**
     * 處理 /delag info 指令
     */
    private boolean handleInfo(CommandSender sender) {
        if (!sender.hasPermission("dontlag.use")) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令！");
            return true;
        }
        
        int disabledCount = plugin.getAIManager().getDisabledCount();
        int optimizedVillagers = plugin.getVillagerManager().getOptimizedCount();
        
        // 獲取自動優化統計
        Map<String, Object> autoStats = plugin.getAutoVillagerOptimizer().getStats();
        
        sender.sendMessage(ChatColor.GOLD + "============ DontLag 資訊 ============");
        sender.sendMessage(ChatColor.YELLOW + "版本: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "手動優化:");
        sender.sendMessage(ChatColor.YELLOW + "  已關閉 AI 的生物: " + ChatColor.WHITE + disabledCount);
        sender.sendMessage(ChatColor.YELLOW + "  已優化的村民: " + ChatColor.WHITE + optimizedVillagers);
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "自動村民優化:");
        sender.sendMessage(ChatColor.YELLOW + "  狀態: " + (autoStats.get("enabled").equals(true) ? 
                         ChatColor.GREEN + "啟用" : ChatColor.RED + "停用"));
        sender.sendMessage(ChatColor.YELLOW + "  閾值: " + ChatColor.WHITE + autoStats.get("threshold") + " 隻/區塊");
        sender.sendMessage(ChatColor.YELLOW + "  追蹤區塊數: " + ChatColor.WHITE + autoStats.get("totalChunks"));
        sender.sendMessage(ChatColor.YELLOW + "  追蹤村民數: " + ChatColor.WHITE + autoStats.get("totalVillagers"));
        sender.sendMessage(ChatColor.YELLOW + "  永久優化數: " + ChatColor.WHITE + autoStats.get("permanentlyOptimized"));
        sender.sendMessage(ChatColor.GOLD + "======================================");
        
        return true;
    }
    
    /**
     * 處理 /delag reload 指令
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dontlag.admin")) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令！");
            return true;
        }
        
        plugin.reloadPluginConfig();
        sender.sendMessage(ChatColor.GREEN + "配置已重新載入！");
        sender.sendMessage(ChatColor.YELLOW + "自動優化系統已根據新配置重新啟動");
        
        return true;
    }
    
    /**
     * 發送幫助訊息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== DontLag 指令幫助 ==========");
        sender.sendMessage(ChatColor.YELLOW + "/delag ai set" + ChatColor.WHITE + " - 切換 AI 控制工具模式");
        sender.sendMessage(ChatColor.YELLOW + "/delag villager set" + ChatColor.WHITE + " - 切換村民優化工具模式");
        sender.sendMessage(ChatColor.YELLOW + "/delag info" + ChatColor.WHITE + " - 查看插件資訊和統計");
        sender.sendMessage(ChatColor.YELLOW + "/delag reload" + ChatColor.WHITE + " - 重新載入配置");
        sender.sendMessage(ChatColor.GRAY + "提示: 自動村民優化功能會在區塊村民");
        sender.sendMessage(ChatColor.GRAY + "      超過閾值時自動優化（可在配置中調整）");
        sender.sendMessage(ChatColor.GOLD + "======================================");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("ai", "villager", "info", "reload");
            String input = args[0].toLowerCase();
            
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("ai") || args[0].equalsIgnoreCase("villager")) {
                List<String> subcommands = Arrays.asList("set");
                String input = args[1].toLowerCase();
                
                for (String subcommand : subcommands) {
                    if (subcommand.startsWith(input)) {
                        completions.add(subcommand);
                    }
                }
            }
        }
        
        return completions;
    }
}


