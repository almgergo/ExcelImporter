package core;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import model.TimeParts;

public class Helper {
	private static Logger logger = Logger.getLogger("Helper");

	public static String readUnkownCellDebrecen(final XSSFSheet mainSheet, final int row, final int col)
			throws Exception {
		int cellType = mainSheet.getRow(row).getCell(col).getCellType();
		switch (cellType) {
		case 0:
			break;
		}

		try {
			return Double.toString(mainSheet.getRow(row).getCell(col).getNumericCellValue());

		} catch (Exception e) {
			try {
				Calendar c = Calendar.getInstance();
				c.setTime(mainSheet.getRow(row).getCell(col).getDateCellValue());
				return c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
			} catch (Exception e1) {
				try {
					return mainSheet.getRow(row).getCell(col).getStringCellValue();
				} catch (Exception e2) {
					throw new Exception("Cell was not numeric or string or date");
				}
			}
		}
	}

	static String readUnkownCell(final XSSFSheet mainSheet, final int row, final int col) throws Exception {
		int cellType = mainSheet.getRow(row).getCell(col).getCellType();
		switch (cellType) {
		case 0:
			break;
		}

		try {
			return mainSheet.getRow(row).getCell(col).getStringCellValue();
		} catch (Exception e) {
			try {
				Calendar c = Calendar.getInstance();
				c.setTime(mainSheet.getRow(row).getCell(col).getDateCellValue());
				return c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
			} catch (Exception e1) {
				try {
					return Double.toString(mainSheet.getRow(row).getCell(col).getNumericCellValue());
				} catch (Exception e2) {
					throw new Exception("Cell was not numeric or string or date");
				}
			}
		}
	}

	public static void trunk(final Calendar time) {
		time.set(11, 0);
		time.set(12, 0);
		time.set(13, 0);
		time.set(14, 0);
	}

	public static TimeParts getTimeParts(String hour) throws NumberFormatException {
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
		TimeParts tp = null;
		try {
			new TimeParts(Integer.parseInt(parts[0]), 0);
		} catch (NumberFormatException e) {
			logger.log(Level.SEVERE, "error creating number from hour string: " + hour, e);
			throw new NumberFormatException("error formatting hour string: " + hour);
		}
		return tp;
	}

	public static void setCalendarTime(final Calendar time, final TimeParts parts, final int hourToSetTo) {
		int hours = parts.getHours();
		int minutes = parts.getMinutes();

		time.set(Calendar.HOUR_OF_DAY, hours);
		time.set(Calendar.MINUTE, minutes);
	}

}
