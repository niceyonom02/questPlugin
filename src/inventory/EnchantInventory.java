package inventory;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import rpgmain.RPGMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class EnchantInventory implements Listener {
    private Inventory chooseEnchant = Bukkit.createInventory(null, 54, "인챈트를 골라주세요!");
    private SetLevelInventory setLevelInventory;
    private HashMap<Enchantment, Integer> enchant = new HashMap<>();
    private Player player;
    private ScrollMaker scrollMaker;
    private RPGMain instance;

    public EnchantInventory(Player player, ScrollMaker scrollMaker, RPGMain instance) {
        this.player = player;
        this.scrollMaker = scrollMaker;
        this.instance = instance;
        //inv = this;

        setLevelInventory = new SetLevelInventory(player, this, instance);
    }

    public void setItem() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();

        File file = new File(instance.getDataFolder() + File.separator + "enchantMenu.yml");
        FileConfiguration enchantConfig = YamlConfiguration.loadConfiguration(file);

        for (String enchantIn : enchantConfig.getConfigurationSection("enchant").getKeys(false)) {
            String localName = ChatColor.translateAlternateColorCodes('&', instance.getUtil().getLocalNameByEnchantCode(enchantIn));
            int slot = enchantConfig.getInt("enchant." + enchantIn + ".slot");
            String materialWithoutData = enchantConfig.getString("enchant." + enchantIn + ".material");
            Short materialData = (Short) enchantConfig.get("enchant." + enchantIn + ".data");

            ArrayList<String> loreWithoutColor = (ArrayList<String>) enchantConfig.getStringList("enchant." + enchantIn + ".lore");

            if(enchantIn.equalsIgnoreCase("check")){
                loreWithoutColor = getEnchantLore(loreWithoutColor);
            }

            ArrayList<String> coloredLore = new ArrayList<>();
            loreWithoutColor.forEach((str) -> coloredLore.add(ChatColor.translateAlternateColorCodes('&', str)));
            loreWithoutColor.clear();
            Material material = Material.valueOf(materialWithoutData);

            if (coloredLore != null) {
                meta.setLore(coloredLore);
            }
            if (localName != null) {
                meta.setDisplayName(localName);
            }

            item = new ItemStack(material);
            if (materialData != null) {
                item.setDurability(materialData);
            }
            item.setItemMeta(meta);
            chooseEnchant.setItem(slot, item);
        }

        /** if(!enchant.isEmpty()){
         lore.clear();
         item = new ItemStack(Material.GOLD_BLOCK);
         meta = item.getItemMeta();
         lore = new ArrayList<>();
         for(Enchantment en : enchant.keySet()){
         lore.add(en.getName() + " +" + enchant.get(en) + "강");
         }
         meta.setLore(lore);
         item.setItemMeta(meta);
         chooseEnchant.setItem(3, item);
         } else{
         lore.clear();
         item = new ItemStack(Material.GOLD_BLOCK);
         chooseEnchant.setItem(3, item);
         } */
    }

    public void putEnchant(Enchantment en, int level) {
        enchant.put(en, level);
    }

    public void openInventory() {
        setItem();
        Bukkit.getPluginManager().registerEvents(this, instance);
        player.openInventory(chooseEnchant);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }
        if (e.getInventory().equals(chooseEnchant)) {
            InventoryCloseEvent.getHandlerList().unregister(this);
            InventoryClickEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler
    public void onEnchantClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        if (e.getCurrentItem() == null) {
            return;
        }
        if (e.getClickedInventory().equals(chooseEnchant)) {
            e.setCancelled(true);

            File file = new File(instance.getDataFolder() + File.separator + "enchantMenu.yml");
            FileConfiguration enchantConfig = YamlConfiguration.loadConfiguration(file);

            for (String enchantIn : enchantConfig.getConfigurationSection("enchant").getKeys(false)) {
                if (enchantConfig.getInt("enchant." + enchantIn + ".slot") == e.getSlot()) {
                    String localName = instance.getUtil().getLocalNameByEnchantCode(enchantIn);

                    if (checkMenuButton(enchantIn)) {
                        break;
                    } else {
                        Enchantment tempEnchant = Enchantment.getByName(enchantIn.toUpperCase());

                        player.sendMessage(instance.getUtil().getConfigMessage("chooseEnchantName").replaceAll("%localName%", localName));
                        player.closeInventory();
                        player.sendMessage(instance.getUtil().getConfigMessage("chooseEnchantLevel"));
                        setLevelInventory.setCurrentEnchant(tempEnchant);
                        setLevelInventory.openInventory();
                        break;
                    }
                }
            }
        }
    }

    public boolean checkMenuButton(String string) {
        if (string.equalsIgnoreCase("close")) {
            player.sendMessage(instance.getUtil().getConfigMessage("setEnchant"));
            for (Enchantment en : enchant.keySet()) {
                scrollMaker.makeEnchantment(en, enchant.get(en));
            }
            player.closeInventory();
            scrollMaker.openInventory();
            return true;
        } else if (string.equalsIgnoreCase("check")) {
            return true;
        }
        return false;
    }

    private ArrayList<String> getEnchantLore(ArrayList<String> list){
        ArrayList<String> lore = new ArrayList<>();

        if(enchant.isEmpty()){
            list.forEach((e) -> {
                if(!e.contains("%enchant%") && !e.contains("%level%")){
                    lore.add(e);
                }
            });
        } else{
            list.forEach((e) -> {
                if(!e.contains("%enchant%") && !e.contains("level%")){
                    lore.add(e);
                } else{
                    for(Enchantment en : enchant.keySet()){
                        String temp = e;

                        if(e.contains("%enchant%")){
                            temp = temp.replaceAll("%enchant%", instance.getUtil().getLocalNameByEnchant(en));
                        }
                        if(e.contains("%level%")){
                            temp = temp.replaceAll("%level%", String.valueOf(enchant.get(en)));
                        }
                        lore.add(temp);
                    }
                }
            });
        }
        return lore;
    }
}
