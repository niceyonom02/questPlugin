package specialmod;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import scroll.Scroll;

public interface Mod {
    void onSuccess(Player player, ItemStack enchanted, Scroll scroll);
}
