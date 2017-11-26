package model;

import java.util.ArrayList;
import java.util.List;

public class Person {
	public static final double RATE = 277.8;

	protected static final int FOUR_HOUR_LIMIT = 1;

	protected List<WorkedDay> daysWorked = new ArrayList<WorkedDay>();
	protected String name;
	protected double bonus;
	protected double bonusHours = 0;
	protected long bonusMinutes = 0;

	protected boolean isEligible;
	protected int column;

	public Person(final String name, final int col) {
		this.name = name;
		this.column = col;
	}

	public void countBonusHours() throws Exception {
		if (isEligible) {
			for (WorkedDay wd : getDaysWorked()) {
				long wdBonusMinutes = wd.getBonusMinutes();
				bonusMinutes += wdBonusMinutes;
				System.out.println(wd.getDay() + " day has " + wdBonusMinutes + " bonus minutes, that is "
						+ 1.0 * wdBonusMinutes / 60 + " bonus hours. " + wd.getStartTime() + " " + wd.getEndTime());
			}
			bonusHours = 1.0 * bonusMinutes / 60;
			bonus = Math.round(bonusHours * RATE);
		}
	}

	public void checkEligibilityForWorkSupport() {
		final int limit = (int) Math.round(1.0 * getDaysWorked().size() / 3);

		for (int current = 0; current < getDaysWorked().size(); current++) {
			WorkedDay cwd = getDaysWorked().get(current);
			for (int other = current; other < getDaysWorked().size(); other++) {
				WorkedDay owd = getDaysWorked().get(other);
				cwd.isDifferentStart(owd);
			}
		}

		// if each start has 1/3 difference
		isEligible = true;
		boolean hasFourHourDifference = false;
		for (WorkedDay wd : getDaysWorked()) {
			if (wd.getDifferentStarts() < limit) {
				isEligible = false;
			}
			if (wd.getFourHourDifferenceIsMet() >= FOUR_HOUR_LIMIT) {
				hasFourHourDifference = true;
			}
		}
		isEligible = isEligible && hasFourHourDifference;

	}

	public boolean isEligible() {
		return isEligible;
	}

	public void setEligible(boolean isEligible) {
		this.isEligible = isEligible;
	}

	public void addWorkedDay(final WorkedDay wd) {
		daysWorked.add(wd);
	}

	public double getHoursWorked() {
		return bonus;
	}

	public void setHoursWorked(double hoursWorked) {
		this.bonus = hoursWorked;
	}

	public double getBonusHours() {
		return bonusHours;
	}

	public void setBonusHours(double bonusHours) {
		this.bonusHours = bonusHours;
	}

	public List<WorkedDay> getDaysWorked() {
		return daysWorked;
	}

	public String getName() {
		return name;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getStartColumn() {
		return column;
	}

	public int getEndColumn() {
		return (column + 1);
	}

	public double getBonus() {
		return bonus;
	}

	public String printPersonData() {
		return name + "\t" + bonusHours + "\t" + Math.round(bonusHours * RATE);

	}

}
