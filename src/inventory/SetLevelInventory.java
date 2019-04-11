package inventory;

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

public class SetLevelInventory implements Listener{
    private Inventory setEnchantLevel = Bukkit.createInventory(null, 27, "인챈트 레벨을 설정해 주세요!");

    private Player player;
    private EnchantInventory enchantInventory;
    private RPGMain instance;

    private Enchantment currentEnchant;

    public SetLevelInventory(Player player, EnchantInventory enchantInventory, RPGMain instance){
        this.player = player;
        this.enchantInventory = enchantInventory;
        this.instance = instance;
        //level = this;
    }

    public void setItem(){
        ItemStack item = new ItemStack(Material.STAINED_GLASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("1강 감소");
        item.setItemMeta(meta);
        setEnchantLevel.setItem(11, item);

        item = new ItemStack(Material.GOLD_BLOCK);
        meta = item.getItemMeta();
        meta.setDisplayName(this.currentEnchant.getName() + ": +" + 1 + "강");
        item.setItemMeta(meta);
        setEnchantLevel.setItem(13, item);

        item = new ItemStack(Material.STAINED_GLASS);
        meta = item.getItemMeta();
        meta.setDisplayName("1강 증가");
        item.setItemMeta(meta);
        setEnchantLevel.setItem(15, item);

        item = new ItemStack(Material.GREEN_GLAZED_TERRACOTTA);
        meta = item.getItemMeta();
        meta.setDisplayName("확인");
        item.setItemMeta(meta);
        setEnchantLevel.setItem(21, item);

        item = new ItemStack(Material.RED_GLAZED_TERRACOTTA);
        meta = item.getItemMeta();
        meta.setDisplayName("취소");
        item.setItemMeta(meta);
        setEnchantLevel.setItem(23, item);
    }

    public void setCurrentEnchant(Enchantment enchant){
        this.currentEnchant = enchant;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(!(e.getPlayer() instanceof  Player)){
            return;
        }

        if(e.getInventory().equals(setEnchantLevel)){
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

        if (e.getClickedInventory().equals(setEnchantLevel)) {
            e.setCancelled(true);
            int currentEnchant = setEnchantLevel.getItem(13).getAmount();

            if(e.getSlot() == 11){
                if(currentEnchant <= 1){
                    player.sendMessage("더 이상 줄일 수 없습니다!");
                } else{
                    setEnchantLevel.getItem(13).setAmount(currentEnchant - 1);
                    ItemMeta meta = setEnchantLevel.getItem(13).getItemMeta();
                    meta.setDisplayName(this.currentEnchant + ": +" + (currentEnchant - 1) + "강");
                    setEnchantLevel.getItem(13).setItemMeta(meta);
                }
            } else if(e.getSlot() == 15){
                if(currentEnchant >= 4){
                    player.sendMessage("더 이상 늘릴 수 없습니다!");
                } else{
                    setEnchantLevel.getItem(13).setAmount(currentEnchant + 1);
                    ItemMeta meta = setEnchantLevel.getItem(13).getItemMeta();
                    meta.setDisplayName(this.currentEnchant + ": +" + (currentEnchant + 1) + "강");
                    setEnchantLevel.getItem(13).setItemMeta(meta);
                }
            } else if(e.getSlot() == 21){
                confirm(currentEnchant);
                player.closeInventory();
                enchantInventory.openInventory();
            } else if(e.getSlot() == 23){
                player.closeInventory();
                enchantInventory.openInventory();
            }
        }
    }

    public void openInventory(){
        setItem();
        Bukkit.getPluginManager().registerEvents(this, instance);
        player.openInventory(setEnchantLevel);
    }

    public void confirm(int plusEnchant){
        enchantInventory.putEnchant(currentEnchant, plusEnchant);
    }
}
