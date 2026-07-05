package io.ncbpfluffybear.slimecustomizer.registration;

import dev.j3fftw.extrautils.utils.LoreBuilderDynamic;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.Capacitor;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import io.ncbpfluffybear.slimecustomizer.SlimeCustomizer;
import io.ncbpfluffybear.slimecustomizer.Utils;
import io.ncbpfluffybear.slimecustomizer.objects.CustomMaterialGenerator;
import net.guizhanss.slimecustomizer.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaterialGenerators {

    public static boolean register(Config materialGenerators) {
        for (String genKey : materialGenerators.getKeys()) {
            if (genKey.equals("EXAMPLE_MATERIAL_GENERATOR")) {
                SlimeCustomizer.getInstance().getLogger().log(Level.WARNING, "material-generators.yml still contains the example material generator! " +
                    "Did you forget to configure it?");
            }

            ItemGroup category = Utils.getCategory(materialGenerators.getString(genKey + ".category"), genKey);
            if (category == null) {
                return false;
            }

            SlimefunItemStack tempStack;
            ItemStack block = null;
            int amount = materialGenerators.getOrSetDefault(genKey + ".item-amount", 1);

            if (amount < 1) {
                Utils.disable(genKey + "'s item-amount must be a positive integer!");
                return false;
            }

            int energyConsumption, energyBuffer, tickRate;
            var cEnergyConsumption = NumberUtils.getConfigInt(
                materialGenerators.getString(genKey + ".stats.energy-consumption"),
                val -> val >= 0
            );
            var cEnergyBuffer = NumberUtils.getConfigInt(
                materialGenerators.getString(genKey + ".stats.energy-buffer"),
                val -> val >= 0
            );
            var cTickRate = NumberUtils.getConfigInt(
                materialGenerators.getString(genKey + ".output.tick-rate"),
                val -> val > 0
            );

            if (cEnergyConsumption.isEmpty()) {
                Utils.disable(genKey + "'s stats.energy-consumption must be an integer greater than or equal to 0!");
                return false;
            } else {
                energyConsumption = cEnergyConsumption.get();
            }
            if (cEnergyBuffer.isEmpty()) {
                Utils.disable(genKey + "'s stats.energy-buffer must be an integer greater than or equal to 0!");
                return false;
            } else {
                energyBuffer = cEnergyBuffer.get();
            }
            if (cTickRate.isEmpty()) {
                Utils.disable(genKey + "'s output.tick-rate must be a positive integer!");
                return false;
            } else {
                tickRate = cTickRate.get();
            }

            block = Utils.getBlockFromConfig(genKey, materialGenerators.getString(genKey + ".block-type"));
            if (block == null) {return false;}

            // Building lore
            List<String> itemLore = Utils.colorList(Stream.concat(
                materialGenerators.getStringList(genKey + ".item-lore").stream(),
                Stream.of(
                    "",
                    "&eMaterial Generator",
                    "&8⇨ &7Speed: &bevery " + tickRate + " Slimefun ticks",
                    LoreBuilderDynamic.powerBuffer(energyBuffer),
                    LoreBuilderDynamic.powerPerSecond(energyConsumption)
                )
            ).collect(Collectors.toList()));

            tempStack = new SlimefunItemStack(genKey, block, materialGenerators.getString(genKey + ".item-name"));

            // Adding lore
            ItemMeta tempMeta = tempStack.getItemMeta();
            tempMeta.setLore(itemLore);
            tempStack.setItemMeta(tempMeta);

            String recipeTypeString = materialGenerators.getString(genKey + ".crafting-recipe-type");
            RecipeType recipeType = Utils.getRecipeType(recipeTypeString, genKey);
            if (recipeType == null) {
                Utils.disable(genKey + "'s crafting-recipe-type is invalid! Please check the wiki.");
                return false;
            }

            /* Crafting recipe */
            ItemStack[] recipe = Utils.buildCraftingRecipe(materialGenerators, genKey, recipeType);
            if (recipe == null) {return false;}

            int outputAmount = materialGenerators.getOrSetDefault(genKey + ".output.amount", 1);

            if (outputAmount < 1) {
                Utils.disable(genKey + "'s output.amount must be a positive integer!");
                return false;
            }

            ItemStack output;
            String outputType = materialGenerators.getString(genKey + ".output.type");
            String outputMaterial = materialGenerators.getString(genKey + ".output.id");
            if (outputType.equalsIgnoreCase("VANILLA")) {
                Material vanillaMat = Material.getMaterial(outputMaterial);
                if (vanillaMat == null) {
                    Utils.disable(genKey + "'s output item id is not a valid vanilla item ID!");
                    return false;
                } else {
                    output = new ItemStack(vanillaMat, outputAmount);
                }
            } else if (outputType.equalsIgnoreCase("SLIMEFUN")) {
                SlimefunItem sfMat = SlimefunItem.getById(outputMaterial);
                if (sfMat == null) {
                    Utils.disable(genKey + "'s output item id is not a valid Slimefun item ID!");
                    return false;
                } else {
                    output = new CustomItemStack(sfMat.getItem().clone(), outputAmount);
                }
            } else if (outputType.equalsIgnoreCase("SAVEDITEM")) {
                output = Utils.retrieveSavedItem(outputMaterial, outputAmount, true);
            } else {
                Utils.disable(genKey + "'s output item type can only be: VANILLA, SLIMEFUN, or SAVEDITEM!");
                return false;
            }

            CustomMaterialGenerator matGen = new CustomMaterialGenerator(category, tempStack, recipeType, recipe,
                tickRate, output);
            matGen.setEnergyPerTick(energyConsumption);
            matGen.setEnergyCapacity(energyBuffer);
            matGen.register(SlimeCustomizer.getInstance());

            Utils.notify("Registered material generator " + genKey + "!");
        }

        return true;
    }
}
