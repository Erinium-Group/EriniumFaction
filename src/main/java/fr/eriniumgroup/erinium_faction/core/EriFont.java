/**
 * The code of this mod element is always locked.
 * <p>
 * You can register new events in this class too.
 * <p>
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 * <p>
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 * <p>
 * This class will be added in the mod root package.
 */
package fr.eriniumgroup.erinium_faction.core;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;


public class EriFont {
    private static Component make(String text, String fontId) {
        Style style = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(EFC.MODID, fontId));
        return Component.literal(text).setStyle(style);
    }

    public static Component orbitron(String text) {
        return make(text, "orbitron");
    }

    public static Component orbitronBold(String text) {
        return make(text, "orbitron_bold");
    }

    public static Component exo2(String text) {
        return make(text, "exo2");
    }

    public static Component audiowide(String text) {
        return make(text, "audiowide");
    }

    // À ajouter dans EriFont.java
    public static interface EriFontAccess {
        Component get(String text);
    }
}