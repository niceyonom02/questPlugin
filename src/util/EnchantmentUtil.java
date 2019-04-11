package util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import rpgmain.RPGMain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EnchantmentUtil {
    private RPGMain instance;
    private File messageFile;
    private File enchantFile;
    private File limitFile;
    private File controlFile;
    private YamlConfiguration enchant;
    private YamlConfiguration message;
    private YamlConfiguration limit;
    private YamlConfiguration control;


    public EnchantmentUtil(RPGMain instance) {
        this.instance = instance;

        enchantFile = new File(instance.getDataFolder(), "enchantMenu.yml");
        enchant = YamlConfiguration.loadConfiguration(enchantFile);

        messageFile = new File(instance.getDataFolder(), "message.yml");
        message = YamlConfiguration.loadConfiguration(messageFile);

        limitFile = new File(instance.getDataFolder(), "limitEnchant.yml");
        limit = YamlConfiguration.loadConfiguration(limitFile);

        controlFile = new File(instance.getDataFolder(), "controlItem.yml");
        control = YamlConfiguration.loadConfiguration(controlFile);
    }

    public String getLocalNameByEnchant(Enchantment en) {
        String code = en.getName();
        return getLocalNameByEnchantCode(code);
    }

    public String getLocalNameByEnchantCode(String code) {
        for (String key : enchant.getConfigurationSection("enchant").getKeys(false)) {
            if (key.equalsIgnoreCase(code)) {
                String locName = enchant.getString("enchant." + key + ".localName");
                return locName;
            }
        }
        return null;
    }

    public String getConfigMessage(String path) {
        for (String key : message.getConfigurationSection("message").getKeys(false)) {
            if (key.equalsIgnoreCase(path)) {
                String unColored = message.getString("message." + key);
                String colorized = ChatColor.translateAlternateColorCodes('&', unColored);
                return colorized;
            }
        }
        return null;
    }

    public HashMap<Enchantment, Integer> getLimitEnchant(String displayName) {
        HashMap<Enchantment, Integer> enchantHashMap = new HashMap<>();

        for (String key : limit.getConfigurationSection("limitEnchant").getKeys(false)) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', key);

            if (displayName.equalsIgnoreCase(coloredName)){
                if (limit.getConfigurationSection("limitEnchant." + key + ".enchant") != null) {
                    for (String enchantKey : limit.getConfigurationSection("limitEnchant." + key + ".enchant").getKeys(false)) {
                        if(enchantKey.equalsIgnoreCase("all")){
                            for(Enchantment en : Enchantment.values()){
                                enchantHashMap.put(en, limit.getInt("limitEnchant." + key + ".enchant." + enchantKey));
                            }
                            return enchantHashMap;
                        }

                        for (Enchantment en : Enchantment.values()) {
                            String toName = en.getName();

                            if(enchantKey.equalsIgnoreCase(toName)){
                                enchantHashMap.put(en, limit.getInt("limitEnchant." + key + ".enchant." + enchantKey));
                                break;
                            }
                        }
                    }
                    return enchantHashMap;
                }
            }
        }
        return getDefaultEnchantLimit();
    }

    public int getLimitScroll(String displayName){
        for(String key : limit.getConfigurationSection("limitEnchant").getKeys(false)){
            String coloredName = ChatColor.translateAlternateColorCodes('&', key);

            if(displayName == null){
                return getDefaultScrollLimit();
            }

            if(displayName.equalsIgnoreCase(coloredName)){
                if(limit.getInt("limitEnchant." + key + ".maxScroll") != 0){
                    int maximum = limit.getInt("limitEnchant." + key + ".maxScroll");
                    return maximum;
                }
            }
        }
        return getDefaultScrollLimit();
    }

    public int getDefaultScrollLimit(){
        return limit.getInt("limitEnchant.defaultScrollLimit");
    }

    public HashMap<Enchantment, Integer> getDefaultEnchantLimit(){
        HashMap<Enchantment, Integer> enchantHashMap = new HashMap<>();

        for(Enchantment en : Enchantment.values()){
            enchantHashMap.put(en, limit.getInt("limitEnchant.defaultEnchantLimit"));
        }
        return enchantHashMap;
    }

    public int getDefaultEnchantLevelByInt(){
        return limit.getInt("limitEnchant.defaultEnchantLimit");
    }

    public boolean isAvailableMaterial(Material material){
        List<String> whiteList = control.getStringList("whitelist");

        if(whiteList.isEmpty()){
            return false;
        }

        for(String e : whiteList){
            if(Material.matchMaterial(e).equals(material)){
                return true;
            }
        }
        return false;
    }

    public boolean isForbidByname(String name){
        List<String> blackList = new ArrayList<>();

        control.getStringList("blacklist").forEach((e) -> {
            e = ChatColor.translateAlternateColorCodes('&', e);
            blackList.add(e);
        });

        if(blackList.isEmpty()){
            return true;
        }

        for(String str : blackList){
            if(str.equalsIgnoreCase(name)){
                return false;
            }
        }
        return true;
    }
}
