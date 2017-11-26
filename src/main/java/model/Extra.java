package model;

public class Extra {
	protected double bonus = 0;
	protected double bonusHours = 0;
	protected long bonusMinutes = 0;
	protected WorkType workType;

	public Extra(WorkType wt) {
		this.workType = wt;
	}

	public double getBonus() {
		return bonus;
	}

	public void setBonus(double bonus) {
		this.bonus = bonus;
	}

	public double getBonusHours() {
		return bonusHours;
	}

	public void setBonusHours(double bonusHours) {
		this.bonusHours = bonusHours;
	}

	public long getBonusMinutes() {
		return bonusMinutes;
	}

	public void setBonusMinutes(long bonusMinutes) {
		this.bonusMinutes = bonusMinutes;
	}

	public WorkType getWorkType() {
		return workType;
	}

	public void setWorkType(WorkType workType) {
		this.workType = workType;
	}

	@Override
	public String toString() {
		return "[" + "workType: " + workType.toString() + ", bonus hours: " + bonusHours + "]";
	}

	public void addBonusMinutes(long bonusMinutes) {
		this.bonusMinutes += bonusMinutes;
	}

	public void calculateBonus(final double RATE) {
		bonusHours = 1.0 * bonusMinutes / 60;
		bonus = Math.round(bonusHours * RATE);

	}

}
