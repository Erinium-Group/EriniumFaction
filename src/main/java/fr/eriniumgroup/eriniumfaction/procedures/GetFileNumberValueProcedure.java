package fr.eriniumgroup.eriniumfaction.procedures;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

public class GetFileNumberValueProcedure {
	public static double execute(File fileobj, String object) {
		if (fileobj == null || object == null)
			return 0;
		File file = new File("");
		com.google.gson.JsonObject Json = new com.google.gson.JsonObject();
		double number = 0;
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
					number = Json.get(object).getAsDouble();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return number;
	}
}