package core;

public enum ProcessMode {
	MUSZAKPOTLEK("Debreceni műszakpótlék"), EJSZAKAIESMUSZAKPOTLEK("Sky éjszakai pótlék és műszakpótlék");

	String name;

	private ProcessMode(String v) {
		this.name = v;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
