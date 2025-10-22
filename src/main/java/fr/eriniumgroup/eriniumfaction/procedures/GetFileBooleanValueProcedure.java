package fr.eriniumgroup.eriniumfaction.procedures;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

public class GetFileBooleanValueProcedure {
	public static boolean execute(File fileobj, String object) {
		if (fileobj == null || object == null)
			return false;
		File file = new File("");
		com.google.gson.JsonObject Json = new com.google.gson.JsonObject();
		double number = 0;
		boolean booleanObj = false;
		file = fileobj;
		{
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				StringBuilder jsonstringbuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					jsonstringbuilder.append(line);
				}
				bufferedReader.close();
				Json = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
				if (Json.has(object)) {
					booleanObj = Json.get(object).getAsBoolean();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return booleanObj;
	}
}