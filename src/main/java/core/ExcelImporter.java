package core;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.toedter.calendar.JMonthChooser;

import model.NameCoordinate;
import model.Person;
import model.TimeParts;
import model.WorkedDay;

public class ExcelImporter implements Runnable {
	private static ExcelImporter excelImporter;

	private static final String CHOOSE_DIRECTORY = "Mappa választása";
	private static final String NO_DIRECTORY_CHOSEN = "Válassza ki az .xlsx-eket tartalmazó mappát";
	private static final String NO_OUT_DIRECTORY_CHOSEN = "Válassza ki a kész fájl mentésének helyét";
	private static final String PROCESSING = "Adatok feldolgozás alatt...";
	private static final String FINISHED = "<font color='green'>Feldolgozás befejezve</font>";
	private static final String GENERATED_FILE_NAME = "\\Kiszámolt Bérpótlékok.xlsx";
	private static final String logFileName = "\\ExcelImporter.log";
	private static final int START_ROW = 5;
	private static final int FIRST_HOUR = 18;
	private static final int FINAL_HOUR = 6;

	private static final int Y_START = 1;
	private static final int X_START = 1;
	private static final int X_RES = 7;
	private static final int Y_RES = 41;

	private static final int ROW_SHIFT = 4;

	private static final Object SHEET_TO_PROCESS = "jelenlétik";

	private JButton startCalc = new JButton("Adatok feldolgozása");
	private JButton selectDir = new JButton("Mappa kiválasztása");
	private JButton selectOutDir = new JButton("Kimeneti mappa kiválasztása");
	private JFrame frame;
	private JLabel label;
	private JLabel outLabel;
	private JLabel resultLabel;
	private JComboBox<ProcessMode> modeCombo;
	private JFileChooser chooser;
	// private JXDatePicker picker;
	private File dir;
	private JMonthChooser monthChooser;
	private File outDir = new File(System.getProperty("user.dir"));
	private String handlerPath = System.getProperty("user.dir") + "\\ExcelImporter.log";
	private static Logger logger = Logger.getLogger("ExcelImporter");
	private static FileHandler handler;
	protected List<NameCoordinate> nameCoords;
	protected int calculationMonth;

	protected ProcessMode mode;

	public String getMonth(int month) {
		return new DateFormatSymbols().getMonths()[month];
	}

	public static ExcelImporter getInstance() throws Exception {
		if (excelImporter == null) {
			excelImporter = new ExcelImporter();
		}
		return excelImporter;
	}

	private ExcelImporter() throws Exception {
		this.frame = new JFrame("Bérpótlék számolás");
		this.frame.setDefaultCloseOperation(3);

		this.frame.setPreferredSize(new Dimension(800, 300));

		this.frame.setVisible(true);

		GridLayout layout = new GridLayout(4, 4);
		this.frame.setLayout(layout);

		this.label = new JLabel("Válassza ki az .xlsx-eket tartalmazó mappát");

		this.outLabel = new JLabel("Válassza ki a kész fájl mentésének helyét");

		monthChooser = new JMonthChooser();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -1);
		monthChooser.setMonth(c.get(Calendar.MONTH));

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
		this.dir = new File(System.getProperty("user.dir"));

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
						ExcelImporter.this.outLabel.setText(ExcelImporter.this.chooser.getSelectedFile().toString());
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

		this.startCalc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					ExcelImporter.this.startCalc.setEnabled(false);
					ExcelImporter.this.calculationMonth = monthChooser.getMonth();
					new Thread(ExcelImporter.getInstance()).start();
				} catch (Exception e) {
					ExcelImporter.this.resultLabel
							.setText("<html> <font color='red'>Hiba a feldolgozás során</font></html>");
				}
			}
		});

		this.resultLabel = new JLabel("");

		// List<String> comboValues = new ArrayList<String>();
		// for (ProcessMode pm : ProcessMode.values()) {
		// comboValues.add(pm.getName());
		// }

		this.modeCombo = new JComboBox<ProcessMode>(ProcessMode.values());

		this.frame.add(this.label);
		this.frame.add(this.outLabel);
		this.frame.add(this.selectDir);
		this.frame.add(this.selectOutDir);
		this.frame.add(this.startCalc);
		this.frame.add(this.resultLabel);
		this.frame.add(this.monthChooser);
		this.frame.add(this.modeCombo);

		this.frame.pack();
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
						switch ((ProcessMode) modeCombo.getSelectedItem()) {
						case MUSZAKPOTLEK:
							processWorkbook(child.getPath(), workSheet);
							break;
						case EJSZAKAIESMUSZAKPOTLEK:
							new NightAndWorkProcessor(calculationMonth).processWorkbook(child.getPath(), workSheet);
							break;
						default:
							break;
						}
						logger.log(Level.INFO, "Successfully processed " + child.toString());

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
		logger.log(Level.INFO, "\nStarted processing " + fileName);

		List<Person> people = new LinkedList<Person>();

		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);

		XSSFWorkbook workbook = new XSSFWorkbook(fis);

		List<XSSFSheet> sheets = new ArrayList<XSSFSheet>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String sheetName = workbook.getSheetName(i);
			if (sheetName != null && !"".equals(sheetName) && !"0".equals(sheetName)) {
				// if (sheetName != null && SHEET_NAME.equals(sheetName)) {
				sheets.add(workbook.getSheet(sheetName));
			}
		}

		for (XSSFSheet sheet : sheets) {
			if (SHEET_TO_PROCESS.equals(sheet.getSheetName())) {
				try {

					int y = Y_START;

					boolean ymore = true;
					boolean xmore = true;

					int x = X_START;
					while (xmore || ymore) {
						Person person = new Person();
						if (loadPeople(person, sheet, people, x, y)) {
							loadWorkedDays(sheet, person);
							xmore = true;
							x += X_RES;
						} else if (xmore) {
							xmore = false;
							y += Y_RES;
							x = X_START;
						} else if (ymore) {
							ymore = false;
						}
						int i = 10;
					}

				} catch (final Exception e) {
					logger.log(Level.SEVERE, "Error while processing sheet: " + sheet.getSheetName() + " errormessage: "
							+ e.getMessage(), e);
				}
			} else {
				logger.log(Level.INFO, "not processing sheet: " + sheet.getSheetName());
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
			person.checkEligibilityForWorkSupport();
		}
	}

	protected boolean loadPeople(Person person, final XSSFSheet mainSheet, List<Person> people, final int col,
			final int row) {
		String name = null;
		try {
			name = mainSheet.getRow(row).getCell(col).getStringCellValue();
		} catch (final NullPointerException e) {
			return false;
		}
		if (name != null && !"".equals(name)) {
			person.setName(name);
			person.setColumn(col);
			person.setRow(row);

			people.add(person);
			return true;
		}
		return false;
	}

	protected void loadWorkedDays(XSSFSheet mainSheet, Person person) throws Exception {
		int row = person.getRow() + ROW_SHIFT;
		Integer day = (int) mainSheet.getRow(row).getCell(0).getNumericCellValue();

		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, calculationMonth);
		c.set(Calendar.DAY_OF_MONTH, day);
		// c.add(Calendar.MONTH, -1);

		Date date = c.getTime();
		while (date != null) {
			// for (Person person : people) {
			int startColumn = person.getStartColumn();
			int endColumn = person.getEndColumn();

			String startHour = Helper.readUnkownCellDebrecen(mainSheet, row, startColumn);
			String endHour = Helper.readUnkownCellDebrecen(mainSheet, row, endColumn);
			if ((!"0.0".equals(startHour) || !"0.0".equals(endHour))
					&& (!"".equals(startHour) && !"".equals(endHour))) {

				WorkedDay newDay = null;
				try {
					newDay = getDatesFromString(startHour, endHour);
				} catch (final NumberFormatException e) {
					logger.log(Level.SEVERE, "startHour: " + startHour + ", endHour: " + endHour + ", at row: " + row
							+ ", col: " + startColumn, e);
				}
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

	public WorkedDay getDatesFromString(String startHour, String endHour) throws NumberFormatException {
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();

		Helper.trunk(startTime);
		Helper.trunk(endTime);
		TimeParts startParts = Helper.getTimeParts(startHour);
		TimeParts endParts = Helper.getTimeParts(endHour);
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

	public static void addHeaderToSheet(Sheet workSheet) {
		Row row = workSheet.createRow((short) workSheet.getLastRowNum());
		row.createCell(0).setCellValue("Név");
		row.createCell(1).setCellValue("Műszakpótlék óraszám");
		row.createCell(2).setCellValue("Műszakpótlék (HUF)");

		row.createCell(4).setCellValue("Műszakpótlék DHL óraszám");
		row.createCell(5).setCellValue("Műszakpótlék DHL (HUF)");

		row.createCell(7).setCellValue("Éjszakai pótlék óraszám");
		row.createCell(8).setCellValue("Éjszakai pótlék (HUF)");

		row.createCell(10).setCellValue("Éjszakai pótlék DHL óraszám");
		row.createCell(11).setCellValue("Éjszakai pótlék DHL (HUF)");
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
