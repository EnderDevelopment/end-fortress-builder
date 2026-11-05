package com.enderdevelopment.plugin;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EndCastleDecorCommand implements CommandExecutor {

    private static final Material GLOW = Material.SEA_LANTERN;
    private static final Material NEON = Material.LIGHT_BLUE_CONCRETE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can decorate the castle.").color(NamedTextColor.RED));
            return true;
        }

        World world = player.getWorld();
        Location base = player.getLocation().getBlock().getLocation();
        int cx = base.getBlockX();
        int cy = base.getBlockY();
        int cz = base.getBlockZ();

        decorateCastle(world, cx, cy, cz);

        player.sendMessage(
            Component.text("Castle decorated with neon lights, end rods, runes and effects!")
                .color(NamedTextColor.AQUA)
        );

        // Firework show over the castle
        launchFireworks(world, cx, cy + 40, cz, 8);
        return true;
    }

    public static void decorateCastle(World world, int cx, int cy, int cz) {
        // Neon beacon columns at each corner of the build area
        for (int[] offset : new int[][]{{-20, -20}, {-20, 20}, {20, -20}, {20, 20}}) {
            addBeaconColumn(world, cx + offset[0], cy, cz + offset[1], 14);
        }

        // Scatter end rod spikes on top of towers / walls
        scatterEndRods(world, cx - 20, cx + 20, cy + 31, cz - 20, cz + 20);

        // Add glow runes along the ground trim
        addGlowRunway(world, cx - 18, cy + 1, cz - 18, cx + 18, cz + 18);
    }

    private static void addBeaconColumn(World world, int x, int y, int z, int height) {
        for (int dy = 1; dy <= height; dy++) {
            Material m = dy % 2 == 0 ? GLOW : NEON;
            if (world.getBlockAt(x, y + dy, z).getType() == Material.AIR) {
                world.getBlockAt(x, y + dy, z).setType(m);
            }
        }
        // Light beam effect
        world.spawnParticle(Particle.END_ROD, x + 0.5, y + height + 1, z + 0.5, 40, 0.4, 3, 0.4, 0.02);
    }

    private static void scatterEndRods(World world, int x1, int x2, int y, int z1, int z2) {
        // Place end rods at regular intervals on the upper rim
        for (int x = x1; x <= x2; x += 5) {
            placeEndRod(world, x, y, z1);
            placeEndRod(world, x, y, z2);
        }
        for (int z = z1; z <= z2; z += 5) {
            placeEndRod(world, x1, y, z);
            placeEndRod(world, x2, y, z);
        }
    }

    private static void placeEndRod(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() != Material.AIR) {
            return;
        }
        block.setType(Material.END_ROD);
    }

    private static void addGlowRunway(World world, int x1, int y, int z1, int x2, int z2) {
        int count = 0;
        for (int x = x1; x <= x2; x += 3) {
            Material m = count % 2 == 0 ? GLOW : NEON;
            Block b = world.getBlockAt(x, y, z1);
            if (b.getType() == Material.AIR || b.getType() == Material.WATER) {
                b.setType(m);
            }
            Block b2 = world.getBlockAt(x, y, z2);
            if (b2.getType() == Material.AIR || b2.getType() == Material.WATER) {
                b2.setType(m);
            }
            count++;
        }
        for (int z = z1; z <= z2; z += 3) {
            Material m = count % 2 == 0 ? GLOW : NEON;
            Block b = world.getBlockAt(x1, y, z);
            if (b.getType() == Material.AIR || b.getType() == Material.WATER) {
                b.setType(m);
            }
            Block b2 = world.getBlockAt(x2, y, z);
            if (b2.getType() == Material.AIR || b2.getType() == Material.WATER) {
                b2.setType(m);
            }
            count++;
        }
    }

    private static void launchFireworks(World world, int x, int y, int z, int count) {
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("FuturisticEndCastle");
        for (int i = 0; i < count; i++) {
            final long delay = i * 15L;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double ox = Math.random() * 20 - 10;
                double oz = Math.random() * 20 - 10;
                Firework fw = (Firework) world.spawnEntity(
                    new Location(world, x + ox, y + (Math.random() * 8), z + oz),
                    EntityType.FIREWORK_ROCKET
                );
                FireworkMeta meta = fw.getFireworkMeta();
                FireworkEffect effect = FireworkEffect.builder()
                    .withColor(Color.fromRGB(0, 150, 255))
                    .withFade(Color.fromRGB(10, 10, 30))
                    .with(FireworkEffect.Type.BURST)
                    .build();
                meta.addEffect(effect);
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            }, delay);
        }
    }
}
