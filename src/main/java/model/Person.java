package model;

import java.util.ArrayList;
import java.util.List;

public class Person {
	public static final double RATE = 277.8;
	private static final int FOUR_HOUR_LIMIT = 1;

	private List<WorkedDay> daysWorked = new ArrayList<WorkedDay>();
	private String name;
	private double bonus;
	private double bonusHours = 0;
	private long bonusMinutes = 0;
	
	private boolean isEligible;
	private int column;
	
	public Person(final String name, final int col){
		this.name = name;
		this.column = col;
	}
	
	public void countBonusHours() throws Exception{
		if (isEligible){
			int dayNo = 0;
			for (WorkedDay wd : daysWorked){
				long wdBonusMinutes = wd.getBonusMinutes();
				bonusMinutes += wdBonusMinutes;
							System.out.println(wd.getDay() + " day has " + wdBonusMinutes + " bonus minutes, that is " + 1.0*wdBonusMinutes / 60 + " bonus hours. " + wd.getStartTime() + " " + wd.getEndTime());
				dayNo++;
			}
			bonusHours = 1.0 * bonusMinutes / 60;
			bonus = Math.round(bonusHours * RATE);
		}
	}
	
	public void checkEligibility(){
		final int limit = (int) Math.round(1.0*daysWorked.size()/3);
		
		for (int current = 0; current<daysWorked.size();current++)
		{
			WorkedDay cwd = daysWorked.get(current);
			for (int other = current; other<daysWorked.size();other++)
			{
				WorkedDay owd = daysWorked.get(other);
				cwd.isDifferentStart(owd);
			}
		}
		
		// if each start has to have 1/3 difference
		isEligible = true;
		boolean hasFourHourDifference = false;
		for (WorkedDay wd : daysWorked)
		{
			if (wd.getDifferentStarts()<limit){
				isEligible=false;
			}
			if (wd.getFourHourDifferenceIsMet() >= FOUR_HOUR_LIMIT){
				hasFourHourDifference = true;
			}
		}
		isEligible = isEligible && hasFourHourDifference;
		
		
		// if only 1 start has to have 1/3 difference
//		boolean hasFourHourDifference = false;
//		boolean hasOneThirdDifferentDays = false;
//		for (WorkedDay wd : daysWorked)
//		{
//			if (wd.getFourHourDifferenceIsMet() > FOUR_HOUR_LIMIT){
//				hasFourHourDifference = true;
//			}
//			if (wd.getDifferentStarts() >= limit){
//				hasOneThirdDifferentDays = true;
//			}
//			 
//			if (hasOneThirdDifferentDays && hasFourHourDifference)
//				break;
//		}
//		isEligible = hasOneThirdDifferentDays && hasFourHourDifference;
	}
	
	public void addWorkedDay(final WorkedDay wd){
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
	
	public int getStartColumn(){
		return column;
	}
	
	public int getEndColumn(){
		return (column+1);
	}
	
	public double getBonus() {
		return bonus;
	}
	
	public String printPersonData(){
		return name + "\t" + bonusHours + "\t" + Math.round(bonusHours*RATE);

	}

	
}
