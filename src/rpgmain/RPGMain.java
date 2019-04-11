package rpgmain;

import inventory.ScrollManager;
import listener.ScrollListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import util.EnchantmentUtil;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class RPGMain extends JavaPlugin implements CommandExecutor {
    public RPGMain instance;
    private HashMap<UUID, String> quest;
    private OnCommand onCommand;
    private ScrollManager scrollManager;
    private EnchantmentUtil enchantmentUtil;
    private ScrollListener scrollListener;

    @Override
    public void onEnable(){
        instance = this;

        /**ItemStack item = new ItemStack(Material.STONE);
        item = NBTEditor.setItemTag(item, 1, "check");
        Object var = NBTEditor.getItemTag(item, "check");
        if((int) var == 1){
            Bukkit.getLogger().severe("!");
        }

        item = NBTEditor.setItemTag(item, (int) var + 1, "check");
        Object var1 = NBTEditor.getItemTag(item, "check");
        if((int) var1 == 2){
            Bukkit.getLogger().severe("!!");
        }*/

        File enchantFile = new File(instance.getDataFolder(), "enchantMenu.yml");
        if(!enchantFile.exists()){
            instance.saveResource("enchantMenu.yml", false);
        }

        File questConfig = new File(instance.getDataFolder(), "questConfig.yml");
        if(!questConfig.exists()){
            instance.saveResource("questConfig.yml", false);
        }

        File configFile = new File(instance.getDataFolder(), "config.yml");
        if(!configFile.exists()){
            instance.saveResource("config.yml", false);
        }

        File messageFile = new File(instance.getDataFolder(), "message.yml");
        if(!messageFile.exists()){
            instance.saveResource("message.yml", false);
        }

        File limitFile = new File(instance.getDataFolder(), "limitEnchant.yml");
        if(!limitFile.exists()){
            instance.saveResource("limitEnchant.yml", false);
        }

        File controlFile = new File(instance.getDataFolder(), "controlItem.yml");
        if(!controlFile.exists()){
            instance.saveResource("controlItem.yml", false);
        }

        enchantmentUtil = new EnchantmentUtil(instance);
        scrollManager = new ScrollManager(instance);
        onCommand = new OnCommand(instance, scrollManager);
        scrollListener = new ScrollListener(instance, scrollManager);

        Bukkit.getPluginManager().registerEvents(scrollListener, instance);

        scrollManager.loadScroll();

        getCommand("퀘스트").setExecutor(this);
        getCommand("tm").setExecutor(this);
        getCommand("스크롤").setExecutor(onCommand);
        getCommand("강화").setExecutor(onCommand);
        getCommand("강화gui").setExecutor(onCommand);
        quest = questLoad();
        if(quest == null){
            quest = new HashMap<>();
        }
    }

    public void yamlReload(){
        enchantmentUtil = new EnchantmentUtil(instance);
    }

    public EnchantmentUtil getUtil(){
        return this.enchantmentUtil;
    }

    public boolean hasOnGoingQuest(Player player){
        if(quest.containsKey(player.getUniqueId())){
            if(!quest.get(player.getUniqueId()).equals("완료")){
                return true;
            }
        }
        return false;
    }

    public boolean hasFinishedQuest(Player player){
        if(quest.containsKey(player.getUniqueId())){
            if(quest.get(player.getUniqueId()).equals("완료")){
                return true;
            }
        }
        return false;
    }

    public void questSave() {
        File questConfig = new File(instance.getDataFolder(), "questConfig.yml");
        YamlConfiguration configYml = YamlConfiguration.loadConfiguration(questConfig);

        configYml.set("quest", null);
        for (UUID name : quest.keySet()) {
            configYml.set("quest." + name, quest.get(name));
        }
        saveConfig();
    }

    public HashMap<UUID, String> questLoad() {
        File questConfig = new File(instance.getDataFolder(), "questConfig.yml");
        YamlConfiguration configYml = YamlConfiguration.loadConfiguration(questConfig);

        HashMap<UUID, String> hashMap = new HashMap<>();
        if (configYml.getConfigurationSection("quest") != null) {
            for (String name : configYml.getConfigurationSection("quest").getKeys(false)) {
                hashMap.put(UUID.fromString(name), (String) (configYml.get("quest." + name)));
            }
            Bukkit.getLogger().info("퀘스트 복구 완료!");
        }
        return hashMap;
    }

    @Override
    public void onDisable(){
        questSave();
        scrollManager.saveScroll();
        scrollManager.clear();
        quest.clear();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg){
        if(label.equalsIgnoreCase("tm")){
            if(sender instanceof ConsoleCommandSender){
                if(arg.length < 2){
                    sender.sendMessage("/tm 닉네임 [메인]:[서브]");
                    return false;
                }
                if(Bukkit.getPlayer(arg[0]) == null){
                    sender.sendMessage("해당 플레이어는 접속중이지 않습니다!");
                    return false;
                }
                Player target = Bukkit.getPlayer(arg[0]);


                String total = "";
                for (int i = 1; i < arg.length; i++) {
                    total += " " + arg[i];
                }
                String[] divide = total.split(":");
                if(divide.length == 1){
                    String main = divide[0];
                    target.sendTitle(main, "", 30, 70, 20);
                    return true;
                }
                String main = divide[0];
                String sub = divide[1];
                target.sendTitle(main, sub, 30, 70, 20);
                return true;
            }
        }

        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if(label.equalsIgnoreCase("퀘스트")){

            if(hasFinishedQuest(player)){
                player.sendMessage("이미 완료한 퀘스트가 있습니다!");
                return false;
            }

            if(arg.length == 0){
                checkQuest(player);
                return true;
            }

            if(arg[0].equalsIgnoreCase("광부")){
                if(!quest.containsKey(player.getUniqueId())){
                    player.sendMessage("다이아몬드 10개, 철괴 25개를 가져오시오.");
                    quest.put(player.getUniqueId(), "광부");
                    return true;
                } else{
                    player.sendMessage("이미 받은 퀘스트가 있습니다!");
                }
                return false;
            } else if(arg[0].equalsIgnoreCase("나무꾼")){
                if(!quest.containsKey(player.getUniqueId())){
                    player.sendMessage("정글나무 25개, 가문비나무 25개를 가져오시오.");
                    quest.put(player.getUniqueId(), "나무꾼");
                    return true;
                } else{
                    player.sendMessage("이미 받은 퀘스트가 있습니다!");
                }
                return false;

            } else if(arg[0].equalsIgnoreCase("농부")){
                if(!quest.containsKey(player.getUniqueId())){
                    player.sendMessage("사탕수수 32개, 감자 32개를 가져오시오.");
                    quest.put(player.getUniqueId(), "농부");
                    return true;
                } else{
                    player.sendMessage("이미 받은 퀘스트가 있습니다!");
                }
                return false;
            } else if(arg[0].equalsIgnoreCase("확인")){
                finishQuest(player);
                return true;
            } else{
                helpMessage(player);
            }
        }

        if(label.equalsIgnoreCase("tm")){
            if(arg.length < 2){
                player.sendMessage("/tm 닉네임 [메인]:[서브]");
                return false;
            }
            if(Bukkit.getPlayer(arg[0]) == null){
                player.sendMessage("해당 플레이어는 접속중이지 않습니다!");
                return false;
            }
            Player target = Bukkit.getPlayer(arg[0]);


            String total = "";
            for (int i = 1; i < arg.length; i++) {
                total += " " + arg[i];
            }
            String[] divide = total.split(":");
            if(divide.length == 1){
                String main = divide[0];
                target.sendTitle(main, "", 30, 70, 20);
                return true;
            }
            String main = divide[0];
            String sub = divide[1];
            target.sendTitle(main, sub, 30, 70, 20);
            return true;
        }
        return false;
    }

    public void helpMessage(Player player){
        player.sendMessage("/퀘스트 광부");
        player.sendMessage("/퀘스트 나무꾼");
        player.sendMessage("/퀘스트 농부");
        player.sendMessage("/퀘스트");
        player.sendMessage("/퀘스트 확인");
    }

    public void checkQuest(Player player){
        if(quest.containsKey(player.getUniqueId())){
            String selection = quest.get(player.getUniqueId());
            if(selection.equals("광부")){
                int diamond = getItemAmount(Material.DIAMOND, player, (short) 0);
                int iron = getItemAmount(Material.IRON_INGOT, player, (short) 0);

                player.sendMessage("§7§m────────────── §f광부 퀘스트 진행중 §7§m ──────────────");
                player.sendMessage(" ");
                player.sendMessage("§b다이아몬드§f: §7(§a" + diamond + " §7/ " + "§c10개§7)\n" +
                        "§7철괴§f: §7(§a" + iron + " §7/ " + "§c25개§7)\n" + "§f해당 아이템을 획득 후 §6/퀘스트 확인 §f명령어를 입력해주세요.");
                player.sendMessage(" ");
                player.sendMessage("§7§m─────────────────────────────────────────────");
            } else if(selection.equals("나무꾼")){
                int jungle = getItemAmount(Material.LOG, player, (short) 3);
                int spruce = getItemAmount(Material.LOG, player, (short) 1);

                player.sendMessage("§7§m────────────── §f나무꾼 퀘스트 진행중 §7§m ──────────────");
                player.sendMessage(" ");
                player.sendMessage("§e정글나무§f: §7(§a" + jungle + " §7/ " + "§c25개§7)\n" +
                        "§6가문비나무§f: §7(§a" + spruce + " §7/ " + "§c25개§7)\n" + "§f해당 아이템을 획득 후 §6/퀘스트 확인 §f명령어를 입력해주세요.");
                player.sendMessage(" ");
                player.sendMessage("§7§m──────────────────────────────────────────────");
            } else if(selection.equals("농부")){
                int potato = getItemAmount(Material.POTATO_ITEM, player, (short) 0);
                int cane = getItemAmount(Material.SUGAR_CANE, player, (short) 0);

                player.sendMessage("§7§m────────────── §f농부 퀘스트 진행중 §7§m ──────────────");
                player.sendMessage(" ");
                player.sendMessage("§6감자§f: §7(§a" + potato + " §7/ " + "§c32개§7)\n" +
                        "§a사탕수수§f: §7(§a" + cane + " §7/ " + "§c32개§7)\n" + "§f해당 아이템을 획득 후 §6/퀘스트 확인 §f명령어를 입력해주세요.");
                player.sendMessage(" ");
                player.sendMessage("§7§m─────────────────────────────────────────────");
            }
        } else{
            player.sendMessage("§7[ §c! §7] §7아직 진행중인 §c퀘스트§7가 없습니다!");
        }
    }

    public void finishQuest(Player player){
        if(quest.containsKey(player.getUniqueId())){
            String selection = quest.get(player.getUniqueId());
            if(selection.equals("광부")){
                int diamond = getItemAmount(Material.DIAMOND, player, (short) 0);
                int iron = getItemAmount(Material.IRON_INGOT, player, (short) 0);

                if(diamond >= 10 && iron >= 25){
                    removeItem(Material.DIAMOND, player, (short) 0, 10);
                    removeItem(Material.IRON_INGOT, player, (short) 0, 25);
                    quest.put(player.getUniqueId(), "완료");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set jobs.complete");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set chatformat.mining");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addlevels " + player.getName() + " 채광 30");
                    player.sendMessage("§7[ §c! §7] §6퀘스트§7를 완료하였습니다!");
                } else{
                    if(diamond >= 10) {
                        player.sendMessage("§7[ §c! §7] 철괴가 §c" + (25 - iron) + "§7개 더 필요합니다!");
                    } else if(iron >= 25) {
                        player.sendMessage("§7[ §c! §7] 다이아온드가 §c" + (10 - diamond) + "§7개 더 필요합니다!");
                    }else {
                        player.sendMessage("§7[ §c! §7] 다이아온드가 §c" + (10 - diamond) + "§7개, 철괴가 §c" + (25 - iron) + "§7개 더 필요합니다!");
                    }
                }
            } else if(selection.equals("나무꾼")){
                int jungle = getItemAmount(Material.LOG, player, (short) 3);
                int spruce = getItemAmount(Material.LOG, player, (short) 1);

                if(jungle >= 25 && spruce >= 25){
                    removeItem(Material.LOG, player, (short) 3, 25);
                    removeItem(Material.LOG, player, (short) 1, 25);
                    quest.put(player.getUniqueId(), "완료");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set jobs.complete");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set chatformat.woodcutting");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addlevels " + player.getName() + " 벌목 30");
                    player.sendMessage("§7[ §c! §7] §6퀘스트§7를 완료하였습니다!");
                } else{
                    if(jungle >= 25) {
                        player.sendMessage("§7[ §c! §7] 가문비나무 원목이 §c" + (25 - spruce) + "§7개 더 필요합니다!");
                    }else if(spruce >= 25) {
                        player.sendMessage("§7[ §c! §7] 정글나무 원목이 §c" + (25 - jungle) + "§7개 더 필요합니다!");
                    }else {
                        player.sendMessage("§7[ §c! §7] 정글나무 원목이 §c" + (25 - jungle) + "§7개, 가문비나무 원목이 §c" + (25 - spruce) + "§7개 더 필요합니다!");
                    }
                }
            } else if(selection.equals("농부")){
                int potato = getItemAmount(Material.POTATO_ITEM, player, (short) 0);
                int cane = getItemAmount(Material.SUGAR_CANE, player, (short) 0);

                if(potato >= 32 && cane >= 32){
                    removeItem(Material.POTATO_ITEM, player, (short) 0, 32);
                    removeItem(Material.SUGAR_CANE, player, (short) 0, 32);
                    quest.put(player.getUniqueId(), "완료");
                    player.sendMessage("§7[ §c! §7] §6퀘스트§7를 완료하였습니다!");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set jobs.complete");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set chatformat.herbalism");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addlevels " + player.getName() + " 약초학 30");

                } else{
                    if(potato >= 32) {
                        player.sendMessage("§7[ §c! §7] 사탕수수가 §c" + (32 - cane) + "§7개 더 필요합니다!");
                    }else if(cane >= 32) {
                        player.sendMessage("§7[ §c! §7] 감자§c " + (32 - potato) + "§7개 더 필요합니다!");
                    }else {
                        player.sendMessage("§7[ §c! §7] 감자§c " + (32 - potato) + "§7개, 사탕수수가 §c" + (32 - cane) + "§7개 더 필요합니다!");
                    }
                }
            }
        } else{
            player.sendMessage("§7[ §c! §7] §7아직 진행중인 §c퀘스트§7가 없습니다!");
        }
    }

    public int getItemAmount(Material mat, Player player, Short data){
        int amount = 0;
        for(ItemStack is : player.getInventory().getContents()){
            if(is == null || is.getType() == Material.AIR){
                continue;
            }

            if(is.getType().equals(mat) && is.getDurability() == data){
                amount += is.getAmount();
            }
        }
        return amount;
    }

    public void removeItem(Material mat, Player player, Short data, int amount){
        int temp = 0;


        for(ItemStack is : player.getInventory().getContents()){
            if(temp >= amount) return;

            if(is == null || is.getType() == Material.AIR){
                continue;
            }

            if(is.getType().equals(mat) && is.getDurability() == data){
                if(is.getAmount() >= amount){
                    is.setAmount(is.getAmount() - (amount - temp));
                    return;
                } else{
                        temp += is.getAmount();
                        is.setAmount(0);
                }
            }
        }
    }
}
