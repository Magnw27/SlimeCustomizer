package io.ncbpfluffybear.slimecustomizer.registration;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.ncbpfluffybear.slimecustomizer.SlimeCustomizer;
import io.ncbpfluffybear.slimecustomizer.Utils;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Researches {
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    public static boolean register(Config researches) {
        if (researches.getKeys().isEmpty()) {
            return true;
        }

        for (String researchKey : researches.getKeys()) {
            if (researchKey.equals("example_research")) {
                SlimeCustomizer.getInstance().getLogger().log(Level.WARNING, "researches.yml still contains the example research! " +
                    "Did you forget to configure it?");
            }

            if (!VALID_KEY.matcher(researchKey).matches()) {
                Utils.disable("Research " + researchKey + "'s ID is invalid, only [a-z0-9._-] are allowed.");
                return false;
            }

            int researchId = researches.getInt(researchKey + ".id");
            String name = researches.getString(researchKey + ".name");
            int cost = researches.getInt(researchKey + ".cost");
            List<String> items = researches.getStringList(researchKey + ".items");
            if (researchId <= 0) {
                Utils.disable("Research " + researchKey + "'s id must be greater than 0!");
                return false;
            }
            if (cost <= 0) {
                Utils.disable("Research " + researchKey + "'s cost must be greater than 0!");
                return false;
            }
            if (name == null) {
                Utils.disable("Research " + researchKey + "'s name cannot be empty!");
                return false;
            }

            Research research = new Research(new NamespacedKey(SlimeCustomizer.getInstance(), researchKey),
                researchId, name, cost);
            for (String itemId : items) {
                SlimefunItem sfItem = SlimefunItem.getById(itemId);
                if (sfItem == null) {
                    Utils.disable("Research " + researchKey + "'s item " + itemId + " is not a Slimefun item!");
                    return false;
                }
                research.addItems(sfItem);
            }

            research.register();

            Utils.notify("Registered research " + researchKey + "!");

        }

        return true;
    }
}
