package rpgmain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ProgressBar {
    private int task;
    private Player player;
    private RPGMain instance;

    public ProgressBar(Player player, RPGMain instance){
        this.player = player;
        this.instance = instance;
    }

    public boolean showBar(){
       task = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
            int s = 0;
            @Override
            public void run() {
                s++;

                if(player == null){
                    Bukkit.getScheduler().cancelTask(task);
                }

                if(s < 101){
                    player.sendTitle(s + "%", getProgressBar(s, 100, 50, "|", "&a", "&c"), 0, 10, 0);
                } else if(s == 101){
                    Bukkit.getScheduler().cancelTask(task);
                }
            }
        }, 0, 1);
       return false;
    }

    public String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor){

        float percent = (float) current / max;

        int progressBars = (int) ((int) totalBars * percent);

        int leftOver = (totalBars - progressBars);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.translateAlternateColorCodes('&', completedColor));
        for (int i = 0; i < progressBars; i++) {
            sb.append(symbol);
        }
        sb.append(ChatColor.translateAlternateColorCodes('&', notCompletedColor));
        for (int i = 0; i < leftOver; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }
}
