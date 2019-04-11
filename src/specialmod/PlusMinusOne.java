package specialmod;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rpgmain.RPGMain;
import scroll.Scroll;

import java.util.Random;

public class PlusMinusOne implements Mod{
    private RPGMain instance;

    public PlusMinusOne(RPGMain instance){
        this.instance = instance;
    }

    public void onSuccess(Player player, ItemStack enchanted, Scroll scroll){
        Random random = new Random();

        for(Enchantment e : scroll.getEnchant().keySet()){
            int i = random.nextInt(100);

            if(i >= 0 && i < scroll.getModSuccess()){
                up(player, enchanted, e);
            } else if(i >= scroll.getModSuccess() && i < 100){
                down(player, enchanted, e);
            }
        }
    }

    public void up(Player player, ItemStack enchanted, Enchantment enchantment){
            int currentLevel = 0;

            if(enchanted.containsEnchantment(enchantment)){
                currentLevel = enchanted.getEnchantmentLevel(enchantment);
            }

            enchanted.addUnsafeEnchantment(enchantment, currentLevel + 1);

            String name = instance.getUtil().getLocalNameByEnchant(enchantment);
            player.sendMessage(name + "인챈트가 성공하였습니다!");
    }

    public void down(Player player, ItemStack enchanted, Enchantment enchantment){
            int currentLevel = 0;

            if(enchanted.containsEnchantment(enchantment)){
                currentLevel = enchanted.getEnchantmentLevel(enchantment);
            }

            if(currentLevel > 1){
                enchanted.addUnsafeEnchantment(enchantment, currentLevel -1);
            } else if(currentLevel >= 1){
                enchanted.removeEnchantment(enchantment);
            }

        String name = instance.getUtil().getLocalNameByEnchant(enchantment);
        player.sendMessage(name + "인챈트가 실패하였습니다!");
    }

    public String toString(){
        return "plusminus";
    }
}
