/**
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package fr.eriniumgroup.eriniumfaction;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;


public class EriFont {
	private static Component make(String text, String fontId) {
		Style style = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("erinium_faction", fontId));
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