package model;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WorkedDay {
	public static final int MIN_DIFFERENCE_IN_MINUTES = 4*60;
	public static final int HOURS_IN_A_DAY = 24;
	public static final int MINUTE_RESOLUTION = 1; // hour step size
//	public static final double MINUTE = 1.0/60;
	
	private static Calendar sixMorning;
	private static Calendar sixEvening;
	private Date day;

	private Date startTime;
	private Date endTime;
	private int differentStarts = 0;
	private int fourHourDifferenceIsMet = 0;
	
	public WorkedDay(final Date s, final Date e){
		startTime = s;
		endTime = e;
		
		sixMorning = Calendar.getInstance();
		sixMorning.set(Calendar.HOUR_OF_DAY,6);
		sixMorning.set(Calendar.MINUTE,0);
		sixMorning.set(Calendar.SECOND,0);
		sixMorning.set(Calendar.MILLISECOND,0);
		
		sixEvening = Calendar.getInstance();
		sixEvening.set(Calendar.HOUR_OF_DAY,18);
		sixEvening.set(Calendar.MINUTE,0);
		sixEvening.set(Calendar.SECOND,0);
		sixEvening.set(Calendar.MILLISECOND,0);
	}
	
	protected void truncCalendar(Calendar c){
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		c.set(Calendar.MILLISECOND,0);
	}
	
	public long getBonusMinutes() throws Exception{
		Calendar stime = Calendar.getInstance();
		stime.setTime(startTime);
		Calendar etime = Calendar.getInstance();
		etime.setTime(endTime);
		
		long diff = etime.getTimeInMillis() - stime.getTimeInMillis();
		if (diff < 0 )
			throw new Exception("End time is before start time");

		final double morningHour = sixMorning.get(Calendar.HOUR_OF_DAY);
		final double eveningHour = sixEvening.get(Calendar.HOUR_OF_DAY);
		
//		double currentHour = stime.get(Calendar.HOUR_OF_DAY);
//		int currentMinutes = stime.get(Calendar.MINUTE);
////		currentHour += 1.0*stime.get(Calendar.MINUTE)/60;
//		
//		double endHour = etime.get(Calendar.HOUR_OF_DAY);
//		int endMinutes = etime.get(Calendar.MINUTE);
//		endHour += 1.0*etime.get(Calendar.MINUTE)/60;
		
		Calendar normalHourCount = Calendar.getInstance();
		truncCalendar(normalHourCount);
		
		while(stime.before(etime)){
			if (hourIsNormal(stime.get(Calendar.HOUR_OF_DAY), morningHour,eveningHour)){
				normalHourCount.add(Calendar.MINUTE, MINUTE_RESOLUTION);
			}
			stime.add(Calendar.MINUTE, MINUTE_RESOLUTION);
		}
		
//		while (!closeEnough(currentHour,currentMinutes,endHour,endMinutes)){
//			if (hourIsNormal(currentHour, morningHour,eveningHour)){
//				normalHourCount+=RESOLUTION;
//			}
//			currentHour += RESOLUTION;
//			
//			if(closeEnough(currentHour , HOURS_IN_A_DAY)){
//				currentHour -= HOURS_IN_A_DAY;
//			}
//		}
//		normalHourCount.get(Calendar.HOUR_OF_DAY)*60 + normalHourCount.get(Calendar.MINUTE);
		
		long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
		minutes -= normalHourCount.get(Calendar.HOUR_OF_DAY)*60 + normalHourCount.get(Calendar.MINUTE);
//		System.out.println(1.0*minutes/60);
		return minutes;
	}
	
	protected boolean closeEnough(final double d1, final double d2){
		if ((Math.abs(d1-d2))<10e-6){
			return true;
		}
		
		return false;
	}
	
	protected boolean hourIsNormal(final double hour, final double morningHour, final double eveningHour){
		if ((hour >= morningHour && hour < eveningHour)){
			return true;
		}else {
			return false;
		}
	}

	public void isDifferentStart(WorkedDay other){
		long diff = other.getStartTime().getTime()-this.getStartTime().getTime();
		long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

		if (Math.abs(minutes)>0){
			this.incDifferentStarts();
			other.incDifferentStarts();
			if (Math.abs(minutes)>=MIN_DIFFERENCE_IN_MINUTES){
				this.incFourHourDifferenceIsMet();
				other.incFourHourDifferenceIsMet();
			}
		}
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getDifferentStarts() {
		return differentStarts;
	}
	public void incDifferentStarts() {
		this.differentStarts++;
	}

	public int getFourHourDifferenceIsMet() {
		return fourHourDifferenceIsMet;
	}

	public void incFourHourDifferenceIsMet() {
		this.fourHourDifferenceIsMet++;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}
}
