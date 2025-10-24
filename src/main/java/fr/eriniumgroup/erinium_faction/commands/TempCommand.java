package fr.eriniumgroup.erinium_faction.commands;

import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.common.util.EFUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class TempCommand {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("temp")

                .executes(arguments -> {
                    Level world = arguments.getSource().getUnsidedLevel();
                    double x = arguments.getSource().getPosition().x();
                    double y = arguments.getSource().getPosition().y();
                    double z = arguments.getSource().getPosition().z();
                    Entity entity = arguments.getSource().getEntity();
                    if (entity == null && world instanceof ServerLevel _servLevel)
                        entity = FakePlayerFactory.getMinecraft(_servLevel);
                    Direction direction = Direction.DOWN;
                    if (entity != null) direction = entity.getDirection();

                    temp(world, x, y, z, entity);
                    return 0;
                }));
    }

    public static void temp(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) return;
        if (entity instanceof ServerPlayer _ent) {
            BlockPos _bpos = BlockPos.containing(x, y, z);
            _ent.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("FactionMenu");
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    String playerFaction = FactionManager.getPlayerFaction(player.getUUID());
                    Faction f = (playerFaction != null) ? FactionManager.getFaction(playerFaction) : null;
                    FactionSnapshot snapshot = makeSnapshot(f);

                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    buf.writeBlockPos(_bpos);
                    buf.writeVarInt(1); // version du payload
                    snapshot.write(buf);
                    return new FactionMenu(id, inventory, buf);
                }
            }, _bpos);
        }
    }

    private static FactionSnapshot makeSnapshot(Faction f) {
        if (f == null) return new FactionSnapshot("", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, java.util.Map.of(), java.util.Map.of());
        String name = f.getName();
        java.io.File factionFile = EFUtils.Faction.FactionFileById(name);
        String displayName = EFUtils.F.GetFileStringValue(factionFile, "displayname");
        if (displayName == null || displayName.isEmpty()) displayName = name;
        String claimList = EFUtils.F.GetFileStringValue(factionFile, "claimlist");
        int claims = (claimList == null || claimList.isEmpty()) ? 0 : claimList.split(",").length;
        int maxClaims = (int) EFUtils.F.GetFileNumberValue(factionFile, "maxClaims");
        int maxPlayers = (int) EFUtils.F.GetFileNumberValue(factionFile, "maxPlayer");
        int level = (int) EFUtils.F.GetFileNumberValue(factionFile, "factionLevel");
        int xp = (int) EFUtils.F.GetFileNumberValue(factionFile, "factionXp");
        int currentPower = (int) EFUtils.F.GetFileNumberValue(factionFile, "power");
        int maxPower = (int) Math.floor(f.getPower());
        int xpRequired = (int) Math.round(f.getXPRequiredForNextLevel(level));
        java.util.Map<java.util.UUID, String> membersRank = new java.util.LinkedHashMap<>();
        java.util.Map<java.util.UUID, String> memberNames = new java.util.LinkedHashMap<>();
        for (var e : f.getMembers().entrySet()) {
            java.util.UUID id = e.getKey();
            String rankName = e.getValue().name();
            membersRank.put(id, rankName);
            String disp = EFUtils.F.GetFileStringValue(EFUtils.F.UUIDFile(String.valueOf(id)), "displayname");
            if (disp == null || disp.isEmpty()) disp = id.toString();
            memberNames.put(id, disp);
        }
        int membersCount = f.getMembers().size();
        return new FactionSnapshot(name, displayName, claims, maxClaims, membersCount, maxPlayers, level, xp, xpRequired, currentPower, maxPower, membersRank, memberNames);
    }
}

