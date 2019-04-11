package scroll;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import rpgmain.RPGMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import specialmod.Mod;

import java.util.*;

public class Scroll {
    private RPGMain instance;

    private ItemStack item = new ItemStack(Material.PAPER);
    private String name;

    private int modSuccess;
    private int modFail;

    private LinkedHashMap<Enchantment, Integer> enchant = new LinkedHashMap<>();
    private Mod mod;
    /**
     * SR + FR + KR + BR should be equal to 100
     */
    private int successRate;
    private int failRate;
    private int keepRate;
    private int brokenRate;
    private ArrayList<String> scrollLore;

    public Scroll(RPGMain instance) {
        this.instance = instance;
    }

    public boolean isAvailable() {
        if ((name != null) && (successRate + failRate + keepRate + brokenRate == 100)) {
            if (!enchant.isEmpty()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void implementScroll(String name, LinkedHashMap<Enchantment, Integer> enchant, int successRate, int failRate, int keepRate, int brokenRate, ArrayList<String> lore, Mod mod) {
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.enchant = enchant;
        this.successRate = successRate;
        this.failRate = failRate;
        this.keepRate = keepRate;
        this.brokenRate = brokenRate;
        this.scrollLore = lore;

        if(mod != null){
            this.mod = mod;

            modSuccess = instance.getConfig().getInt("scroll." + name + ".modsuccess");
            modFail = instance.getConfig().getInt("scroll." + name + ".modfail");

            if(modSuccess + modFail != 100){
                modSuccess = 50;
                modFail = 50;
            }
        }

        setLore();
    }

    public int getModSuccess(){
        return modSuccess;
    }

    public int getModFail(){
        return modFail;
    }

    public String getName() {
        return name;
    }

    public void saveScroll() {
        name = name.replaceAll("§", "&");

        ArrayList<String> loreForSave = new ArrayList<>();
        scrollLore.forEach((e) -> loreForSave.add(e.replaceAll("§", "&")));
        scrollLore.clear();

        for (Enchantment en : enchant.keySet()) {
            instance.getConfig().set("scroll." + name + ".enchant" + "." + en.getName(), enchant.get(en));
        }
        instance.getConfig().set("scroll." + name + ".successRate", successRate);
        instance.getConfig().set("scroll." + name + ".failRate", failRate);
        instance.getConfig().set("scroll." + name + ".keepRate", keepRate);
        instance.getConfig().set("scroll." + name + ".brokenRate", brokenRate);
        instance.getConfig().set("scroll." + name + ".lore", loreForSave);

        if(mod != null){
            instance.getConfig().set("scroll." + name + ".mod", mod.toString());

            instance.getConfig().set("scroll." + name + ".modsuccess", modSuccess);
            instance.getConfig().set("scroll." + name + ".modfail", modFail);
        }

        instance.saveConfig();
    }

    public void setLore() {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (scrollLore != null) {
            meta.setLore(removePlaceHolder(scrollLore));
        } else {
            meta.setLore(getLoreFromConfig());
            scrollLore = (ArrayList<String>) instance.getConfig().getStringList("scroll." + name.replaceAll("§", "&") + ".lore");
        }
        item.setItemMeta(meta);
    }

    public ItemStack getItemStack() {
        setLore();
        return item;
    }

    public boolean hasMod(){
        return mod != null;
    }

    public Mod getMod(){
        return mod;
    }

    public HashMap<Enchantment, Integer> getEnchant(){
        return enchant;
    }

    public int getSuccessRate(){
        return successRate;
    }

    public int getFailRate() {
        return failRate;
    }

    public int getKeepRate() {
        return keepRate;
    }

    public int getBrokenRate() {
        return brokenRate;
    }

    private ArrayList<String> getLoreFromConfig() {
        ArrayList<String> temp = (ArrayList<String>) instance.getConfig().getStringList("scroll." + name.replaceAll("§", "&") + ".lore");

            temp = removePlaceHolder(temp);
            return temp;
    }

    public String getBriefDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        for(Enchantment e : enchant.keySet()){
            sb.append("§7 :: " + "§e" + instance.getUtil().getLocalNameByEnchant(e) + " §7-> " + "§e" + enchant.get(e));
        }

        sb.append("\n §e성공확률§7: " + successRate + "§e% ");
        sb.append("§7 :: §e실패확률§7: " + failRate + "§e% ");
        sb.append("§7 :: §e유지확률§7: " + keepRate + "§e% ");
        sb.append("§7 :: §e파괴확률§7: " + brokenRate + "§e% ");

        return sb.toString();
    }

    private ArrayList<String> removePlaceHolder(ArrayList<String> list) {
        ArrayList<String> returnList = new ArrayList<>();

        list.forEach((e) -> {
            e = e.replaceAll("%name%", name);
            e = e.replaceAll("%success%", String.valueOf(successRate));
            e = e.replaceAll("%fail%", String.valueOf(failRate));
            e = e.replaceAll("%broken%", String.valueOf(brokenRate));
            e = ChatColor.translateAlternateColorCodes('&', e);
            returnList.add(e);
        });
        return returnList;
    }

   /** public String enchantRegexor(String e){
        if(StringUtils.substringBetween(e, "%", "%") != null){
            String regex = (StringUtils.substringBetween(e, "%", "%"));
            if(regex.contains("enchant")){
                int index = (regex.indexOf("t") + 1);
                String numberInString = regex.substring(index, index + 1);
                try{
                    int number = Integer.parseInt(numberInString);

                }catch(NumberFormatException ex){
                    int a = regex.lastIndexOf("%");
                    StringBuilder sb = new StringBuilder(regex);
                    sb.deleteCharAt(a);
                    String recursive = sb.toString();
                    enchantRegexor(recursive);
                }
            }
        }
        return null;
    } */
}
