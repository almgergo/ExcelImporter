package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NightWorkedDay extends WorkedDay {

	protected List<WorkTime> workTimes;

	public NightWorkedDay() {
		this.workTimes = new ArrayList<WorkTime>();
	}

	public BonusMinutes getExtendedBonusMinutes() throws Exception {
		long storageBonusMinutes = 0;
		long dhlBonusMinutes = 0;

		for (WorkTime wt : workTimes) {
			switch (wt.workType) {
			case RAKODAS:
				storageBonusMinutes += getBonusMinutes(wt);
				break;
			case DHL:
				dhlBonusMinutes += getBonusMinutes(wt);
				break;
			default:
				break;
			}
		}
		return new BonusMinutes(storageBonusMinutes, dhlBonusMinutes);
	}

	public long getBonusMinutes(WorkTime wt) throws Exception {
		Calendar stime = Calendar.getInstance();
		stime.setTime(wt.getStartDate());
		Calendar etime = Calendar.getInstance();
		etime.setTime(wt.getEndDate());

		long diff = etime.getTimeInMillis() - stime.getTimeInMillis();
		if (diff < 0)
			throw new Exception("End time is before start time");

		final double morningHour = sixMorning.get(Calendar.HOUR_OF_DAY);
		final double eveningHour = sixEvening.get(Calendar.HOUR_OF_DAY);

		Calendar normalHourCount = Calendar.getInstance();
		truncCalendar(normalHourCount);

		while (stime.before(etime)) {
			if (hourIsNormal(stime.get(Calendar.HOUR_OF_DAY), morningHour, eveningHour)) {
				normalHourCount.add(Calendar.MINUTE, MINUTE_RESOLUTION);
			}
			stime.add(Calendar.MINUTE, MINUTE_RESOLUTION);
		}

		long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
		minutes -= normalHourCount.get(Calendar.HOUR_OF_DAY) * 60 + normalHourCount.get(Calendar.MINUTE);
		return minutes;
	}

	public void addWorkTime(Date s, Date e, WorkType wt) {
		workTimes.add(new WorkTime(s, e, wt));
	}

	public void addWorkTime(WorkTime wt) {
		this.workTimes.add(wt);
	}

	public String getTimes() {
		StringBuilder sb = new StringBuilder();
		for (WorkTime wt : workTimes) {
			sb.append("\nType: " + wt.getWorkType() + ", start: " + wt.getStartDate() + ", end: " + wt.getEndDate()
					+ ".");
		}
		return sb.toString();
	}

	public WorkedDay getWorkedDay() {
		if (workTimes.isEmpty()) {
			return null;
		}
		Date start = workTimes.get(0).getStartDate();
		Date end = workTimes.get(workTimes.size() - 1).getEndDate();

		// Calendar startTime = Calendar.getInstance();
		// startTime.setTime(start);
		// Calendar endTime = Calendar.getInstance();
		// endTime.setTime(end);
		//
		// if (startTime.get(Calendar.HOUR) >= endTime.get(Calendar.HOUR)) {
		// endTime.add(5, 1);
		// }
		// setCalendarTime(startTime, startParts, 18);
		// setCalendarTime(endTime, endParts, 6);

		return new WorkedDay(start, end);
	}

	public List<WorkTime> getWorkTimes() {
		return workTimes;
	}

}
