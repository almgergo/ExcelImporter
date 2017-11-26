package model;

public class BonusMinutes {

	protected long storageBonus;
	protected long dhlBonus;

	public BonusMinutes(long s, long d) {
		this.storageBonus = s;
		this.dhlBonus = d;
	}

	public long getStorageBonus() {
		return storageBonus;
	}

	public void setStorageBonus(long storageBonus) {
		this.storageBonus = storageBonus;
	}

	public long getDhlBonus() {
		return dhlBonus;
	}

	public void setDhlBonus(long dhlBonus) {
		this.dhlBonus = dhlBonus;
	}
}
