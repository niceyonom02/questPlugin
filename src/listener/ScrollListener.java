package listener;

import Main.Main;
import event.EventManager;
import inventory.ScrollManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rpgmain.RPGMain;
import rpgmain.ProgressBar;
import scroll.Scroll;
import specialmod.Mod;
import specialmod.PlusMinusOne;
import util.NBTEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ScrollListener implements Listener {
    private ArrayList<UUID> inReinforce = new ArrayList<>();
    private HashMap<UUID, ItemStack> is = new HashMap<>();
    private HashMap<UUID, ItemStack> scrollItem = new HashMap<>();
    private RPGMain instance;
    private ScrollManager scrollManager;

    public ScrollListener(RPGMain instance, ScrollManager scrollManager) {
        this.instance = instance;
        this.scrollManager = scrollManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (inReinforce.contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().getInventory().addItem(is.get(e.getPlayer().getUniqueId()));
            e.getPlayer().getInventory().addItem(scrollItem.get(e.getPlayer().getUniqueId()));

            is.remove(e.getPlayer().getUniqueId());
            scrollItem.remove(e.getPlayer().getUniqueId());

            inReinforce.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        if (e.getCurrentItem() == null) {
            return;
        }
        Player player = (Player) e.getWhoClicked();

        if (inReinforce.contains(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage("§6강화§f가 §e진행 중§f입니다!");
            return;
        }

        if (e.getInventory().getName().equalsIgnoreCase("§0스크롤을 아이템 위에 겹쳐주세요.")) {

            if (scrollManager.findScroll(player.getItemOnCursor()) != null) {
                if(e.getClickedInventory().getItem(e.getSlot()) == null){
                    return;
                }

                ItemStack enchanted = e.getClickedInventory().getItem(e.getSlot()).clone();

                if (enchanted != null) {
                    e.setCancelled(true);
                    Scroll scroll = scrollManager.findScroll(player.getItemOnCursor());

                    if (enchanted.getType() != null) {
                        if (!isAvailableMaterial(enchanted.getType())) {
                            player.setItemOnCursor(null);
                            e.getClickedInventory().setItem(e.getSlot(), null);
                            player.closeInventory();
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 3.0F, 0.5F);
                            player.sendTitle("", "§c해당 §e아이템§f은 강화가 불가능합니다!", 20, 20, 20);
                            // player.getInventory().addItem(scroll.getItemStack());
                            e.getClickedInventory().addItem(enchanted);
                            e.getClickedInventory().addItem(scroll.getItemStack());
                            return;
                        }
                    }

                    if (enchanted.getItemMeta().getDisplayName() != null) {
                        if (!isForbidByName(enchanted.getItemMeta().getDisplayName())) {
                            player.setItemOnCursor(null);
                            e.getClickedInventory().setItem(e.getSlot(), null);
                            player.closeInventory();
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 3.0F, 0.5F);
                            player.sendTitle("", "§c해당 이름§f을 가진 §e아이템§f은 강화가 불가능합니다!", 20, 20, 20);
                            // player.getInventory().addItem(scroll.getItemStack());
                            e.getClickedInventory().addItem(enchanted);
                            e.getClickedInventory().addItem(scroll.getItemStack());
                            return;
                        }
                    }

                    if (enchanted.getAmount() != 1) {
                        player.setItemOnCursor(null);
                        e.getClickedInventory().setItem(e.getSlot(), null);
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 3.0F, 0.5F);
                        player.sendTitle("", "§c1개§f의 §e아이템§f만 강화가 가능합니다!", 20, 20, 20);
                        // player.getInventory().addItem(scroll.getItemStack());
                        e.getClickedInventory().addItem(enchanted);
                        e.getClickedInventory().addItem(scroll.getItemStack());
                        return;
                    }

                    if (!maximumScrollCheck(enchanted)) {
                        player.setItemOnCursor(null);
                        e.getClickedInventory().setItem(e.getSlot(), null);
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 3.0F, 0.5F);
                        player.sendTitle("", "§f이 아이템에 §c스크롤§f을 더 이상 사용할 수 없습니다!", 20, 20, 20);
                        // player.getInventory().addItem(scroll.getItemStack());
                        e.getClickedInventory().addItem(enchanted);
                        e.getClickedInventory().addItem(scroll.getItemStack());
                        return;
                    }
                    if (!maximumEnchantCheck(enchanted, scroll)) {
                        player.setItemOnCursor(null);
                        e.getClickedInventory().setItem(e.getSlot(), null);
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 3.0F, 0.5F);
                        player.sendTitle("", "§6특정 인챈트§f가 §c강화 최대치§f에 도달하였습니다!", 20, 20, 20);
                        // player.getInventory().addItem(scroll.getItemStack());
                        e.getClickedInventory().addItem(enchanted);
                        e.getClickedInventory().addItem(scroll.getItemStack());
                        return;
                    }

                    inReinforce.add(player.getUniqueId());
                    is.put(player.getUniqueId(), enchanted.clone());
                    scrollItem.put(player.getUniqueId(), scroll.getItemStack());

                    player.setItemOnCursor(null);
                    e.getClickedInventory().setItem(e.getSlot(), null);

                    if (NBTEditor.getItemTag(enchanted, "scrollLimit") == null) {
                        enchanted = NBTEditor.setItemTag(enchanted, 1, "scrollLimit");

                        if (enchanted.getItemMeta().getLore() != null) {
                            ItemMeta m = enchanted.getItemMeta();
                            ArrayList<String> lore = (ArrayList<String>) m.getLore();

                            int index = 0;
                            boolean found = false;

                            for (String s : lore) {
                                if (s.contains("회 강화 시도")) {
                                    index = lore.indexOf(s);
                                    found = true;
                                    break;
                                }
                            }
                            int n = (int) NBTEditor.getItemTag(enchanted, "scrollLimit");

                            if(found){
                                lore.remove(index);
                                lore.add(index, "§e" + n + " / " + instance.getUtil().getLimitScroll(enchanted.getItemMeta().getDisplayName()) + "회 강화 시도");
                                m.setLore(lore);
                                enchanted.setItemMeta(m);
                                found = false;
                            } else{
                                lore.add("");
                                lore.add("§e" + n + " / " + instance.getUtil().getLimitScroll(enchanted.getItemMeta().getDisplayName()) + "회 강화 시도");
                                m.setLore(lore);
                                enchanted.setItemMeta(m);
                            }

                            if (!lore.contains("§7[§e최대 인챈트§7]")) {
                                if (enchanted.getItemMeta().getDisplayName() != null) {
                                    lore.add("§f");
                                    lore.add("§7[§e최대 인챈트§7]");

                                    for (Enchantment en : instance.getUtil().getLimitEnchant(enchanted.getItemMeta().getDisplayName()).keySet()) {
                                        if (instance.getUtil().getLimitEnchant(enchanted.getItemMeta().getDisplayName()).equals(instance.getUtil().getDefaultEnchantLimit())) {
                                            lore.add("§e" + "모든 인챈트" + " §7: §6" + instance.getUtil().getDefaultEnchantLevelByInt() + " 레벨");
                                            break;
                                        }

                                        lore.add("§e" + instance.getUtil().getLocalNameByEnchant(en) + " §7: §6" + instance.getUtil().getLimitEnchant(enchanted.getItemMeta().getDisplayName()).get(en) + " 레벨");
                                    }
                                } else {
                                    lore.add("");
                                    lore.add("§7[§e최대 인챈트§7]");
                                    lore.add("§e" + "모든 인챈트" + " §7: §6" + instance.getUtil().getDefaultEnchantLevelByInt() + " 레벨");
                                }
                            }

                            m.setLore(lore);
                            enchanted.setItemMeta(m);
                        } else {
                            ArrayList<String> lore = new ArrayList<>();
                            ItemMeta m = enchanted.getItemMeta();

                            int n = (int) NBTEditor.getItemTag(enchanted, "scrollLimit");
                            lore.add("§e" + n + " / " + instance.getUtil().getLimitScroll(enchanted.getItemMeta().getDisplayName()) + "회 강화 시도");

                            if (!lore.contains("§7[§e최대 인챈트§7]")) {
                                if (enchanted.getItemMeta().getDisplayName() != null) {
                                    lore.add("");
                                    lore.add("§7[§e최대 인챈트§7]");

                                    for (Enchantment en : instance.getUtil().getLimitEnchant(enchanted.getItemMeta().getDisplayName()).keySet()) {
                                        if (instance.getUtil().getLimitEnchant(enchanted.getItemMeta().getDisplayName()).equals(instance.getUtil().getDefaultEnchantLimit())) {
                                            lore.add("§e" + "모든 인챈트" + " §7: §6" + instance.getUtil().getDefaultEnchantLevelByInt() + " 레벨");
                                            break;
                                        }

                                        lore.add("§e" + instance.getUtil().getLocalNameByEnchant(en) + " §7: §6" + instance.getUtil().getLimitEnchant(enchanted.getItemMeta().getDisplayName()).get(en) + " 레벨");
                                    }
                                } else {
                                    lore.add("");
                                    lore.add("§7[§e최대 인챈트§7]");
                                    lore.add("§e" + "모든 인챈트" + " §7: §6" + instance.getUtil().getDefaultEnchantLevelByInt() + " 레벨");
                                }
                            }

                            m.setLore(lore);
                            enchanted.setItemMeta(m);
                        }
                        //player.getInventory().addItem(enchanted);
                        //player.getInventory().setItem(e.getSlot(), enchanted);
                    } else {
                        int previous = (int) NBTEditor.getItemTag(enchanted, "scrollLimit");
                        enchanted = NBTEditor.setItemTag(enchanted, (previous + 1), "scrollLimit");

                        if (enchanted.getItemMeta().getLore() != null) {
                            ItemMeta m = enchanted.getItemMeta();
                            ArrayList<String> lore = (ArrayList<String>) m.getLore();

                            int index = 0;
                            boolean found = false;

                            for (String s : lore) {
                                if (s.contains("회 강화 시도")) {
                                    index = lore.indexOf(s);
                                    found = true;
                                    break;
                                }
                            }
                            int n = (int) NBTEditor.getItemTag(enchanted, "scrollLimit");

                            if(found){
                                lore.remove(index);
                                lore.add(index, "§e" + n + " / " + instance.getUtil().getLimitScroll(enchanted.getItemMeta().getDisplayName()) + "회 강화 시도");
                                m.setLore(lore);
                                enchanted.setItemMeta(m);
                                found = false;
                            }else{
                                lore.add("");
                                lore.add("§e" + n + " / " + instance.getUtil().getLimitScroll(enchanted.getItemMeta().getDisplayName()) + "회 강화 시도");
                                m.setLore(lore);
                                enchanted.setItemMeta(m);
                            }
                        }
                        //player.getInventory().addItem(enchanted);
                        //player.getInventory().setItem(e.getSlot(), enchanted);
                    }

                    ItemStack finalItem = enchanted;

                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 3.0F, 0.5F);
                    ProgressBar progressBar = new ProgressBar(player, instance);
                    progressBar.showBar();

                    Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                return;
                            }

                            notifyResult(scroll, player, finalItem);

                            inReinforce.remove(player.getUniqueId());
                            if (hasTwoAvailableSlot(player)) {
                                player.getInventory().addItem(finalItem);
                            } else {
                                EventManager ev = Main.instance.getEventManager();
                                ev.addItemStack(player, finalItem);
                            }
                        }
                    }, 100L);
                }
            }
        }
    }

    public void notifyResult(Scroll scroll, Player player, ItemStack enchanted) {
        ReinforceResult result = random(scroll.getSuccessRate(), scroll.getFailRate(), scroll.getKeepRate(), scroll.getBrokenRate());

        if (result == ReinforceResult.success) {
            if(scroll.hasMod()){
                Mod mod = scroll.getMod();
                mod.onSuccess(player, enchanted, scroll);
                return;
            }
            for (Enchantment en : scroll.getEnchant().keySet()) {
                int currentLevel = enchanted.getEnchantmentLevel(en);
                enchanted.addUnsafeEnchantment(en, currentLevel + scroll.getEnchant().get(en));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.0F, 0.5F);
            player.sendTitle("§c강화§f에 §a성공 §f하였습니다!", "", 20, 20, 20);
        } else if (result == ReinforceResult.fail) {
            for (Enchantment en : scroll.getEnchant().keySet()) {
                if (!enchanted.containsEnchantment(en)) {
                    continue;
                }

                if (enchanted.getEnchantmentLevel(en) <= scroll.getEnchant().get(en)) {
                    enchanted.removeEnchantment(en);
                    continue;
                }

                int currentLevel = enchanted.getEnchantmentLevel(en);
                enchanted.addUnsafeEnchantment(en, (currentLevel - scroll.getEnchant().get(en)));
            }
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3.0F, 0.5F);
            player.sendTitle("§c강화§f에 §7실패 §f하였습니다!", "", 20, 20, 20);
        } else if (result == ReinforceResult.keep) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 3.0F, 0.5F);
            player.sendTitle("§c아이템§f이 §e유지 §f되었습니다!", "", 20, 20, 20);
        } else if (result == ReinforceResult.broken) {
            enchanted.setAmount(0);
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 3.0F, 0.5F);
            player.sendTitle("§6아이템§f이 §c파괴 §f되었습니다!", "", 20, 20, 20);
        }
    }

    private ReinforceResult random(int success, int fail, int keep, int broken) {
        Random random = new Random();
        int result = random.nextInt(100);

        if (result >= 0 && result < success) {
            return ReinforceResult.success;
        }

        if (result >= success && result < (success + fail)) {
            return ReinforceResult.fail;
        }

        if (result >= (success + fail) && result < (success + fail + keep)) {
            return ReinforceResult.keep;
        }

        if (result >= (success + fail + keep) && result < (success + fail + keep + broken)) {
            return ReinforceResult.broken;
        }
        return null;
    }

    private boolean maximumScrollCheck(ItemStack item) {

        if (NBTEditor.getItemTag(item, "scrollLimit") == null) {
            item = NBTEditor.setItemTag(item, 0, "scrollLimit");
        }

        if (item.getItemMeta().hasDisplayName()) {
            int scrollLimit = instance.getUtil().getLimitScroll(item.getItemMeta().getDisplayName());
            if ((int) NBTEditor.getItemTag(item, "scrollLimit") >= scrollLimit) {
                return false;
            }
        } else {
            if ((int) NBTEditor.getItemTag(item, "scrollLimit") >= instance.getUtil().getDefaultScrollLimit()) {
                return false;
            }
        }
        return true;
    }

    private boolean maximumEnchantCheck(ItemStack item, Scroll scroll) {
        HashMap<Enchantment, Integer> scrollEnchant = scroll.getEnchant();
        HashMap<Enchantment, Integer> enchantmentHashMap;

        if (item.getItemMeta().hasDisplayName()) {
            enchantmentHashMap = instance.getUtil().getLimitEnchant(item.getItemMeta().getDisplayName());

            for (Enchantment key : enchantmentHashMap.keySet()) {
                if (scrollEnchant.containsKey(key)) {
                    if (item.getEnchantmentLevel(key) + scrollEnchant.get(key) > enchantmentHashMap.get(key)) {
                        return false;
                    }
                }
            }
        } else {
            enchantmentHashMap = instance.getUtil().getDefaultEnchantLimit();

            for (Enchantment key : enchantmentHashMap.keySet()) {
                if (scrollEnchant.containsKey(key)) {
                    if (item.getEnchantmentLevel(key) + scrollEnchant.get(key) > enchantmentHashMap.get(key)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean hasTwoAvailableSlot(Player player) {
        int k = 0;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) == null) {
                if (i >= 36 && i <= 40) {
                    continue;
                }
                k++;
            }
        }

        return k >= 2;
    }

    public boolean isAvailableMaterial(Material material) {
        return instance.getUtil().isAvailableMaterial(material);
    }

    public boolean isForbidByName(String name) {
        return instance.getUtil().isForbidByname(name);
    }

    /**private boolean maximumEnchantCheck(ItemStack item){
     return false;
     } */

}
