package model;

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