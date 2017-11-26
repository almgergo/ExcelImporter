package core;

import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import model.TimeParts;

public class Helper {

	public static String readUnkownCell(XSSFSheet mainSheet, int row, int col) throws Exception {
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(mainSheet.getRow(row).getCell(col).getDateCellValue());
			return c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
		} catch (Exception e) {
			try {
				return Double.toString(mainSheet.getRow(row).getCell(col).getNumericCellValue());
			} catch (Exception e1) {
				try {
					return mainSheet.getRow(row).getCell(col).getStringCellValue();
				} catch (Exception e2) {
					throw new Exception("Cell was not numeric or String");
				}
			}
		}
	}

	public static void trunk(Calendar time) {
		time.set(11, 0);
		time.set(12, 0);
		time.set(13, 0);
		time.set(14, 0);
	}

	public static TimeParts getTimeParts(String hour) {
		hour = hour.trim();
		String[] parts = hour.split("[.]");
		if (1 == parts.length) {
			parts = hour.split("[:]");
		}
		if (1 == parts.length) {
			parts = hour.split("[,]");
		}
		if (1 == parts.length) {
			parts = hour.split("[_]");
		}
		if (1 == parts.length) {
			parts = hour.split("[ ]");
		}
		if (parts.length > 1) {
			return new TimeParts(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		return new TimeParts(Integer.parseInt(parts[0]), 0);
	}

	public static void setCalendarTime(Calendar time, TimeParts parts, int hourToSetTo) {
		int hours = parts.getHours();
		int minutes = parts.getMinutes();

		time.set(Calendar.HOUR_OF_DAY, hours);
		time.set(Calendar.MINUTE, minutes);
	}

}
