package model;

public class ExtraContainer {
	protected Extra workExtra;
	protected Extra dhlExtra;

	public ExtraContainer() {
		workExtra = new Extra(WorkType.RAKODAS);
		dhlExtra = new Extra(WorkType.DHL);
	}

	public void addExtraMinutes(BonusMinutes bm) {
		getWorkExtra().addBonusMinutes(bm.getStorageBonus());
		getDhlExtra().addBonusMinutes(bm.getDhlBonus());
	}

	public Extra getWorkExtra() {
		return workExtra;
	}

	public void setWorkExtra(Extra workExtra) {
		this.workExtra = workExtra;
	}

	public Extra getDhlExtra() {
		return dhlExtra;
	}

	public void setDhlExtra(Extra dhlExtra) {
		this.dhlExtra = dhlExtra;
	}

}
