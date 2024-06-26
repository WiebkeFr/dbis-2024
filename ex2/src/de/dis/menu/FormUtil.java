package de.dis.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
/**
 * Kleine Helferklasse zum Einlesen von Formulardaten
 */
public class FormUtil {
	/**
	 * Liest einen String vom standard input ein
	 * @param label Zeile, die vor der Eingabe gezeigt wird
	 * @return eingelesene Zeile
	 */
	public static String readString(String label) {
		String ret = null;
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try {
			System.out.print(label+": ");
			ret = stdin.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * Liest einen Integer vom standard input ein
	 * @param label Zeile, die vor der Eingabe gezeigt wird
	 * @return eingelesener Integer
	 */
	public static int readInt(String label) {
		int ret = 0;
		boolean finished = false;

		while(!finished) {
			String line = readString(label);
			
			try {
				ret = Integer.parseInt(line);
				finished = true;
			} catch (NumberFormatException e) {
				System.err.println("Ungültige Eingabe: Bitte geben Sie eine Zahl an!");
			}
		}
		
		return ret;
	}

	public static LocalDate readDate(String label) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = null;
		boolean finished = false;

		while (!finished) {
			String line = readString(label + " (format yyyy-MM-dd)");
			try {
				date = LocalDate.parse(line, formatter);
				finished = true;
			} catch (DateTimeParseException e) {
				System.err.println("Ungültige Eingabe: Bitte geben Sie das Datum im Format yyyy-MM-dd an.");
			}
		}

		return date;
	}

	public static String editValue(String type, String oldValue) {
		String label = type;
		if (oldValue.length() != 0) {
			label += " (default: \'" + oldValue + "\')";
		}
		String input = FormUtil.readString(label);
		return input.length() == 0 ? oldValue: input;
	}

	public static int editValue(String type, int oldValue) {
		String label = type;
		if (oldValue != -1) {
			label += " (current: " + oldValue + ")";
		}
		return FormUtil.readInt(label);
	}

	public static boolean editValue(String type, boolean oldValue) {
		String oldValueString = oldValue ? "ja": "nein";
		String input = FormUtil.readString(type + " (default: \'" + oldValueString + "\')");
		return input.length() == 0 ? oldValue : input.equals("ja");
	}
}
