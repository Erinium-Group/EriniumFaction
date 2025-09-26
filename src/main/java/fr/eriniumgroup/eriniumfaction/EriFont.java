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
import net.minecraft.resources.ResourceLocation;


public final class EriFont {
	public static final String MODID = "erinium_faction";
	private EriFont() {}

	public static Component audiowide(String text){
		return (Component) Component.nullToEmpty(text).getStyle().withFont(new ResourceLocation(MODID, "audiowide"));
	}

	public static Component exo2(String text){
		return (Component) Component.nullToEmpty(text).getStyle().withFont(new ResourceLocation(MODID, "exo2"));
	}

	public static Component orbitron(String text){
		return (Component) Component.nullToEmpty(text).getStyle().withFont(new ResourceLocation(MODID, "orbitron"));
	}


}