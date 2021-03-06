package net.citizensnpcs.api.astar.pathfinder;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

public class MinecraftBlockExaminer implements BlockExaminer {
    private boolean canStandIn(Material mat) {
        return PASSABLE.contains(mat);
    }

    private boolean canStandOn(Material mat) {
        return !UNWALKABLE.contains(mat);
    }

    private boolean contains(Material[] search, Material... find) {
        for (Material haystack : search) {
            for (Material needle : find)
                if (haystack == needle)
                    return true;
        }
        return false;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (above == Material.WEB || in == Material.WEB)
            return 1F;
        if (below == Material.SOUL_SAND || below == Material.ICE)
            return 1F;
        if (isLiquid(above, below, in))
            return 0.5F;
        if (isDoor(above, below)) {
            point.addCallback(new OpenDoorCallback());
        }
        return 0.5F - COSTS[source.getLightLevel(pos)];
    }

    private boolean isDoor(Material... below) {
        return contains(below, Material.WOOD_DOOR);
    }

    private boolean isLiquid(Material... materials) {
        return contains(materials, Material.WATER, Material.STATIONARY_WATER, Material.LAVA,
                Material.STATIONARY_LAVA);
    }

    @Override
    public boolean isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (!below.isBlock() || !canStandOn(below)) {
            return false;
        }
        if (!canStandIn(above) || !canStandIn(in)) {
            return false;
        }
        return true;
    }

    private static final float[] COSTS = new float[16];
    private static final Vector DOWN = new Vector(0, -1, 0);
    private static final Set<Material> PASSABLE = EnumSet.of(Material.AIR, Material.DEAD_BUSH,
            Material.DETECTOR_RAIL, Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
            Material.FENCE_GATE, Material.ITEM_FRAME, Material.LADDER, Material.LEVER, Material.LONG_GRASS,
            Material.MELON_STEM, Material.NETHER_FENCE, Material.PUMPKIN_STEM, Material.POWERED_RAIL,
            Material.RAILS, Material.RED_ROSE, Material.RED_MUSHROOM, Material.REDSTONE,
            Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_WIRE, Material.SIGN,
            Material.SIGN_POST, Material.SNOW, Material.STRING, Material.STONE_BUTTON,
            Material.SUGAR_CANE_BLOCK, Material.TRIPWIRE, Material.VINE, Material.WALL_SIGN, Material.WHEAT,
            Material.WATER, Material.WEB, Material.WOOD_BUTTON, Material.WOODEN_DOOR,
            Material.STATIONARY_WATER);
    private static final Set<Material> UNWALKABLE = Sets.union(
            EnumSet.of(Material.AIR, Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS), PASSABLE);
    private static final Vector UP = new Vector(0, 1, 0);
    static {
        // from Minecraft WorldProvider code
        float f = 0.0F;

        for (int i = 0; i <= 15; ++i) {
            float f1 = 1.0F - i / 15.0F;

            COSTS[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
        }
    }
}
