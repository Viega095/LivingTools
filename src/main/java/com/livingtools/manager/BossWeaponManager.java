package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Armas finales de jefe — equipables con PDC y efectos modestos.
 */
public final class BossWeaponManager {

    private static NamespacedKey weaponKey() {
        return new NamespacedKey(LivingToolsPlugin.getInstance(), "boss_final_weapon");
    }

    public enum FinalWeapon {
        VERDANT_STAFF("verdant_staff", Material.STICK, "&2&lBastón Ancestral del Bosque",
                "Canaliza la vida del bosque. Regeneración en biomas forestales.",
                Collections.singletonList(Enchantment.DURABILITY)),
        SANDSTORM_FANG("sandstorm_fang", Material.GOLDEN_SWORD, "&6&lColmillo Real del Wyrm",
                "Filosa recompensa del desierto. Fuego y empuje en arenas.",
                Arrays.asList(Enchantment.DURABILITY, Enchantment.FIRE_ASPECT)),
        ABYSS_ANCHOR("abyss_anchor", Material.TRIDENT, "&3&lAncla del Abismo",
                "Dominio de las profundidades. Lealtad y fuerza bajo el agua.",
                Arrays.asList(Enchantment.DURABILITY, Enchantment.LOYALTY));

        private final String id;
        private final Material material;
        private final String displayName;
        private final String flavor;
        private final List<Enchantment> enchantments;

        FinalWeapon(String id, Material material, String displayName, String flavor, List<Enchantment> enchantments) {
            this.id = id;
            this.material = material;
            this.displayName = displayName;
            this.flavor = flavor;
            this.enchantments = enchantments;
        }

        public Material getMaterial() {
            return material;
        }
    }

    private BossWeaponManager() {
    }

    public static ItemStack create(FinalWeapon weapon) {
        ItemStack item = new ItemStack(weapon.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(MessageUtils.color(weapon.displayName));
        meta.setLore(Arrays.asList(
                MessageUtils.color("&7" + weapon.flavor),
                MessageUtils.color("&8Arma legendaria de jefe")));
        for (Enchantment enchantment : weapon.enchantments) {
            int level = enchantment == Enchantment.LOYALTY ? 2 : 1;
            meta.addEnchant(enchantment, level, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(weaponKey(), PersistentDataType.STRING, weapon.id);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean is(FinalWeapon weapon, ItemStack item) {
        if (item == null || item.getType() != weapon.material || !item.hasItemMeta()) {
            return false;
        }
        String stored = item.getItemMeta().getPersistentDataContainer().get(weaponKey(), PersistentDataType.STRING);
        return weapon.id.equals(stored);
    }

    public static FinalWeapon fromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String stored = item.getItemMeta().getPersistentDataContainer().get(weaponKey(), PersistentDataType.STRING);
        if (stored == null) {
            return null;
        }
        for (FinalWeapon weapon : FinalWeapon.values()) {
            if (weapon.id.equals(stored)) {
                return weapon;
            }
        }
        return null;
    }
}
