package fr.eriniumgroup.erinium_faction.client.overlay;

import fr.eriniumgroup.erinium_faction.common.network.EFVariables;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FactionTitleOverlay extends Overlay {

    @Override
    public void render(GuiGraphics guiGraphics, int i, int i1, float v) {

        if (EFVariables.PLAYER_VARIABLES.get())
    }
}
