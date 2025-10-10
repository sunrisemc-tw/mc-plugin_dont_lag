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
            case "set":
                return handleSet(sender);
                
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
     * 處理 /delag set 指令
     */
    private boolean handleSet(CommandSender sender) {
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
     * 處理 /delag info 指令
     */
    private boolean handleInfo(CommandSender sender) {
        if (!sender.hasPermission("dontlag.use")) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令！");
            return true;
        }
        
        int disabledCount = plugin.getAIManager().getDisabledCount();
        
        sender.sendMessage(ChatColor.GOLD + "========== DontLag 資訊 ==========");
        sender.sendMessage(ChatColor.YELLOW + "版本: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "已關閉 AI 的生物數量: " + ChatColor.WHITE + disabledCount);
        sender.sendMessage(ChatColor.GOLD + "================================");
        
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
        
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "配置已重新載入！");
        
        return true;
    }
    
    /**
     * 發送幫助訊息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== DontLag 指令幫助 ==========");
        sender.sendMessage(ChatColor.YELLOW + "/delag set" + ChatColor.WHITE + " - 切換 AI 控制工具模式");
        sender.sendMessage(ChatColor.YELLOW + "/delag info" + ChatColor.WHITE + " - 查看插件資訊");
        sender.sendMessage(ChatColor.YELLOW + "/delag reload" + ChatColor.WHITE + " - 重新載入配置");
        sender.sendMessage(ChatColor.GOLD + "=====================================");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("set", "info", "reload");
            String input = args[0].toLowerCase();
            
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        }
        
        return completions;
    }
}


