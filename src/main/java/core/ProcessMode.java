package core;

public enum ProcessMode {
	MUSZAKPOTLEK("Sima műszakpótlék"), EJSZAKAIESMUSZAKPOTLEK("Éjszakai pótlék és műszakpótlék");

	String name;

	private ProcessMode(String v) {
		this.name = v;
	}

}
