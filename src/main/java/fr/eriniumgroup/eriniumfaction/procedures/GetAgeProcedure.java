package fr.eriniumgroup.eriniumfaction.procedures;

public class GetAgeProcedure {
	public static double execute(double currentTime, double dateOfCreation) {
		double ageInSeconds = 0;
		double ageInDays = 0;
		ageInSeconds = currentTime - dateOfCreation;
		ageInDays = ageInSeconds / 86400;
		return ageInDays;
	}
}