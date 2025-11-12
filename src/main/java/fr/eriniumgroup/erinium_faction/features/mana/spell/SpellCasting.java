package fr.eriniumgroup.erinium_faction.features.mana.spell;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelData;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelManager;
import fr.eriniumgroup.erinium_faction.features.mana.ManaAttachments;
import fr.eriniumgroup.erinium_faction.features.mana.PlayerManaData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SpellCasting {
    private static int requiredLevelForTier(int tier) {
        return switch (tier) {
            case 1 -> 1;
            case 2 -> 20;
            case 3 -> 45;
            case 4 -> 70;
            case 5 -> 90;
            case 6 -> 100; // ultimate
            default -> Integer.MAX_VALUE;
        };
    }

    public static boolean cast(ServerPlayer player, ResourceLocation spellId) {
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Spell spell = SpellRegistry.get(spellId);
        if (spell == null) {
            player.displayClientMessage(Component.translatable("erinium_faction.spell.unknown", spellId.toString()), true);
            return false;
        }
        PlayerManaData md = player.getData(ManaAttachments.PLAYER_MANA);
        if (!md.knowsSpell(spellId.toString())) {
            player.displayClientMessage(Component.translatable("erinium_faction.spell.unknown", spellId.toString()), true);
            return false;
        }
        if (md.isOnCooldown(spellId.toString(), now)) {
            player.displayClientMessage(Component.translatable("erinium_faction.spell.on_cooldown", ""), true);
            return false;
        }
        PlayerLevelData ld = PlayerLevelManager.getLevelData(player);
        int lvl = (ld != null) ? ld.getLevel() : 1;
        int req = requiredLevelForTier(spell.tier);
        if (lvl < req) {
            player.displayClientMessage(Component.translatable("erinium_faction.spell.level_req", req), true);
            return false;
        }
        double max = md.computeMaxMana(player);
        if (md.getMana() < spell.manaCost) {
            player.displayClientMessage(Component.translatable("erinium_faction.mana.not_enough"), true);
            return false;
        }
        boolean ok = false;
        try {
            if ("fire".equals(spell.type)) {
                ok = FireSpells.cast(level, player, spell);
            }
        } catch (Exception ex) {
            EFC.log.error("Casting failed for {}: {}", spellId, ex.toString());
        }
        if (!ok) return false;
        md.setMana(md.getMana() - spell.manaCost, max);
        md.setCooldown(spellId.toString(), now + spell.cooldownTicks);
        return true;
    }
}
