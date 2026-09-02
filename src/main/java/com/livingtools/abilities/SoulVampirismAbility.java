package com.livingtools.abilities;

import com.livingtools.data.LivingTool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class SoulVampirismAbility extends Ability implements org.bukkit.event.Listener {

    public SoulVampirismAbility() {
        super("soul_vampirism", "Vampirismo de Almas", "Probabilidad de reparar al matar.", 15, 7500);
    }

    @Override
    public boolean isCompatible(Material type) {
        return type.name().contains("SWORD") || type.name().contains("AXE");
    }

    @Override
    public List<String> getIncompatibleAbilities() {
        return Arrays.asList("self_repair"); // Incompatible with Auto-Repair
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (LivingTool.isLivingTool(item)) {
                LivingTool tool = new LivingTool(item);
                if (tool.hasAbility(getId()) && tool.getData().isAbilityActive(getId())) {
                    // 20% Chance
                    if (Math.random() < 0.20) {
                        repairItem(player, item, tool);
                    }
                }
            }
        }
    }

    private void repairItem(Player player, ItemStack item, LivingTool tool) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable))
            return;

        Damageable damageable = (Damageable) meta;
        if (damageable.getDamage() > 0) {
            int repairAmount = 5; // Fixed amount
            int newDamage = Math.max(0, damageable.getDamage() - repairAmount);

            damageable.setDamage(newDamage);
            item.setItemMeta((ItemMeta) damageable);

            if (tool.isBroken()) {
                tool.setBroken(false);
                player.sendMessage(ChatColor.DARK_PURPLE + "Tu arma se ha alimentado de un alma y ha revivido.");
            }

            player.playSound(player.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 0.5f, 1);
        }
    }
}
