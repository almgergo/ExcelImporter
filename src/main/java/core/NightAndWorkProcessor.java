package core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.NightPerson;
import model.NightWorkedDay;
import model.Person;
import model.TimeParts;
import model.WorkTime;
import model.WorkType;

public class NightAndWorkProcessor {
	protected int calculationMonth;

	public NightAndWorkProcessor(int calcMonth) {
		this.calculationMonth = calcMonth;
	}

	public class PositionCoordinates {
		public static final int nameX = 1;
		public static final int nameY = 1;
	}

	private static Logger logger = Logger.getLogger("ExcelImporter");

	public void processWorkbook(String fileName, Sheet workSheet) throws Exception {
		logger.log(Level.INFO, "\nStarted processing " + fileName);

		NightPerson nightPerson = null;

		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);

		XSSFWorkbook workbook = new XSSFWorkbook(fis);

		// List<XSSFSheet> sheets = new ArrayList<XSSFSheet>();
		// for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
		// String sheetName = workbook.getSheetName(0);
		// if (sheetName != null && !"".equals(sheetName) && !"0".equals(sheetName)) {
		XSSFSheet sheet = workbook.getSheetAt(0);
		// }
		// }

		// XSSFSheet mainSheet = workbook.getSheet("Munka1");

		// Person p = new Person("", 0);
		// for (
		//
		// XSSFSheet sheet : sheets) {
		try {
			nightPerson = loadPerson(sheet);
			if (nightPerson == null) {
				workbook.close();
				throw new Exception("Person could not be initialized");
			}
			loadWorkedDays(sheet, nightPerson);
		} catch (final Exception e) {
			logger.log(Level.SEVERE,
					"Error while loading sheet: " + sheet.getSheetName() + " errormessage: " + e.getMessage(), e);
			return;
		}

		checkEligibility(nightPerson);

		countWorkHours(nightPerson);

		workbook.close();

		Row row = workSheet.createRow((short) (workSheet.getLastRowNum() + 1));

		row.createCell(0).setCellValue(nightPerson.getName());
		row.createCell(1).setCellValue(nightPerson.getWorkBonuses().getWorkExtra().getBonusHours());
		// row.createCell(2).setCellValue(nightPerson.getWorkBonuses().getWorkExtra().getBonus());

		row.createCell(4).setCellValue(nightPerson.getWorkBonuses().getDhlExtra().getBonusHours());
		row.createCell(5).setCellValue(nightPerson.getWorkBonuses().getDhlExtra().getBonus());

		row.createCell(7).setCellValue(nightPerson.getNightlyBonuses().getWorkExtra().getBonusHours());
		// row.createCell(8).setCellValue(nightPerson.getNightlyBonuses().getWorkExtra().getBonus());

		row.createCell(10).setCellValue(nightPerson.getNightlyBonuses().getDhlExtra().getBonusHours());
		row.createCell(11).setCellValue(nightPerson.getNightlyBonuses().getDhlExtra().getBonus());
		// }
		logger.log(Level.INFO, "Finished processing " + fileName);
	}

	protected void countWorkHours(Person person) throws Exception {
		person.countBonusHours();
	}

	protected void checkEligibility(Person person) {
		person.checkEligibilityForWorkSupport();
	}

	protected NightPerson loadPerson(XSSFSheet mainSheet) {
		int row = PositionCoordinates.nameX;
		int col = PositionCoordinates.nameY;

		String name = mainSheet.getRow(row).getCell(col).getStringCellValue();
		NightPerson person = new NightPerson(name, col);

		return person;
	}

	protected void loadWorkedDays(XSSFSheet mainSheet, NightPerson person) throws Exception {
		int row = 5;

		Integer day = null;
		Date date = null;
		Calendar c = null;

		try {
			day = (int) mainSheet.getRow(row).getCell(0).getNumericCellValue();
			c = Calendar.getInstance();
			c.set(Calendar.MONTH, calculationMonth);
			c.set(Calendar.DAY_OF_MONTH, day);

			date = c.getTime();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error in reading date from sheet", e);
		}

		while (date != null) {
			int startColumn = person.getStartColumn();
			int endColumn = person.getEndColumn();

			NightWorkedDay newDay = new NightWorkedDay();
			newDay.setDay(date);

			int shift = 0;
			String startHour = Helper.readUnkownCell(mainSheet, row, startColumn);
			String endHour = Helper.readUnkownCell(mainSheet, row, endColumn);
			if ((!"0.0".equals(startHour)) || (!"0.0".equals(endHour))) {
				newDay.addWorkTime(getDatesFromString(startHour, endHour, WorkType.RAKODAS));
			}

			shift += 3;
			startHour = Helper.readUnkownCell(mainSheet, row, startColumn + shift);
			endHour = Helper.readUnkownCell(mainSheet, row, endColumn + shift);
			if ((!"0.0".equals(startHour)) || (!"0.0".equals(endHour))) {
				newDay.addWorkTime(getDatesFromString(startHour, endHour, WorkType.DHL));
			}

			shift += 3;
			startHour = Helper.readUnkownCell(mainSheet, row, startColumn + shift);
			endHour = Helper.readUnkownCell(mainSheet, row, endColumn + shift);
			if ((!"0.0".equals(startHour)) || (!"0.0".equals(endHour))) {
				newDay.addWorkTime(getDatesFromString(startHour, endHour, WorkType.RAKODAS));
			}

			if (!newDay.getWorkTimes().isEmpty()) {
				person.addNightWorkedDay(newDay);
			}
			row++;
			try {
				day = (int) mainSheet.getRow(row).getCell(0).getNumericCellValue();
				if (day == null || day <= 0 || day > 31) {
					break;
				} else {
					c.set(Calendar.DAY_OF_MONTH, day);
					date = c.getTime();
				}
			} catch (Exception e) {
				date = null;
			}
		}
	}

	public WorkTime getDatesFromString(String startHour, String endHour, WorkType workType) {
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();

		Helper.trunk(startTime);
		Helper.trunk(endTime);

		startTime.set(Calendar.MONTH, calculationMonth);
		endTime.set(Calendar.MONTH, calculationMonth);

		TimeParts startParts = Helper.getTimeParts(startHour);
		TimeParts endParts = Helper.getTimeParts(endHour);
		if (startParts.getHours() >= endParts.getHours()) {
			endTime.add(Calendar.DAY_OF_MONTH, 1);
		}
		Helper.setCalendarTime(startTime, startParts, 18);
		Helper.setCalendarTime(endTime, endParts, 6);

		return new WorkTime(startTime.getTime(), endTime.getTime(), workType);
	}

	public static void addHeaderToSheet(Sheet workSheet) {
		Row row = workSheet.createRow((short) workSheet.getLastRowNum());
		row.createCell(0).setCellValue("Név");
		row.createCell(1).setCellValue("Óraszám");
		row.createCell(2).setCellValue("Bérpótlék (HUF)");
	}
}
