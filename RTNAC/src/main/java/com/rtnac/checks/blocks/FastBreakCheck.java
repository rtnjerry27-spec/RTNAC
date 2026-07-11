package com.rtnac.checks.blocks;

import com.rtnac.RTNAC;
import com.rtnac.checks.Check;
import com.rtnac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

/**
 * Estimates vanilla break time for the block+tool combo and flags breaks
 * that complete significantly faster than that estimate (classic
 * "FastBreak"/nuker-adjacent hack). This uses a simplified hardness table
 * covering the most commonly exploited blocks rather than the full vanilla
 * dataset, and is intentionally lenient (min-break-time-multiplier) to avoid
 * punishing efficiency enchant / haste edge cases.
 */
public class FastBreakCheck extends Check {

    public FastBreakCheck(RTNAC plugin) {
        super(plugin, "fastbreak", "fastbreak");
    }

    private double baseHardness(Material m) {
        return switch (m) {
            case STONE, COBBLESTONE, ANDESITE, DIORITE, GRANITE -> 1.5;
            case DIRT, GRASS_BLOCK, SAND, GRAVEL, PODZOL, MYCELIUM -> 0.5;
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS -> 2.0;
            case OBSIDIAN -> 50.0;
            case IRON_ORE, DEEPSLATE_IRON_ORE, COAL_ORE, DEEPSLATE_COAL_ORE -> 3.0;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 3.0;
            case NETHERITE_BLOCK -> 50.0;
            case ANCIENT_DEBRIS -> 30.0;
            default -> 1.0;
        };
    }

    private double toolMultiplier(ItemStack tool, Material blockMat) {
        if (tool == null) return 1.0;
        String toolName = tool.getType().name();
        double base = 1.0;

        boolean correctTool = (toolName.contains("PICKAXE") && !blockMat.name().contains("WOOD"))
                || (toolName.contains("AXE") && !toolName.contains("PICKAXE"))
                || toolName.contains("SHOVEL") || toolName.contains("HOE");

        if (toolName.startsWith("NETHERITE")) base = 9.0;
        else if (toolName.startsWith("DIAMOND")) base = 8.0;
        else if (toolName.startsWith("IRON")) base = 6.0;
        else if (toolName.startsWith("STONE")) base = 4.0;
        else if (toolName.startsWith("GOLDEN")) base = 12.0;
        else if (toolName.startsWith("WOODEN")) base = 2.0;

        if (!correctTool) base = 1.0;

        if (tool.getEnchantments().containsKey(Enchantment.EFFICIENCY)) {
            int lvl = tool.getEnchantments().get(Enchantment.EFFICIENCY);
            base += (lvl * lvl + 1);
        }

        return Math.max(base, 1.0);
    }

    public void handleBreak(Player player, PlayerData data, Block block, long breakStartMs) {
        if (!isEnabled() || player.hasPermission("rtnac.bypass")) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long actualMs = now - breakStartMs;
        if (breakStartMs <= 0 || actualMs <= 0 || actualMs > 10_000) return; // ignore unreliable samples

        double hardness = baseHardness(block.getType());
        if (hardness <= 0) return; // instant-break blocks (e.g. grass, torches)

        ItemStack tool = player.getInventory().getItemInMainHand();
        double multiplier = toolMultiplier(tool, block.getType());

        double hasteBonus = 1.0;
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            int amp = player.getPotionEffect(PotionEffectType.HASTE).getAmplifier() + 1;
            hasteBonus = 1.0 + (amp * 0.2);
        }

        double expectedSeconds = (hardness * 1.5) / (multiplier * hasteBonus);
        double expectedMs = expectedSeconds * 1000;

        double minMultiplier = cfgDouble("min-break-time-multiplier", 0.6);
        double minAllowedMs = expectedMs * minMultiplier;

        if (actualMs < minAllowedMs && expectedMs > 150) {
            flag(player, data, String.format("broke %s in %dms (expected ~%.0fms)", block.getType(), actualMs, expectedMs), 1.5);
        }
    }
}
