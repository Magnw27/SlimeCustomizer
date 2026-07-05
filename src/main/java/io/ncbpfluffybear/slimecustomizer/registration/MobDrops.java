package io.ncbpfluffybear.slimecustomizer.registration;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.slimecustomizer.SlimeCustomizer;
import io.ncbpfluffybear.slimecustomizer.Utils;
import io.ncbpfluffybear.slimecustomizer.objects.SCMobDrop;
import net.guizhanss.guizhanlib.minecraft.helper.entity.EntityTypeHelper;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.logging.Level;

/**
 * {@link MobDrops} registers the mob drops
 * in the mob-drops config file.
 *
 * @author NCBPFluffyBear
 */
public class MobDrops {

    public static boolean register(Config drops) {
        for (String dropKey : drops.getKeys()) {
            if (dropKey.equals("EXAMPLE_DROP")) {
                SlimeCustomizer.getInstance().getLogger().log(Level.WARNING, "mob-drops.yml still contains the example configuration! " +
                    "Did you forget to configure it?");
            }

            String itemType = drops.getString(dropKey + ".item-type");

            ItemGroup category = Utils.getCategory(drops.getString(dropKey + ".category"), dropKey);
            if (category == null) {return false;}

            String materialString = drops.getString(dropKey + ".item-id");
            SlimefunItemStack tempStack;
            ItemStack item = null;
            int amount = drops.getOrSetDefault(dropKey + ".item-amount", 1);
            int chance = drops.getOrSetDefault(dropKey + ".chance", 100);

            if (itemType == null) {
                Utils.disable(dropKey + " has not set item-type!");
                return false;
            }
            if (materialString == null) {
                Utils.disable(dropKey + " has not set item-id!");
                return false;
            }
            if (amount < 1) {
                Utils.disable(dropKey + "'s item-amount must be a positive integer!");
                return false;
            }
            if (chance < 0 || chance > 100) {
                Utils.disable(dropKey + "'s chance must be between 0-100!");
                return false;
            }

            if (itemType.equalsIgnoreCase("CUSTOM")) {

                Material material = Material.getMaterial(materialString);

                /* Item material type */
                if (material == null && !materialString.startsWith("SKULL")) {
                    Utils.disable(dropKey + "'s item-id is invalid!");
                    return false;
                } else if (material != null) {
                    item = new ItemStack(material);
                } else if (materialString.startsWith("SKULL")) {
                    item = SlimefunUtils.getCustomHead(materialString.replace("SKULL", "").toLowerCase());
                }

                item.setAmount(amount);

                // Building lore
                List<String> itemLore = Utils.colorList(drops.getStringList(dropKey + ".item-lore"));

                tempStack = new SlimefunItemStack(dropKey, item, drops.getString(dropKey + ".item-name"));

                // Adding lore
                ItemMeta tempMeta = tempStack.getItemMeta();
                tempMeta.setLore(itemLore);
                tempStack.setItemMeta(tempMeta);
            } else if (itemType.equalsIgnoreCase("SAVEDITEM")) {
                item = Utils.retrieveSavedItem(materialString, amount, true);
                if (item == null) {return false;}

                tempStack = new SlimefunItemStack(dropKey, item);
            } else {
                Utils.disable(dropKey + "'s item-type can only be CUSTOM or SAVEDITEM!");
                return false;
            }

            // Get mob type that drops the item
            String mobType = drops.getString(dropKey + ".mob");
            EntityType mob;
            String egg = drops.getString(dropKey + ".recipe-display-item");
            Material eggMaterial = Material.getMaterial(egg);

            if (mobType == null) {
                Utils.disable(dropKey + "'s mob is not a valid entity type!");
                return false;
            }

            try {
                mob = EntityType.valueOf(mobType);
            } catch (IllegalArgumentException e) {
                Utils.disable(dropKey + "'s mob is not a valid entity type!");
                return false;
            }

            if (mob == EntityType.UNKNOWN) {
                Utils.disable(dropKey + "'s mob is not a valid entity type!");
                return false;
            }

            if (eggMaterial == null) {
                Utils.disable(dropKey + "'s recipe-display-item is not a valid vanilla item ID!");
                return false;
            }

            /* Crafting recipe */
            ItemStack[] recipe = new ItemStack[] {
                    null, null, null,
                    null, new CustomItemStack(eggMaterial, "&b" + Utils.capitalize(mobType), "&7Kill " + EntityTypeHelper.getName(mob))
            };

            if (itemType.equalsIgnoreCase("CUSTOM")) {
                new SCMobDrop(category, tempStack, RecipeType.MOB_DROP, recipe, chance
                ).register(SlimeCustomizer.getInstance());
            } else {
                new SCMobDrop(category, tempStack, RecipeType.MOB_DROP, recipe, item, chance
                ).register(SlimeCustomizer.getInstance());
            }

            Utils.notify("Registered mob drop item " + dropKey + "!");
        }

        return true;
    }

}
