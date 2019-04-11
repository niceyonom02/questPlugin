package inventory;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import rpgmain.RPGMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import scroll.Scroll;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ScrollMaker implements Listener {
    private Player player;
    private RPGMain instance;
    private ScrollManager scrollManager;
    private HashMap<String, String> behavior = new HashMap<>();
    private LinkedHashMap<Enchantment, Integer> enchant = new LinkedHashMap<>();
    private Inventory scrollInventory;
    private ArrayList<String> scrollLore = new ArrayList<>();
    private EnchantInventory enchantInventory;

    public ScrollMaker(Player player, RPGMain instance, ScrollManager scrollManager) {
        this.player = player;
        this.instance = instance;
        this.scrollManager = scrollManager;


        scrollInventory = Bukkit.createInventory(null, 45, "스크롤 제작");
        Bukkit.getLogger().info("inventory.ScrollMaker" + player.getName());

        enchantInventory = new EnchantInventory(player, this, instance);
    }

    public void makeEnchantment(Enchantment en, int level) {
        if (en!= null) {
            enchant.put(en, level);
        }
        behavior.remove(player.getName());
    }

    public void setItem() {
        settingItem(Material.DIAMOND, "이름", 11);
        settingItem(Material.BED, "인챈트", 13);
        settingItem(Material.GOLD_BLOCK, "성공 확률", 15);
        settingItem(Material.IRON_BLOCK, "실패 확률", 21);
        settingItem(Material.DIAMOND_BLOCK, "유지 확률", 23);
        settingItem(Material.SNOW_BLOCK, "파괴 확률", 29);
        settingItem(Material.LOG, "설명", 31);
        settingItem(Material.EMERALD_BLOCK, "스크롤 만들기", 33);
    }

    public void openInventory() {
        setItem();
        Bukkit.getPluginManager().registerEvents(this, instance);
        player.openInventory(scrollInventory);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        if (e.getInventory().equals(scrollInventory)) {
            Bukkit.getPluginManager().registerEvents(this, instance);
            InventoryCloseEvent.getHandlerList().unregister(this);
            InventoryClickEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();

        if (behavior.containsKey(player.getName())) {
            switch (behavior.get(player.getName())) {
                case "이름":
                    e.setCancelled(true);
                    behavior.put("이름", ChatColor.translateAlternateColorCodes('&', message));
                    behavior.remove(player.getName());
                    player.sendMessage(instance.getUtil().getConfigMessage("setName"));
                    openInventory();
                    break;
                case "성공 확률":
                    e.setCancelled(true);
                    try {
                        Integer.parseInt(message);
                        behavior.put("성공 확률", message);
                        behavior.remove(player.getName());
                        player.sendMessage(instance.getUtil().getConfigMessage("setSuccess"));
                        openInventory();
                        break;
                    } catch (NumberFormatException ex) {
                        typeMisMatch();
                        break;
                    }
                case "실패 확률":
                    e.setCancelled(true);
                    try {
                        Integer.parseInt(message);
                        behavior.put("실패 확률", message);
                        behavior.remove(player.getName());
                        player.sendMessage(instance.getUtil().getConfigMessage("setFail"));
                        openInventory();
                        break;
                    } catch (NumberFormatException ex) {
                        typeMisMatch();
                        break;
                    }
                case "유지 확률":
                    e.setCancelled(true);
                    try {
                        Integer.parseInt(message);
                        behavior.put("유지 확률", message);
                        behavior.remove(player.getName());
                        player.sendMessage(instance.getUtil().getConfigMessage("setKeep"));
                        openInventory();
                        break;
                    } catch (NumberFormatException ex) {
                        typeMisMatch();
                        break;
                    }
                case "파괴 확률":
                    e.setCancelled(true);
                    try {
                        Integer.parseInt(message);
                        behavior.put("파괴 확률", message);
                        behavior.remove(player.getName());
                        player.sendMessage(instance.getUtil().getConfigMessage("setBroken"));
                        openInventory();
                        break;
                    } catch (NumberFormatException ex) {
                        typeMisMatch();
                        break;
                    }
                case "설명":
                    e.setCancelled(true);
                    if(message.equalsIgnoreCase("저장")){
                        behavior.remove(player.getName());
                        player.sendMessage(instance.getUtil().getConfigMessage("setLore"));
                        openInventory();
                        break;
                    } else{
                        scrollLore.add(ChatColor.translateAlternateColorCodes('&', message));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
            }
        }
    }

    private void typeMisMatch(){
        behavior.remove(player.getName());
        player.sendMessage(instance.getUtil().getConfigMessage("mustNatural"));
        openInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        if (e.getCurrentItem() == null) {
            return;
        }

        if (e.getClickedInventory().equals(scrollInventory)) {
            e.setCancelled(true);

            if (e.getSlot() == 11) {
                behavior.put(player.getName(), "이름");
                player.closeInventory();
                player.sendMessage(instance.getUtil().getConfigMessage("typeName"));
            } else if (e.getSlot() == 13) {
                behavior.put(player.getName(), "인챈트");
                player.closeInventory();
                enchantInventory.openInventory();
            } else if (e.getSlot() == 15) {
                behavior.put(player.getName(), "성공 확률");
                player.closeInventory();
                player.sendMessage(instance.getUtil().getConfigMessage("typeSuccess"));
            } else if (e.getSlot() == 21) {
                behavior.put(player.getName(), "실패 확률");
                player.closeInventory();
                player.sendMessage(instance.getUtil().getConfigMessage("typeFail"));
            } else if (e.getSlot() == 23) {
                behavior.put(player.getName(), "유지 확률");
                player.closeInventory();
                player.sendMessage(instance.getUtil().getConfigMessage("typeKeep"));
            } else if (e.getSlot() == 29) {
                behavior.put(player.getName(), "파괴 확률");
                player.closeInventory();
                player.sendMessage(instance.getUtil().getConfigMessage("typeBroken"));
            } else if(e.getSlot() == 31){
                behavior.put(player.getName(), "설명");
                player.closeInventory();
                player.sendMessage(instance.getUtil().getConfigMessage("typeSave"));
            } else if(e.getSlot() == 33){
                Scroll newScroll = new Scroll(instance);
                try{
                    newScroll.implementScroll(behavior.get("이름"), enchant, Integer.parseInt(behavior.get("성공 확률")),
                            Integer.parseInt(behavior.get("실패 확률")), Integer.parseInt(behavior.get("유지 확률")), Integer.parseInt(behavior.get("파괴 확률")), scrollLore, null);
                    if(newScroll.isAvailable()){
                        if(!scrollManager.isNameDuplicate(newScroll.getName())){
                            scrollManager.registerScrollByPlayer(player, newScroll);
                            player.closeInventory();
                        } else{
                            player.sendMessage(instance.getUtil().getConfigMessage("duplicate"));
                        }
                    } else{
                        player.sendMessage(instance.getUtil().getConfigMessage("notComplete"));
                    }
                }catch (Exception ex){
                    player.sendMessage(instance.getUtil().getConfigMessage("notComplete"));
                }
            }
        }
    }


    public void settingItem(Material material, String behaviorPath, int slot) {
        ArrayList<String> lore = new ArrayList<>();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (behaviorPath.equalsIgnoreCase("스크롤 만들기")) {
            lore.add("§6스크롤 완성하기");
            meta.setLore(lore);
            item.setItemMeta(meta);
            scrollInventory.setItem(slot, item);
            return;
        }

        if (behaviorPath.equalsIgnoreCase("인챈트")) {
            if (enchant.isEmpty()) {
                lore.add(instance.getUtil().getConfigMessage("setPath").replaceAll("%behaviorPath%", behaviorPath));
            } else {
                for (Enchantment en : enchant.keySet()) {
                    lore.add(instance.getUtil().getLocalNameByEnchant(en) + " :: +" + enchant.get(en) + "강");
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            scrollInventory.setItem(slot, item);
            return;
        }

        if(behaviorPath.equalsIgnoreCase("설명")){
            if(scrollLore.isEmpty()){
                lore.add(instance.getUtil().getConfigMessage("setPath").replaceAll("%behaviorPath%", behaviorPath));
                //lore.add("§6이 곳을 눌러 스크롤의 " + behaviorPath + "을(를) 설정하세요!");
            } else{
                for(String e : scrollLore){
                    lore.add(e);
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            scrollInventory.setItem(slot, item);
            return;
        }

        if (behavior.get(behaviorPath) == null) {
            lore.add(instance.getUtil().getConfigMessage("setPath").replaceAll("%behaviorPath%", behaviorPath));
        } else {
            lore.add(instance.getUtil().getConfigMessage("currentPath").replaceAll("%behaviorPath%", behaviorPath).replaceAll("%getBehavior%", behavior.get(behaviorPath)));
            //lore.add("현재 " + behaviorPath + ":: " + behavior.get(behaviorPath));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        scrollInventory.setItem(slot, item);
    }
}
