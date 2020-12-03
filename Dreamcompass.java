package me.prostedeni.goodcraft.dreamcompass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class Dreamcompass extends JavaPlugin implements Listener {

    public static HashMap<Player, String> Mapa;
    public static HashMap<Player, Location> PortalMap;

    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(this,this);
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Mapa = new HashMap<>();
        PortalMap = new HashMap<>();
    }

    public static java.util.Random rand = new java.util.Random();

    public static int epsilon;

    public static Location loc0;
    public static Location loc1;
    public static Location loc2;

    public static int minimumDist;

    public static int maximumX;
    public static int maximumZ;

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        reloadConfig();
        saveConfig();
    }

    public static boolean hasAdvancement(Player player, String achname){
        Advancement ach = null;
        for (Iterator<Advancement> iter = Bukkit.getServer().advancementIterator(); iter.hasNext(); ) {
            Advancement adv = iter.next();
            if (adv.getKey().getKey().equalsIgnoreCase(achname)){
                ach = adv;
                break;
            }
        }
        AdvancementProgress prog = player.getAdvancementProgress(ach);
        if (prog.isDone()){
            return true;
        }
        return false;
    }
    @EventHandler
    public void onEvent(PlayerInteractEvent e) {
        if (e.getPlayer().getItemInHand().getType() == Material.COMPASS) {
            if (Mapa.containsKey(e.getPlayer())) {

                Player target = Bukkit.getPlayer(Mapa.get(e.getPlayer()));
                Location targetLoc = target.getLocation();
                if (target.getWorld().getEnvironment() == World.Environment.NETHER || target.getWorld().getEnvironment() == World.Environment.THE_END) {
                    if (PortalMap.containsKey(target)){
                        if (e.getPlayer().getWorld().getEnvironment() != target.getWorld().getEnvironment()) {
                            targetLoc = PortalMap.get(target);
                        }
                    }
                }
                    if (e.getPlayer().hasPermission("dreamcompass.use")) {
                        if (target != null) {
                            boolean mode = getConfig().getBoolean("DefaultMode");
                            if (mode) {
                                e.getPlayer().setCompassTarget(targetLoc);
                                e.getPlayer().sendMessage(ChatColor.GOLD + "Compass pointing to " + Mapa.get(e.getPlayer()));
                                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', "&3&lY: &b" + target.getLocation().getBlockY())));
                                int EnderChance = getConfig().getInt("EnderChance");
                                if (EnderChance > 0 && EnderChance <= 100) {
                                    if (hasAdvancement(target, "nether/obtain_blaze_rod")) {
                                        if (target.getWorld().getName().equals("world")) {
                                            double EnderChanceDouble = ((double) EnderChance / (double) 100);
                                            double r = rand.nextDouble();
                                            if (r >= 1 - EnderChanceDouble) {

                                                int minimumDist = getConfig().getInt("MinimumDistance");
                                                int maximumX = getConfig().getInt("MaximumDistance");
                                                int maximumZ = getConfig().getInt("MaximumDistance");

                                                int randomX = ThreadLocalRandom.current().nextInt((targetLoc.getBlockX() - maximumX), (targetLoc.getBlockX() + maximumX + 1));
                                                int randomZ = ThreadLocalRandom.current().nextInt((targetLoc.getBlockZ() - maximumZ), (targetLoc.getBlockZ() + maximumZ + 1));

                                                Biome biome = target.getWorld().getBiome(targetLoc.getBlockX(), targetLoc.getBlockZ());

                                                if ((biome == Biome.GRAVELLY_MOUNTAINS || biome == Biome.MOUNTAINS || biome == Biome.MOUNTAIN_EDGE || biome == Biome.MODIFIED_GRAVELLY_MOUNTAINS || biome == Biome.SNOWY_MOUNTAINS || biome == Biome.SNOWY_TAIGA_MOUNTAINS || biome == Biome.TAIGA_MOUNTAINS || biome == Biome.ICE_SPIKES || biome == Biome.WOODED_MOUNTAINS || biome == Biome.SHATTERED_SAVANNA) || (targetLoc.getBlockY() > 120)) {
                                                    epsilon = (targetLoc.getBlockY() - 70);
                                                } else {
                                                    epsilon = (targetLoc.getBlockY() - 15);
                                                }

                                                Location loc0 = new Location(target.getWorld(), randomX, epsilon, randomZ);
                                                Location loc1 = new Location(target.getWorld(), randomX, epsilon + 1, randomZ);
                                                Location loc2 = new Location(target.getWorld(), randomX, epsilon + 2, randomZ);

                                                while (loc0.distance(targetLoc) < minimumDist) {
                                                    randomX = ThreadLocalRandom.current().nextInt((targetLoc.getBlockX() - maximumX), (targetLoc.getBlockX() + maximumX + 1));
                                                    randomZ = ThreadLocalRandom.current().nextInt((targetLoc.getBlockZ() - maximumZ), (targetLoc.getBlockZ() + maximumZ + 1));
                                                    loc0 = new Location(target.getWorld(), randomX, epsilon, randomZ);
                                                    if (loc0.distance(targetLoc) < minimumDist) {
                                                        break;
                                                    }
                                                }
                                                for (int i = 2; i < 200; ++i) {
                                                    ++epsilon;
                                                    loc0 = new Location(target.getWorld(), randomX, epsilon, randomZ);
                                                    loc1 = new Location(target.getWorld(), randomX, epsilon + 1, randomZ);
                                                    loc2 = new Location(target.getWorld(), randomX, epsilon + 2, randomZ);
                                                    if (loc0.getBlock().getType() == Material.AIR && loc1.getBlock().getType() == Material.AIR && loc2.getBlock().getType() == Material.AIR) {
                                                        target.getWorld().spawnEntity(loc0, EntityType.ENDERMAN);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!mode) {
                                e.getPlayer().setCompassTarget(targetLoc);
                                e.getPlayer().sendMessage(ChatColor.GOLD + "Compass pointing to " + Mapa.get(e.getPlayer()));
                                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', "&3&lX: &b" + target.getLocation().getBlockX() + "   &3&lY: &b" + target.getLocation().getBlockY() + "   &3&lZ: &b" + target.getLocation().getBlockZ() + "   &c&lWorld: &e" + target.getWorld().getName())));
                                int EnderChance = getConfig().getInt("EnderChance");
                                if (EnderChance > 0 && EnderChance <= 100) {
                                    if (hasAdvancement(target, "nether/obtain_blaze_rod")) {
                                        if (target.getWorld().getName().equals("world")) {
                                            double EnderChanceDouble = ((double) EnderChance / (double) 100);
                                            double r = rand.nextDouble();
                                            if (r >= 1 - EnderChanceDouble) {

                                                int minimumDist = getConfig().getInt("MinimumDistance");
                                                int maximumX = getConfig().getInt("MaximumDistance");
                                                int maximumZ = getConfig().getInt("MaximumDistance");

                                                int randomX = ThreadLocalRandom.current().nextInt((targetLoc.getBlockX() - maximumX), (targetLoc.getBlockX() + maximumX + 1));
                                                int randomZ = ThreadLocalRandom.current().nextInt((targetLoc.getBlockZ() - maximumZ), (targetLoc.getBlockZ() + maximumZ + 1));

                                                Biome biome = target.getWorld().getBiome(target.getLocation().getBlockX(), target.getLocation().getBlockZ());

                                                if ((biome == Biome.GRAVELLY_MOUNTAINS || biome == Biome.MOUNTAINS || biome == Biome.MOUNTAIN_EDGE || biome == Biome.MODIFIED_GRAVELLY_MOUNTAINS || biome == Biome.SNOWY_MOUNTAINS || biome == Biome.SNOWY_TAIGA_MOUNTAINS || biome == Biome.TAIGA_MOUNTAINS || biome == Biome.ICE_SPIKES || biome == Biome.WOODED_MOUNTAINS || biome == Biome.SHATTERED_SAVANNA) || (target.getLocation().getBlockY() > 120)) {
                                                    epsilon = (targetLoc.getBlockY() - 70);
                                                } else {
                                                    epsilon = (targetLoc.getBlockY() - 15);
                                                }

                                                Location loc0 = new Location(target.getWorld(), randomX, epsilon, randomZ);
                                                Location loc1 = new Location(target.getWorld(), randomX, epsilon + 1, randomZ);
                                                Location loc2 = new Location(target.getWorld(), randomX, epsilon + 2, randomZ);

                                                while (loc0.distance(targetLoc) < minimumDist) {
                                                    randomX = ThreadLocalRandom.current().nextInt((targetLoc.getBlockX() - maximumX), (targetLoc.getBlockX() + maximumX + 1));
                                                    randomZ = ThreadLocalRandom.current().nextInt((targetLoc.getBlockZ() - maximumZ), (targetLoc.getBlockZ() + maximumZ + 1));
                                                    loc0 = new Location(target.getWorld(), randomX, epsilon, randomZ);
                                                    if (loc0.distance(targetLoc) < minimumDist) {
                                                        break;
                                                    }
                                                }
                                                for (int i = 2; i < 200; ++i) {
                                                    ++epsilon;
                                                    loc0 = new Location(target.getWorld(), randomX, epsilon, randomZ);
                                                    loc1 = new Location(target.getWorld(), randomX, epsilon + 1, randomZ);
                                                    loc2 = new Location(target.getWorld(), randomX, epsilon + 2, randomZ);
                                                    if (loc0.getBlock().getType() == Material.AIR && loc1.getBlock().getType() == Material.AIR && loc2.getBlock().getType() == Material.AIR) {
                                                        target.getWorld().spawnEntity(loc0, EntityType.ENDERMAN);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lPlayer &4&l" + Mapa.get(e.getPlayer()) + "&c&l is offline"));
                        }
                    } else {
                        e.getPlayer().sendMessage(ChatColor.DARK_RED + "You don't have permission to do that");
                    }
            }
        }
    }

    @EventHandler
    public void onNetherEndEnter(PlayerPortalEvent e){
        if (Mapa.containsValue(e.getPlayer().getName())){

            Location portalLoc = e.getPlayer().getLocation();

            new BukkitRunnable(){
                @Override
                public void run(){
                    if (e.getPlayer().getWorld().getEnvironment() == World.Environment.NETHER || e.getPlayer().getWorld().getEnvironment() == World.Environment.THE_END){
                        PortalMap.put(e.getPlayer(), portalLoc);
                    }
                }
            }.runTaskLaterAsynchronously(this, 200);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("compass")){
            if(sender instanceof Player) {
                Player player = (Player) sender;
                if(player.hasPermission("dreamcompass.use")) {
                    if (args.length == 0) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lUse /compass <player> or /compass reload"));
                        return true;
                    }

                    if(args.length == 1) {
                        String name = args[0];
                        Player target = Bukkit.getPlayer(name);
                        if (player.hasPermission("dreamcompass.use")) {
                            if (args[0].equalsIgnoreCase("help")){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&3 Use &c&l/compass <name> &3to get compass, use &c&l/compass reload &3and use &c&l/help &3to get this help message"));
                            } else {
                                if (args[0].equalsIgnoreCase("reload")) {
                                    reloadConfig();
                                    getConfig();
                                    saveConfig();
                                    player.sendMessage(ChatColor.DARK_AQUA + "Config reloaded");
                                } else {
                                    if (target == null) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lPlayer &4&l" + name + "&c&l is offline"));
                                        return true;
                                    } else {
                                        Mapa.put(player, name);
                                        player.setItemInHand(new ItemStack(Material.COMPASS));
                                        player.setCompassTarget(target.getLocation());
                                        sender.sendMessage(ChatColor.GOLD + "Compass pointing to " + name);
                                        boolean mode = getConfig().getBoolean("DefaultMode");
                                        if (mode){
                                            player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', "&3&lY: &b" + target.getLocation().getBlockY())));
                                        } else{
                                            player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', "&3&lX: &b" + target.getLocation().getBlockX() + "   &3&lY: &b" + target.getLocation().getBlockY() + "   &3&lZ: &b" + target.getLocation().getBlockZ() + "   &c&lWorld: &e" + target.getWorld().getName())));
                                        }

                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return true;
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&4&lYou don't have the permission to do that"));
                }

                return false;
            }
            return false;
        }
        return false;
    }
}
