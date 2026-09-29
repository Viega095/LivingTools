package com.livingtools.manager;

import com.livingtools.LivingToolsPlugin;
import com.livingtools.utils.ItemSerializer;
import com.livingtools.visuals.SoundHarmonicsEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SoulGuildManager — Sistema de Hermandades de Almas, Bóveda Compartida y Progresión Grupal.
 */
public class SoulGuildManager {

    public static final int MAX_MEMBERS = 8;
    private static final Map<String, SoulGuild> guildsByName = new ConcurrentHashMap<>();
    private static final Map<UUID, SoulGuild> playerGuildMap = new ConcurrentHashMap<>();
    private static final Map<UUID, String> pendingInvites = new ConcurrentHashMap<>();
    private static File guildFile;
    private static FileConfiguration guildConfig;

    public static class SoulGuild {
        private final String name;
        private UUID leader;
        private final Set<UUID> members = new HashSet<>();
        private int level;
        private long guildXp;
        private ItemStack[] vaultContents;

        public SoulGuild(String name, UUID leader) {
            this.name = name;
            this.leader = leader;
            this.members.add(leader);
            this.level = 1;
            this.guildXp = 0;
            this.vaultContents = new ItemStack[54];
        }

        public String getName() { return name; }
        public UUID getLeader() { return leader; }
        public void setLeader(UUID leader) { this.leader = leader; }
        public Set<UUID> getMembers() { return members; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public long getGuildXp() { return guildXp; }
        public void setGuildXp(long guildXp) { this.guildXp = guildXp; }
        public ItemStack[] getVaultContents() { return vaultContents; }
        public void setVaultContents(ItemStack[] vaultContents) { this.vaultContents = vaultContents; }
    }

    public static void init() {
        guildFile = new File(LivingToolsPlugin.getInstance().getDataFolder(), "guilds.yml");
        if (!guildFile.exists()) {
            try {
                guildFile.getParentFile().mkdirs();
                guildFile.createNewFile();
            } catch (IOException e) {
                LivingToolsPlugin.getInstance().getLogger().severe("No se pudo crear guilds.yml: " + e.getMessage());
            }
        }
        load();
    }

    public static void load() {
        guildsByName.clear();
        playerGuildMap.clear();
        guildConfig = YamlConfiguration.loadConfiguration(guildFile);

        ConfigurationSection section = guildConfig.getConfigurationSection("guilds");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name", key);
            String leaderStr = section.getString(key + ".leader");
            if (leaderStr == null) continue;

            UUID leader = UUID.fromString(leaderStr);
            SoulGuild guild = new SoulGuild(name, leader);
            guild.setLevel(section.getInt(key + ".level", 1));
            guild.setGuildXp(section.getLong(key + ".xp", 0L));

            List<String> memberList = section.getStringList(key + ".members");
            guild.getMembers().clear();
            guild.getMembers().add(leader);
            for (String m : memberList) {
                try {
                    guild.getMembers().add(UUID.fromString(m));
                } catch (IllegalArgumentException ignored) {}
            }

            String vaultBase64 = section.getString(key + ".vault");
            if (vaultBase64 != null && !vaultBase64.isEmpty()) {
                try {
                    guild.setVaultContents(ItemSerializer.fromBase64(vaultBase64));
                } catch (Exception e) {
                    guild.setVaultContents(new ItemStack[54]);
                }
            }

            guildsByName.put(name.toLowerCase(), guild);
            for (UUID member : guild.getMembers()) {
                playerGuildMap.put(member, guild);
            }
        }
    }

    public static void save() {
        if (guildFile == null) return;
        guildConfig = new YamlConfiguration();

        for (SoulGuild guild : guildsByName.values()) {
            String path = "guilds." + guild.getName().toLowerCase();
            guildConfig.set(path + ".name", guild.getName());
            guildConfig.set(path + ".leader", guild.getLeader().toString());
            guildConfig.set(path + ".level", guild.getLevel());
            guildConfig.set(path + ".xp", guild.getGuildXp());

            List<String> memberStrings = new ArrayList<>();
            for (UUID m : guild.getMembers()) {
                memberStrings.add(m.toString());
            }
            guildConfig.set(path + ".members", memberStrings);

            if (guild.getVaultContents() != null) {
                try {
                    guildConfig.set(path + ".vault", ItemSerializer.toBase64(guild.getVaultContents()));
                } catch (Exception ignored) {}
            }
        }

        try {
            guildConfig.save(guildFile);
        } catch (IOException e) {
            LivingToolsPlugin.getInstance().getLogger().severe("Error al guardar guilds.yml: " + e.getMessage());
        }
    }

    public static boolean createGuild(Player player, String name) {
        if (getGuild(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Ya perteneces a una Hermandad de Almas.");
            return false;
        }

        if (name == null || name.length() < 3 || name.length() > 16 || !name.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage(ChatColor.RED + "El nombre debe tener entre 3 y 16 caracteres alfanuméricos.");
            return false;
        }

        if (guildsByName.containsKey(name.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "Ya existe una Hermandad con ese nombre.");
            return false;
        }

        SoulGuild guild = new SoulGuild(name, player.getUniqueId());
        guildsByName.put(name.toLowerCase(), guild);
        playerGuildMap.put(player.getUniqueId(), guild);
        save();

        player.sendMessage(ChatColor.GREEN + "✦ ¡Hermandad de Almas '" + ChatColor.GOLD + name + ChatColor.GREEN + "' fundada con éxito!");
        SoundHarmonicsEngine.playVictoryFanfare(player);
        return true;
    }

    public static boolean inviteMember(Player leader, Player target) {
        SoulGuild guild = getGuild(leader.getUniqueId());
        if (guild == null || !guild.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Solo el Líder de la Hermandad puede invitar nuevos miembros.");
            return false;
        }

        if (guild.getMembers().size() >= MAX_MEMBERS) {
            leader.sendMessage(ChatColor.RED + "La Hermandad ya alcanzó el límite máximo de " + MAX_MEMBERS + " miembros.");
            return false;
        }

        if (getGuild(target.getUniqueId()) != null) {
            leader.sendMessage(ChatColor.RED + target.getName() + " ya pertenece a una Hermandad.");
            return false;
        }

        pendingInvites.put(target.getUniqueId(), guild.getName());
        leader.sendMessage(ChatColor.GREEN + "Has invitado a " + target.getName() + " a la hermandad.");
        target.sendMessage(ChatColor.GOLD + "✦ Has sido invitado a la Hermandad de Almas '" + ChatColor.WHITE + guild.getName()
                + ChatColor.GOLD + "'. Usa " + ChatColor.YELLOW + "/lt guild accept" + ChatColor.GOLD + " para unirte.");
        return true;
    }

    public static boolean acceptInvite(Player player) {
        String guildName = pendingInvites.remove(player.getUniqueId());
        if (guildName == null) {
            player.sendMessage(ChatColor.RED + "No tienes ninguna invitación de Hermandad pendiente.");
            return false;
        }

        SoulGuild guild = guildsByName.get(guildName.toLowerCase());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "La Hermandad ya no existe.");
            return false;
        }

        if (guild.getMembers().size() >= MAX_MEMBERS) {
            player.sendMessage(ChatColor.RED + "La Hermandad ya está llena.");
            return false;
        }

        guild.getMembers().add(player.getUniqueId());
        playerGuildMap.put(player.getUniqueId(), guild);
        save();

        broadcast(guild, ChatColor.GREEN + "✦ " + player.getName() + " se ha unido a la Hermandad de Almas.");
        return true;
    }

    public static boolean leaveGuild(Player player) {
        SoulGuild guild = getGuild(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "No perteneces a ninguna Hermandad.");
            return false;
        }

        if (guild.getLeader().equals(player.getUniqueId())) {
            if (guild.getMembers().size() > 1) {
                player.sendMessage(ChatColor.RED + "Eres el líder. Debes transferir el liderazgo o expulsar a los miembros antes de salir.");
                return false;
            } else {
                disbandGuild(player);
                return true;
            }
        }

        guild.getMembers().remove(player.getUniqueId());
        playerGuildMap.remove(player.getUniqueId());
        save();

        player.sendMessage(ChatColor.YELLOW + "Has abandonado la Hermandad de Almas.");
        broadcast(guild, ChatColor.RED + "✦ " + player.getName() + " ha abandonado la Hermandad.");
        return true;
    }

    public static boolean disbandGuild(Player leader) {
        SoulGuild guild = getGuild(leader.getUniqueId());
        if (guild == null || !guild.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Solo el Líder puede disolver la Hermandad.");
            return false;
        }

        broadcast(guild, ChatColor.DARK_RED + "✦ La Hermandad de Almas '" + guild.getName() + "' ha sido disuelta por el líder.");

        for (UUID member : guild.getMembers()) {
            playerGuildMap.remove(member);
        }
        guildsByName.remove(guild.getName().toLowerCase());
        save();
        return true;
    }

    public static SoulGuild getGuild(UUID playerUuid) {
        return playerGuildMap.get(playerUuid);
    }

    public static SoulGuild getGuildByName(String name) {
        if (name == null) return null;
        return guildsByName.get(name.toLowerCase());
    }

    public static long getRequiredXp(int level) {
        if (level >= 10) return Long.MAX_VALUE;
        return level * 25000L;
    }

    public static void addGuildXp(SoulGuild guild, long amount) {
        if (guild == null || guild.getLevel() >= 10 || amount <= 0) return;

        guild.setGuildXp(guild.getGuildXp() + amount);
        long req = getRequiredXp(guild.getLevel());

        if (guild.getGuildXp() >= req && guild.getLevel() < 10) {
            guild.setLevel(guild.getLevel() + 1);
            guild.setGuildXp(guild.getGuildXp() - req);
            save();

            broadcast(guild, "");
            broadcast(guild, ChatColor.GOLD + "✦✦✦ ¡NIVEL DE HERMANDAD AUMENTADO! Nivel " + guild.getLevel() + " ✦✦✦");
            broadcast(guild, ChatColor.AQUA + "Nuevas ventajas y mejoras de bóveda desbloqueadas.");
            broadcast(guild, "");

            for (UUID memberUuid : guild.getMembers()) {
                Player p = Bukkit.getPlayer(memberUuid);
                if (p != null && p.isOnline()) {
                    SoundHarmonicsEngine.playVictoryFanfare(p);
                }
            }
        }
    }

    public static void broadcast(SoulGuild guild, String message) {
        if (guild == null) return;
        for (UUID memberUuid : guild.getMembers()) {
            Player p = Bukkit.getPlayer(memberUuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
            }
        }
    }

    public static List<Player> getNearbyMembers(Player player, double radius) {
        List<Player> list = new ArrayList<>();
        SoulGuild guild = getGuild(player.getUniqueId());
        if (guild == null || player.getWorld() == null) return list;

        double rSq = radius * radius;
        for (UUID uuid : guild.getMembers()) {
            if (uuid.equals(player.getUniqueId())) continue;
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && other.isOnline() && other.getWorld().equals(player.getWorld())) {
                if (other.getLocation().distanceSquared(player.getLocation()) <= rSq) {
                    list.add(other);
                }
            }
        }
        return list;
    }

    public static void cleanup(UUID uuid) {
        pendingInvites.remove(uuid);
    }
}
