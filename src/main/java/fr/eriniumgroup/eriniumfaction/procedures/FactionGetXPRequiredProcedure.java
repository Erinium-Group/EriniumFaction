package fr.eriniumgroup.eriniumfaction.procedures;

public class FactionGetXPRequiredProcedure {
	public static double execute(double level) {
		if (level >= 20) {
			return 0;
		}
		return 500 * Math.pow(1.5, level);
	}
}