package core;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.Person;
import model.WorkedDay;

public class ExcelImporter implements Runnable {
	private static ExcelImporter excelImporter;
	private static final String CHOOSE_DIRECTORY = "Mappa választása";
	private static final String NO_DIRECTORY_CHOSEN = "Válassza ki az .xlsx-eket tartalmazó mappát";
	private static final String NO_OUT_DIRECTORY_CHOSEN = "Válassza ki a kész fájl mentésének helyét";
	private static final String PROCESSING = "Adatok feldolgozás alatt...";
	private static final String FINISHED = "<font color='green'>Feldolgozás befejezve</font>";
	private static final String GENERATED_FILE_NAME = "\\Kiszámolt Bérpótlékok.xlsx";
	private static final int START_ROW = 5;
	private static final int FIRST_HOUR = 18;
	private static final int FINAL_HOUR = 6;
	private JButton startCalc = new JButton("Adatok feldolgozása");
	private JButton selectDir = new JButton("Mappa kiválasztása");
	private JButton selectOutDir = new JButton("Kimeneti mappa kiválasztása");
	private JFrame frame;
	private JLabel label;
	private JLabel outLabel;
	private JLabel resultLabel;
	private JFileChooser chooser;
	private File dir;
	private File outDir = new File(System.getProperty("user.dir"));
	private String handlerPath = System.getProperty("user.dir") + "\\ExcelImporter.log";
	private static final String logFileName = "\\ExcelImporter.log";
	private static Logger logger = Logger.getLogger("ExcelImporter");
	private static FileHandler handler;
	protected List<NameCoordinate> nameCoords;

	public static ExcelImporter getInstance() throws Exception {
		if (excelImporter == null) {
			excelImporter = new ExcelImporter();
		}
		return excelImporter;
	}

	private ExcelImporter() throws Exception {
		this.frame = new JFrame("Bérpótlék számolás");
		this.frame.setDefaultCloseOperation(3);

		this.frame.setPreferredSize(new Dimension(800, 200));
		this.frame.pack();
		this.frame.setVisible(true);

		GridLayout layout = new GridLayout(3, 3);
		this.frame.setLayout(layout);

		this.label = new JLabel("Válassza ki az .xlsx-eket tartalmazó mappát");
		this.frame.add(this.label);

		this.outLabel = new JLabel("Válassza ki a kész fájl mentésének helyét");
		this.frame.add(this.outLabel);

		this.selectDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ExcelImporter.this.chooser = new JFileChooser();
				ExcelImporter.this.chooser.setCurrentDirectory(new File("."));
				ExcelImporter.this.chooser.setDialogTitle("Mappa választása");
				ExcelImporter.this.chooser.setFileSelectionMode(1);

				ExcelImporter.this.chooser.setAcceptAllFileFilterUsed(false);
				if (ExcelImporter.this.chooser.showOpenDialog(ExcelImporter.this.frame) == 0) {
					ExcelImporter.this.dir = ExcelImporter.this.chooser.getSelectedFile();
					ExcelImporter.this.label.setText(ExcelImporter.this.chooser.getSelectedFile().toString());
				} else {
					System.out.println("No Selection ");
				}
			}
		});
		this.frame.add(this.selectDir);

		this.selectOutDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ExcelImporter.this.chooser = new JFileChooser();
				ExcelImporter.this.chooser.setCurrentDirectory(new File("."));
				ExcelImporter.this.chooser.setDialogTitle("Mappa választása");
				ExcelImporter.this.chooser.setFileSelectionMode(1);

				ExcelImporter.this.chooser.setAcceptAllFileFilterUsed(false);
				if (ExcelImporter.this.chooser.showOpenDialog(ExcelImporter.this.frame) == 0) {
					if (ExcelImporter.this.chooser.getSelectedFile().isDirectory()) {
						ExcelImporter.this.outDir = ExcelImporter.this.chooser.getSelectedFile();
					} else {
						ExcelImporter.this.outDir = ExcelImporter.this.chooser.getCurrentDirectory();
						ExcelImporter.this.outLabel.setText(ExcelImporter.this.chooser.getSelectedFile().toString());
					}
					try {
						ExcelImporter.this.handlerPath = (ExcelImporter.this.outDir.getPath() + "\\ExcelImporter.log");
					} catch (SecurityException e1) {
						e1.printStackTrace();
					}
				} else {
					System.out.println("No Selection ");
				}
			}
		});
		this.frame.add(this.selectOutDir);

		this.startCalc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					ExcelImporter.this.startCalc.setEnabled(false);
					new Thread(ExcelImporter.getInstance()).start();
				} catch (Exception e) {
					ExcelImporter.this.resultLabel
							.setText("<html> <font color='red'>Hiba a feldolgozás során</font></html>");
				}
			}
		});
		this.frame.add(this.startCalc);

		this.resultLabel = new JLabel("");
		this.frame.add(this.resultLabel);
	}

	public void processWorkbooks(File dir) throws IOException {
		handler = new FileHandler(this.handlerPath);
		handler.setEncoding("UTF-8");
		SimpleFormatter formatter = new SimpleFormatter();
		handler.setFormatter(formatter);

		logger.addHandler(handler);

		this.label.setText("Adatok feldolgozás alatt...");
		this.nameCoords = new LinkedList();
		this.nameCoords.add(new NameCoordinate(1, 1));
		// this.nameCoords.add(new NameCoordinate(1, 6));
		// this.nameCoords.add(new NameCoordinate(1, 10));

		Workbook wb = new XSSFWorkbook();
		Sheet workSheet = wb.createSheet("Berpótlék");

		addHeaderToSheet(workSheet);
		StringBuilder finalMessage = new StringBuilder("<html>");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if ((!child.isDirectory()) && ("xlsx".equals(FilenameUtils.getExtension(child.toString())))
						&& (!child.toString().contains("\\Kiszámolt Bérpótlékok.xlsx"))) {
					try {
						processWorkbook(child.getPath(), workSheet);
					} catch (Exception e) {
						finalMessage
								.append("<font color='red'>Hiba a " + child + " fájl feldolgozása során.</font><br> ");
						logger.log(Level.SEVERE, "Error processing " + child.getName() + ", " + e.getMessage(), e);
					}
				}
			}
		}
		String outFile;
		if (this.outDir != null) {
			outFile = this.outDir.toString() + "\\Kiszámolt Bérpótlékok.xlsx";
		} else {
			outFile = new File(".").toString() + "\\Kiszámolt Bérpótlékok.xlsx";
		}
		FileOutputStream fileOut = new FileOutputStream(outFile);
		wb.write(fileOut);
		fileOut.close();
		wb.close();

		this.label.setText("Válassza ki az .xlsx-eket tartalmazó mappát");
		finalMessage.append("<font color='green'>Feldolgozás befejezve</font></html>");
		this.resultLabel.setText(finalMessage.toString());

		handler.close();
		logger.removeHandler(handler);
	}

	public void processWorkbook(String fileName, Sheet workSheet) throws Exception {
		logger.log(Level.INFO, "Started processing " + fileName);

		List<Person> people = new LinkedList<Person>();

		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);

		XSSFWorkbook workbook = new XSSFWorkbook(fis);

		List<XSSFSheet> sheets = new ArrayList<XSSFSheet>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String sheetName = workbook.getSheetName(i);
			if (sheetName != null && !"".equals(sheetName) && !"0".equals(sheetName)) {
				sheets.add(workbook.getSheet(sheetName));
			}
		}

		// XSSFSheet mainSheet = workbook.getSheet("Munka1");

		// Person p = new Person("", 0);
		for (XSSFSheet sheet : sheets) {
			try {
				Person p = loadPeople(sheet, people);
				loadWorkedDays(sheet, p);
			} catch (final Exception e) {
				logger.log(Level.SEVERE,
						"Error while loading sheet: " + sheet.getSheetName() + " errormessage: " + e.getMessage(), e);
			}
		}

		checkEligibility(people);

		countWorkHours(people);

		workbook.close();
		for (Person person : people) {
			Row row = workSheet.createRow((short) (workSheet.getLastRowNum() + 1));

			row.createCell(0).setCellValue(person.getName());
			row.createCell(1).setCellValue(person.getBonusHours());
			row.createCell(2).setCellValue(person.getBonus());
		}
		logger.log(Level.INFO, "Finished processing " + fileName);
	}

	protected void countWorkHours(List<Person> people) throws Exception {
		for (Person person : people) {
			person.countBonusHours();
		}
	}

	protected void checkEligibility(List<Person> people) {
		for (Person person : people) {
			person.checkEligibility();
		}
	}

	protected Person loadPeople(XSSFSheet mainSheet, List<Person> people) {
		// for (NameCoordinate nc : this.nameCoords) {
		int row = this.nameCoords.get(0).getRow();
		int col = this.nameCoords.get(0).getCol();

		String name = mainSheet.getRow(row).getCell(col).getStringCellValue();
		Person person = new Person(name, col);

		people.add(person);
		return person;
		// }

	}

	protected void loadWorkedDays(XSSFSheet mainSheet, Person person) throws Exception {
		int row = 5;
		Integer day = (int) mainSheet.getRow(row).getCell(0).getNumericCellValue();

		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, day);
		c.add(Calendar.MONTH, -1);

		Date date = c.getTime();
		while (date != null) {
			// for (Person person : people) {
			int startColumn = person.getStartColumn();
			int endColumn = person.getEndColumn();

			String startHour = readUnkownCell(mainSheet, row, startColumn);
			String endHour = readUnkownCell(mainSheet, row, endColumn);
			if ((!"0.0".equals(startHour)) || (!"0.0".equals(endHour))) {
				WorkedDay newDay = getDatesFromString(startHour, endHour);
				newDay.setDay(date);
				person.addWorkedDay(newDay);
			}
			// }
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

	protected String readUnkownCell(XSSFSheet mainSheet, int row, int col) throws Exception {
		try {
			return Double.toString(mainSheet.getRow(row).getCell(col).getNumericCellValue());
		} catch (IllegalStateException localIllegalStateException) {
			try {
				return mainSheet.getRow(row).getCell(col).getStringCellValue();
			} catch (IllegalStateException localIllegalStateException1) {
				throw new Exception("Cell was not either numeric or String");
			}
		}
	}

	protected void trunk(Calendar time) {
		time.set(11, 0);
		time.set(12, 0);
		time.set(13, 0);
		time.set(14, 0);
	}

	public WorkedDay getDatesFromString(String startHour, String endHour) {
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();

		trunk(startTime);
		trunk(endTime);

		TimeParts startParts = getTimeParts(startHour);
		TimeParts endParts = getTimeParts(endHour);
		if (startParts.getHours() >= endParts.getHours()) {
			endTime.add(5, 1);
		}
		setCalendarTime(startTime, startParts, 18);
		setCalendarTime(endTime, endParts, 6);

		return new WorkedDay(startTime.getTime(), endTime.getTime());
	}

	protected void setCalendarTime(Calendar time, TimeParts parts, int hourToSetTo) {
		int hours = parts.getHours();
		int minutes = parts.getMinutes();

		time.set(11, hours);
		time.set(12, minutes);
	}

	protected TimeParts getTimeParts(String hour) {
		String[] parts = hour.split("[.]");
		if (0 == parts.length) {
			parts = hour.split("[:]");
		} else if (0 == parts.length) {
			parts = hour.split("[,]");
		} else if (0 == parts.length) {
			parts = hour.split(" ");
		}
		if (parts.length > 1) {
			return new TimeParts(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
		return new TimeParts(Integer.parseInt(parts[0]), 0);
	}

	public class TimeParts {
		private int hours;
		private int minutes;

		public TimeParts(int h, int m) {
			this.hours = h;
			this.minutes = m;
		}

		public int getHours() {
			return this.hours;
		}

		public void setHours(int hours) {
			this.hours = hours;
		}

		public int getMinutes() {
			return this.minutes;
		}

		public void setMinutes(int minutes) {
			this.minutes = minutes;
		}
	}

	public class NameCoordinate {
		protected int row;
		protected int col;

		public NameCoordinate(int row, int col) {
			this.row = row;
			this.col = col;
		}

		public int getRow() {
			return this.row;
		}

		public void setRow(int row) {
			this.row = row;
		}

		public int getCol() {
			return this.col;
		}

		public void setCol(int col) {
			this.col = col;
		}
	}

	public static void addHeaderToSheet(Sheet workSheet) {
		Row row = workSheet.createRow((short) workSheet.getLastRowNum());
		row.createCell(0).setCellValue("Név");
		row.createCell(1).setCellValue("Óraszám");
		row.createCell(2).setCellValue("Bérpótlék (HUF)");
	}

	public void run() {
		try {
			processWorkbooks(this.dir);
			this.startCalc.setEnabled(true);
		} catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage(), e);
			this.startCalc.setEnabled(true);
			handler.close();
			logger.removeHandler(handler);
		}
	}
}