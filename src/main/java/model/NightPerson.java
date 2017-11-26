package model;

import java.util.ArrayList;
import java.util.List;

public class NightPerson extends Person {
	public static final double DHL_RATE = 420;
	public static final double DHL_NIGHT_RATE = 420;

	private double bonus;
	private double bonusHours = 0;
	private long bonusMinutes = 0;

	private double dhlBonus;
	private double dhlBonusHours = 0;
	private long dhlBonusMinutes = 0;

	protected List<NightWorkedDay> nightWorkedDays;

	public NightPerson(String name, int col) {
		super(name, col);
		nightWorkedDays = new ArrayList<NightWorkedDay>();
	}

	@Override
	public void countBonusHours() throws Exception {
		if (isEligible()) {
			for (NightWorkedDay nwd : getNigthWorkedDays()) {
				BonusMinutes bonusMinutesContainer = nwd.getExtendedBonusMinutes();
				// long wdDhlBonusMinutes = nwd.getDhlBonusMinutes();
				bonusMinutes += bonusMinutesContainer.getStorageBonus();
				dhlBonusMinutes += bonusMinutesContainer.getDhlBonus();
				System.out.println(nwd.getDay() + " day has " + bonusMinutes + " STORAGE bonus minutes, that is "
						+ 1.0 * bonusMinutes / 60 + " bonus hours. " + nwd.getTimes());
				System.out.println(nwd.getDay() + " day has " + dhlBonusMinutes + " DHL bonus minutes, that is "
						+ 1.0 * dhlBonusMinutes / 60 + " bonus hours. " + nwd.getTimes());
			}
			bonusHours = 1.0 * bonusMinutes / 60;
			bonus = Math.round(bonusHours * RATE);

			dhlBonusHours = 1.0 * dhlBonusMinutes / 60;
			dhlBonus = Math.round(dhlBonusHours * DHL_RATE);
		}
	}

	public List<NightWorkedDay> getNigthWorkedDays() {
		return nightWorkedDays;
	}

	public void addNightWorkedDay(NightWorkedDay nwd) {
		this.nightWorkedDays.add(nwd);
	}

	@Override
	public List<WorkedDay> getDaysWorked() {
		if (daysWorked == null || daysWorked.isEmpty()) {
			for (NightWorkedDay nwd : nightWorkedDays) {
				WorkedDay wd = nwd.getWorkedDay();
				if (wd != null) {
					daysWorked.add(wd);
				}
			}
		}
		return daysWorked;
	}

	@Override
	public double getBonus() {
		return bonus;
	}

	public void setBonus(double bonus) {
		this.bonus = bonus;
	}

	@Override
	public double getBonusHours() {
		return bonusHours;
	}

	@Override
	public void setBonusHours(double bonusHours) {
		this.bonusHours = bonusHours;
	}

	public double getDhlBonus() {
		return dhlBonus;
	}

	public void setDhlBonus(double dhlBonus) {
		this.dhlBonus = dhlBonus;
	}

	public double getDhlBonusHours() {
		return dhlBonusHours;
	}

	public void setDhlBonusHours(double dhlBonusHours) {
		this.dhlBonusHours = dhlBonusHours;
	}

}
