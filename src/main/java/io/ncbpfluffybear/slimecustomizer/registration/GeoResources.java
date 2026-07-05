package io.ncbpfluffybear.slimecustomizer.registration;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.slimecustomizer.SlimeCustomizer;
import io.ncbpfluffybear.slimecustomizer.Utils;
import io.ncbpfluffybear.slimecustomizer.objects.SCGeoResource;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * {@link GeoResources} registers the geo resources.
 *
 * @author ybw0014
 */
public class GeoResources {

    public static boolean register(Config geoResources) {
        for (String geoKey : geoResources.getKeys()) {
            if (geoKey.equals("EXAMPLE_GEO")) {
                SlimeCustomizer.getInstance().getLogger().log(Level.WARNING, "geo-resources.yml still contains the example configuration! " +
                    "Did you forget to configure it?");
            }

            String itemType = geoResources.getString(geoKey + ".item-type");

            ItemGroup category = Utils.getCategory(geoResources.getString(geoKey + ".category"), geoKey);
            if (category == null) {return false;}

            String itemId = geoResources.getString(geoKey + ".item-id");
            SlimefunItemStack tempStack;
            ItemStack item = null;
            int maxDeviation = geoResources.getOrSetDefault(geoKey + ".max-deviation", 0);

            if (itemType == null) {
                Utils.disable(geoKey + " has not set item-type!");
                return false;
            }
            if (itemId == null) {
                Utils.disable(geoKey + " has not set item-id!");
                return false;
            }
            if (maxDeviation < 0) {
                Utils.disable(geoKey + "'s max-deviation cannot be less than 0!");
                return false;
            }

            if (itemType.equalsIgnoreCase("CUSTOM")) {

                Material material = Material.getMaterial(itemId);

                /* Item material type */
                if (material == null && !itemId.startsWith("SKULL")) {
                    Utils.disable(geoKey + "'s item-id is invalid!");
                    return false;
                } else if (material != null) {
                    item = new ItemStack(material);
                } else if (itemId.startsWith("SKULL")) {
                    item = SlimefunUtils.getCustomHead(itemId.replace("SKULL", ""));
                }

                // Building lore
                List<String> itemLore = Utils.colorList(geoResources.getStringList(geoKey + ".item-lore"));

                tempStack = new SlimefunItemStack(geoKey, item, geoResources.getString(geoKey + ".item-name"));

                // Adding lore
                ItemMeta tempMeta = tempStack.getItemMeta();
                tempMeta.setLore(itemLore);
                tempStack.setItemMeta(tempMeta);
            } else if (itemType.equalsIgnoreCase("SAVEDITEM")) {
                item = Utils.retrieveSavedItem(itemId, 1, true);
                if (item == null) {return false;}

                tempStack = new SlimefunItemStack(geoKey, item);
            } else {
                Utils.disable(geoKey + "'s item-type can only be CUSTOM or SAVEDITEM!");
                return false;
            }

            // Load biome map (not really)
            Map<Biome, Integer> biomeMap = new HashMap<>();
            Map<World.Environment, Integer> environmentMap = new HashMap<>();
            ConfigurationSection biomes = geoResources.getConfiguration().getConfigurationSection(geoKey + ".biome");
            ConfigurationSection environments = geoResources.getConfiguration().getConfigurationSection(geoKey + ".environment");
            if (biomes == null && environments == null) {
                Utils.disable(geoKey + " has no biome or environment configured!");
                return false;
            }

            if (biomes != null) {
                for (String biomeKey : biomes.getKeys(false)) {
                    Biome biome;
                    int amount;

                    try {
                        biome = Biome.valueOf(biomeKey);
                    } catch (IllegalArgumentException ex) {
                        Utils.disable(geoKey + "'s biome " + biomeKey + " is not a valid biome!");
                        return false;
                    }

                    try {
                        amount = Integer.parseInt(biomes.getString(biomeKey));
                    } catch (NumberFormatException ex) {
                        Utils.disable(geoKey + "'s biome " + biomeKey + " has an invalid amount!");
                        return false;
                    }

                    if (amount < 0) {
                        Utils.disable(geoKey + "'s biome " + biomeKey + " amount cannot be negative!");
                        return false;
                    }

                    biomeMap.put(biome, amount);
                }
            }

            if (environments != null) {
                for (String environmentKey : environments.getKeys(false)) {
                    World.Environment environment;
                    int amount;

                    try {
                        environment = World.Environment.valueOf(environmentKey);
                    } catch (IllegalArgumentException ex) {
                        Utils.disable(geoKey + "'s environment " + environmentKey + " is not a valid environment!");
                        return false;
                    }

                    try {
                        amount = Integer.parseInt(environments.getString(environmentKey));
                    } catch (NumberFormatException ex) {
                        Utils.disable(geoKey + "'s environment " + environmentKey + " has an invalid amount!");
                        return false;
                    }

                    if (amount < 0) {
                        Utils.disable(geoKey + "'s environment " + environmentKey + " amount cannot be negative!");
                        return false;
                    }

                    environmentMap.put(environment, amount);
                }
            }

            if (itemType.equalsIgnoreCase("CUSTOM")) {
                new SCGeoResource(category, tempStack, maxDeviation, biomeMap, environmentMap
                ).registerGeo(SlimeCustomizer.getInstance());
            } else {
                new SCGeoResource(category, tempStack, item, maxDeviation, biomeMap, environmentMap
                ).registerGeo(SlimeCustomizer.getInstance());
            }

            Utils.notify("Registered GEO resource " + geoKey + "!");
        }

        return true;
    }

}
