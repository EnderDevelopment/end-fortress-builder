package com.enderdevelopment.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EndCastleCommand implements CommandExecutor {

    private static final int TOWER_HEIGHT = 42;

    // Core materials
    private static final Material WALL = Material.BLACK_CONCRETE;
    private static final Material DARK_ACCENT = Material.BLACKSTONE;
    private static final Material TRIM = Material.BLUE_CONCRETE;
    private static final Material GLOW = Material.SEA_LANTERN;
    private static final Material GLASS = Material.BLUE_STAINED_GLASS;
    private static final Material NEON = Material.LIGHT_BLUE_CONCRETE;
    private static final Material FLOOR = Material.POLISHED_BLACKSTONE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can build the castle.").color(NamedTextColor.RED));
            return true;
        }

        World world = player.getWorld();
        Location base = player.getLocation().getBlock().getLocation();
        base.setY(base.getBlockY());

        buildCastle(world, base);

        player.sendMessage(
            Component.text("Futuristic blue-black castle built! Use ")
                .color(NamedTextColor.AQUA)
                .append(Component.text("/endcastledecor").color(NamedTextColor.GOLD))
                .append(Component.text(" to decorate it with neon lights and effects.").color(NamedTextColor.AQUA))
        );
        return true;
    }

    /**
     * Builds the full castle structure centered at the given location (front-left of structure).
     * The structure spans roughly CASTLE_RADIUS in each direction.
     */
    public static void buildCastle(World world, Location base) {
        // Clear the build area in the air so nothing obstructs.
        int cx = base.getBlockX();
        int cy = base.getBlockY();
        int cz = base.getBlockZ();

        // ---- Central keep (large box) ----
        int keepWidth = 26;
        int keepDepth = 26;
        int keepHeight = 30;

        // Central keep centered on the build point.
        int keepX1 = cx;
        int keepZ1 = cz;
        int keepX2 = cx + keepWidth - 1;
        int keepZ2 = cz + keepDepth - 1;

        // ---- Four corner towers ----
        buildTower(world, cx - 8, cy, cz - 8, 9, TOWER_HEIGHT);
        buildTower(world, cx + keepWidth + 6, cy, cz - 8, 9, TOWER_HEIGHT);
        buildTower(world, cx - 8, cy, cz + keepDepth + 6, 9, TOWER_HEIGHT);
        buildTower(world, cx + keepWidth + 6, cy, cz + keepDepth + 6, 9, TOWER_HEIGHT);

        // ---- Four mid towers on the keep corners ----
        buildTower(world, keepX1 - 1, cy, keepZ1 - 1, 7, 24);
        buildTower(world, keepX2 + 1, cy, keepZ1 - 1, 7, 24);
        buildTower(world, keepX1 - 1, cy, keepZ2 + 1, 7, 24);
        buildTower(world, keepX2 + 1, cy, keepZ2 + 1, 7, 24);

        // ---- Keep walls with battlements ----
        buildKeep(world, keepX1, keepX2, keepZ1, keepZ2, cy, keepHeight);

        // ---- Ground platform ----
        buildGround(world, cx - 20, cy, cz - 20, cx + keepWidth + 20, cz + keepDepth + 20);

        // ---- Entrance (front, on the +z side) ----
        buildEntrance(world, cx + keepWidth / 2, cy, cz + keepDepth);

        // ---- Interior floor and light columns ----
        buildInterior(world, keepX1, keepX2, keepZ1, keepZ2, cy, keepHeight);
    }

    private static void buildGround(World world, int x1, int y, int z1, int x2, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                for (int dy = 0; dy < 2; dy++) {
                    Block b = world.getBlockAt(x, y - dy, z);
                    if (b.getType() == Material.AIR || b.getType() == Material.BLACK_CONCRETE
                        || b.getType() == Material.POLISHED_BLACKSTONE) {
                        b.setType(dy == 0 ? FLOOR : DARK_ACCENT);
                    }
                }
            }
        }
        // Trim the platform edges with glowing blue.
        for (int x = x1; x <= x2; x++) {
            setBlock(world, x, y, z1, NEON);
            setBlock(world, x, y, z2, NEON);
        }
        for (int z = z1; z <= z2; z++) {
            setBlock(world, x1, y, z, NEON);
            setBlock(world, x2, y, z, NEON);
        }
    }

    private static void buildTower(World world, int cx, int cy, int cz, int radius, int height) {
        int r = radius;
        // Outer ring
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                boolean edge = (x == cx - r || x == cx + r || z == cz - r || z == cz + r);
                boolean nearEdge = (Math.abs(x - cx) == r - 1 || Math.abs(z - cz) == r - 1);
                if (!edge && nearEdge && (x == cx - r + 1 || x == cx + r - 1 || z == cz - r + 1 || z == cz + r - 1)) {
                    continue;
                }
                if (edge) {
                    for (int dy = 0; dy < height; dy++) {
                        setBlock(world, x, cy + dy, z, dy < 3 ? DARK_ACCENT : WALL);
                    }
                }
            }
        }
        // Top rim glow
        for (int x = cx - r; x <= cx + r; x++) {
            setBlock(world, x, cy + height, cz - r, GLOW);
            setBlock(world, x, cy + height, cz + r, GLOW);
        }
        for (int z = cz - r; z <= cz + r; z++) {
            setBlock(world, cx - r, cy + height, z, GLOW);
            setBlock(world, cx + r, cy + height, z, GLOW);
        }
        // Crenellation (battlements) on the top edges
        for (int i = 0; i <= r * 2; i += 3) {
            setBlock(world, cx - r + i, cy + height + 1, cz - r, NEON);
            setBlock(world, cx - r + i, cy + height + 1, cz + r, NEON);
            setBlock(world, cx + r, cy + height + 1, cz - r + i, NEON);
            setBlock(world, cx - r, cy + height + 1, cz - r + i, NEON);
        }
        // Inner pillar of light at tower top
        for (int i = 0; i < 3; i++) {
            setBlock(world, cx, cy + height + 1 + i, cz, i == 0 ? GLOW : NEON);
        }
    }

    private static void buildKeep(World world, int x1, int x2, int z1, int z2, int y, int height) {
        // Outer walls
        for (int x = x1; x <= x2; x++) {
            for (int dy = 0; dy < height; dy++) {
                setBlock(world, x, y + dy, z1, dy < 4 ? DARK_ACCENT : WALL);
                setBlock(world, x, y + dy, z2, dy < 4 ? DARK_ACCENT : WALL);
            }
        }
        for (int z = z1; z <= z2; z++) {
            for (int dy = 0; dy < height; dy++) {
                setBlock(world, x1, y + dy, z, dy < 4 ? DARK_ACCENT : WALL);
                setBlock(world, x2, y + dy, z, dy < 4 ? DARK_ACCENT : WALL);
            }
        }

        // Battlement top rim
        for (int x = x1; x <= x2; x += 2) {
            setBlock(world, x, y + height, z1, GLOW);
            setBlock(world, x, y + height, z2, GLOW);
            setBlock(world, x, y + height + 1, z1, NEON);
            setBlock(world, x, y + height + 1, z2, NEON);
        }
        for (int z = z1; z <= z2; z += 2) {
            setBlock(world, x1, y + height, z, GLOW);
            setBlock(world, x2, y + height, z, GLOW);
            setBlock(world, x1, y + height + 1, z, NEON);
            setBlock(world, x2, y + height + 1, z, NEON);
        }

        // Horizontal accent lines on the walls
        for (int x = x1; x <= x2; x++) {
            setBlock(world, x, y + 4, z1, TRIM);
            setBlock(world, x, y + 4, z2, TRIM);
            setBlock(world, x, y + height - 4, z1, TRIM);
            setBlock(world, x, y + height - 4, z2, TRIM);
        }
        for (int z = z1; z <= z2; z++) {
            setBlock(world, x1, y + 4, z, TRIM);
            setBlock(world, x2, y + 4, z, TRIM);
            setBlock(world, x1, y + height - 4, z, TRIM);
            setBlock(world, x2, y + height - 4, z, TRIM);
        }
    }

    private static void buildEntrance(World world, int ex, int ey, int ez) {
        // Doorway: 3 wide, 5 tall opening with blue glow above
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                Block b = world.getBlockAt(ex + dx, ey + dy, ez);
                if (b.getType() == Material.BLACK_CONCRETE || b.getType() == Material.BLACKSTONE) {
                    b.setType(Material.AIR);
                }
            }
        }
        // Glow above the door
        for (int dx = -1; dx <= 1; dx++) {
            setBlock(world, ex + dx, ey + 5, ez, GLOW);
            setBlock(world, ex + dx, ey + 6, ez, NEON);
        }
        // Door pillars
        setBlock(world, ex - 2, ey, ez, DARK_ACCENT);
        setBlock(world, ex + 2, ey, ez, DARK_ACCENT);
        // Blue glass panels beside the door
        setBlock(world, ex - 3, ey + 2, ez, GLASS);
        setBlock(world, ex + 3, ey + 2, ez, GLASS);
        // Floor glow inside the entrance
        setBlock(world, ex, ey, ez + 1, GLOW);
    }

    private static void buildInterior(World world, int x1, int x2, int z1, int z2, int y, int height) {
        // Floor inside the keep with a central glow grid
        for (int x = x1 + 2; x <= x2 - 2; x++) {
            for (int z = z1 + 2; z <= z2 - 2; z++) {
                if ((x - x1) % 5 == 0) {
                    setBlock(world, x, y, z, TRIM);
                } else if ((z - z1) % 5 == 0) {
                    setBlock(world, x, y, z, TRIM);
                }
            }
        }
        // Central light column (a large glowing beacon pillar)
        int cx = (x1 + x2) / 2;
        int cz = (z1 + z2) / 2;
        for (int dy = 1; dy < height - 2; dy++) {
            if (dy % 2 == 0) {
                setBlock(world, cx, y + dy, cz, GLOW);
            } else {
                setBlock(world, cx, y + dy, cz, NEON);
            }
        }
        // Glow ring at the base of the pillar
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlock(world, cx + dx, y + 1, cz + dz, dx == 0 && dz == 0 ? GLOW : NEON);
            }
        }
        // Blue glass windows on the walls
        int half = height / 2;
        for (int x = x1 + 2; x <= x2 - 2; x += 4) {
            setBlock(world, x, y + half, z1, GLASS);
            setBlock(world, x, y + half, z2, GLASS);
        }
        for (int z = z1 + 2; z <= z2 - 2; z += 4) {
            setBlock(world, x1, y + half, z, GLASS);
            setBlock(world, x2, y + half, z, GLASS);
        }
    }

    public static void setBlock(World world, int x, int y, int z, Material material) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() == Material.BEDROCK) {
            return;
        }
        block.setType(material);
    }
}
