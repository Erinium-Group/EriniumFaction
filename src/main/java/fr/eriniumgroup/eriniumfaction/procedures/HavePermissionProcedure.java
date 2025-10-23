package fr.eriniumgroup.eriniumfaction.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import java.util.regex.Pattern;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

import fr.eriniumgroup.eriniumfaction.network.EriniumFactionModVariables;

public class HavePermissionProcedure {
	public static boolean execute(Entity entity, String perm) {
		if (entity == null || perm == null)
			return false;
		File file = new File("");
		com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
		String tempId = "";
		String factionRank = "";
		boolean impossibleToInterract = false;
		boolean needcheckworldfile = false;
		tempId = entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction;
		if (entity instanceof ServerPlayer) {
			if (entity.getData(EriniumFactionModVariables.PLAYER_VARIABLES).faction_bypass) {
				return true;
			} else if ((tempId).equals("wilderness")) {
				return false;
			}
		}
		file = FactionFileByIdProcedure.execute(tempId);
		if (file.exists()) {
			{
				try {
					BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
					StringBuilder jsonstringbuilder = new StringBuilder();
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						jsonstringbuilder.append(line);
					}
					bufferedReader.close();
					jsonObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
					if (jsonObject.get("isAdminFaction").getAsBoolean() || jsonObject.get("isWarzone").getAsBoolean() || jsonObject.get("isSafezone").getAsBoolean()) {
						impossibleToInterract = true;
					} else {
						if (entity instanceof ServerPlayer) {
							factionRank = new Object() {
								private String extraireValeur(String texte, String cle, String separateurValeur, String separateurObjet) {
									if (!texte.contains(separateurValeur) || !texte.contains(separateurObjet)) {
										return "null"; // Séparateurs manquants
									}
									String[] objets = texte.split("\\s*" + Pattern.quote(separateurObjet) + "\\s*");
									for (String objet : objets) {
										String[] parts = objet.split(Pattern.quote(separateurValeur), 2);
										if (parts.length == 2 && parts[0].trim().equals(cle)) {
											return parts[1].trim();
										}
									}
									return "null"; // Clé pas trouvée
								}
							}.extraireValeur((entity.getStringUUID()), jsonObject.get("memberList").getAsString(), ":", ",");
							if ((jsonObject.get("owner").getAsString()).equals(entity.getStringUUID())) {
								impossibleToInterract = false;
							} else if (jsonObject.get(factionRank).getAsString().contains(perm)) {
								impossibleToInterract = false;
							} else {
								impossibleToInterract = true;
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (impossibleToInterract) {
				return false;
			}
		}
		return true;
	}
}