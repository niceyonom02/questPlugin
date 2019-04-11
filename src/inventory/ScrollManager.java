package inventory;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import rpgmain.RPGMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import scroll.Scroll;
import specialmod.Mod;
import specialmod.PlusMinusOne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class ScrollManager implements Listener {
    private ArrayList<Scroll> scrolls = new ArrayList<>();
    private RPGMain instance;
    private int tempTask;

    public ScrollManager(RPGMain instance){
        this.instance = instance;
    }

    public void registerScrollByPlayer(Player player, Scroll scroll){
            scrolls.add(scroll);
            scroll.saveScroll();
            player.getInventory().addItem(scroll.getItemStack());
            player.sendMessage(instance.getUtil().getConfigMessage("registeredScroll"));
    }

    private void registerScrollByconsole(Scroll scroll){
        if(!scrolls.contains(scroll)){
            scrolls.add(scroll);
        }
    }

    public boolean isNameDuplicate(String name){
        for(Scroll scroll : scrolls){
            String existName = scroll.getName();
            if(name.equalsIgnoreCase(existName)){
                return true;
            }
        }
        return false;
    }

    public void clearScrolls(){
        scrolls.clear();
    }

    //TODO
    public void showScrollList(Player player){
        for(int i = 0; i < scrolls.size(); i++){
            player.sendMessage("[" + (i+1) + "] :: "+ scrolls.get(i).getBriefDescription());
        }
    }

    public Scroll findScroll(ItemStack itemStack){
        for(Scroll scroll : scrolls){
            if(scroll.getItemStack().equals(itemStack)){
                return scroll;
            }
        }
        return null;
    }

    public void makeScroll(Player player){
        ScrollMaker newScroll = new ScrollMaker(player, instance, this);
        newScroll.openInventory();
    }

    //TODO SaveScroll
    public void saveScroll(){
        for(Scroll sr : scrolls){
            sr.saveScroll();
        }
    }

    public void loadScroll(){
        LinkedHashMap<Enchantment, Integer> tempEnchant;
        if(instance.getConfig().getConfigurationSection("scroll") != null){
            for(String scrollName : instance.getConfig().getConfigurationSection("scroll").getKeys(false)){
                tempEnchant = new LinkedHashMap<>();

                if(instance.getConfig().getConfigurationSection("scroll." + scrollName + ".enchant") != null){
                    for(String enchant : instance.getConfig().getConfigurationSection("scroll." + scrollName + ".enchant").getKeys(false)){
                        tempEnchant.put(findEnchant(enchant), instance.getConfig().getConfigurationSection("scroll." + scrollName + ".enchant").getInt(enchant));
                    }
                }
                int successRate = instance.getConfig().getInt("scroll." + scrollName + ".successRate");
                int failRate = instance.getConfig().getInt("scroll." + scrollName + ".failRate");
                int keepRate = instance.getConfig().getInt("scroll." + scrollName + ".keepRate");
                int brokenRate = instance.getConfig().getInt("scroll." + scrollName + ".brokenRate");
                Mod mod = findMod(instance.getConfig().getString("scroll." + scrollName + ".mod"));

                Scroll tempScroll = new Scroll(instance);

                if(mod == null){
                    tempScroll.implementScroll(scrollName, tempEnchant, successRate, failRate, keepRate, brokenRate, null, null);
                } else{
                    tempScroll.implementScroll(scrollName, tempEnchant, successRate, failRate, keepRate, brokenRate, null, mod);
                }
                registerScrollByconsole(tempScroll);
            }
        }
    }

    public Mod findMod(String name){
        if(name == null){
            return null;
        }

        if(name.equalsIgnoreCase("plusminus")){
            return new PlusMinusOne(instance);
        }

        return null;
    }

    public void deleteScroll(Player player, int seenNumber){
        int index = seenNumber -1;
        try{
            Scroll scroll = scrolls.get(index);
            instance.getConfig().set("scroll." + scroll.getName(), null);
            instance.saveConfig();
            scrolls.remove(index);
            player.sendMessage(instance.getUtil().getConfigMessage("deletedScroll").replaceAll("%scrollName%", scroll.getName()));
        }catch(IndexOutOfBoundsException e){
            player.sendMessage(instance.getUtil().getConfigMessage("noSuchIndexScroll"));
        }
    }

    public ArrayList<Scroll> getScrollList(){
        return scrolls;
    }

    public void givePlayerScroll(Player player, int seenNumber){
        int index = seenNumber -1;
        try{
            Scroll scroll = scrolls.get(index);
            player.getInventory().addItem(scroll.getItemStack());
        }catch(IndexOutOfBoundsException e){
            player.sendMessage(instance.getUtil().getConfigMessage("noSuchIndexScroll"));
        }
    }

    public void clear(){
        this.scrolls.clear();
    }

    public Enchantment findEnchant(String id){
        for(Enchantment en : Enchantment.values()){
            if(en.getName().equalsIgnoreCase(id)){
                return en;
            }
        }
        return null;
    }

  /**  @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getCursor().getType().equals(Material.DIAMOND)){
            Bukkit.getLogger().info("다이아몬드 작동");
            if(e.getClickedInventory().getItem(e.getSlot()).getType().equals(Material.GOLD_BLOCK)){
                Bukkit.getLogger().info("금블럭 작동");
                e.getInventory().remove(e.getCursor());
                e.getClickedInventory().getItem(e.getSlot()).setAmount(2);
                e.getWhoClicked().openInventory(Bukkit.createInventory(null, 0, "TEST"));
                e.setCancelled(true);
            }
        }
    } */
    

}
