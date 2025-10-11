package tw.sunrisemc.dontlag.util;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Discord Webhook 工具類
 * 用於發送通知到 Discord
 */
public class DiscordWebhook {
    
    private final Plugin plugin;
    private String webhookUrl;
    
    public DiscordWebhook(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 設置 Webhook URL
     */
    public void setWebhookUrl(String url) {
        this.webhookUrl = url;
    }
    
    /**
     * 獲取 Webhook URL
     */
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    /**
     * 檢查是否已配置 Webhook
     */
    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isEmpty() && !webhookUrl.equals("none");
    }
    
    /**
     * 發送村民密集警告
     */
    public void sendVillagerDensityAlert(Location location, int villagerCount) {
        if (!isConfigured()) {
            return;
        }
        
        String worldName = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        String content = String.format(
            "⚠️ **村民密集警告**\n\n" +
            "檢測到在 **0.5 格內**有 **%d 隻村民**堆疊在一起！\n\n" +
            "**位置：**\n" +
            "```\n" +
            "世界: %s\n" +
            "座標: X=%d, Y=%d, Z=%d\n" +
            "```\n\n" +
            "這些村民已被**強制鎖定**，需要管理員手動解除。\n" +
            "可能原因：玩家濫用、繁殖機故障、區塊問題。",
            villagerCount, worldName, x, y, z
        );
        
        sendEmbed(
            "村民密集警告",
            content,
            0xFF0000, // 紅色
            String.format("世界: %s | 座標: %d, %d, %d", worldName, x, y, z)
        );
    }
    
    /**
     * 發送嵌入式訊息
     */
    private void sendEmbed(String title, String description, int color, String footer) {
        if (!isConfigured()) {
            return;
        }
        
        // 異步發送，避免阻塞主線程
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "DontLag-Plugin");
                connection.setDoOutput(true);
                
                // 構建 JSON（手動構建以避免依賴額外庫）
                String json = String.format(
                    "{\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d,\"footer\":{\"text\":\"%s\"},\"timestamp\":\"%s\"}]}",
                    escapeJson(title),
                    escapeJson(description),
                    color,
                    escapeJson(footer),
                    Instant.now().toString()
                );
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    plugin.getLogger().info("Discord 通知已發送");
                } else {
                    plugin.getLogger().warning("Discord Webhook 返回錯誤代碼: " + responseCode);
                }
                
                connection.disconnect();
                
            } catch (Exception e) {
                plugin.getLogger().warning("發送 Discord Webhook 失敗: " + e.getMessage());
            }
        });
    }
    
    /**
     * 轉義 JSON 字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

