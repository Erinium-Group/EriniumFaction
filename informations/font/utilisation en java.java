Font orbitronFont = Minecraft.getInstance().fontManager.createFont(
    new net.minecraft.client.gui.FontManager.FontDefinition(
        new ResourceLocation("erinium_faction", "orbitron")
    )
);
orbitronFont.draw(poseStack, "Faction Menu", x, y, 0xFFFFFF);