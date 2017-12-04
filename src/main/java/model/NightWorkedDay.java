package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NightWorkedDay extends WorkedDay {

    protected List<WorkTime> workTimes;

    protected static Calendar tenEvening;
    {
        tenEvening = Calendar.getInstance();
        tenEvening.set(Calendar.HOUR_OF_DAY, 22);
        tenEvening.set(Calendar.MINUTE, 0);
        tenEvening.set(Calendar.SECOND, 0);
        tenEvening.set(Calendar.MILLISECOND, 0);
    }

    public NightWorkedDay() {
        this.workTimes = new ArrayList<WorkTime>();
    }

    public BonusMinutes getExtendedBonusMinutes( final double morningHour, final double eveningHour ) throws Exception {
        long storageBonusMinutes = 0;
        long dhlBonusMinutes = 0;

        for ( WorkTime wt : workTimes ) {
            switch ( wt.workType ) {
                case RAKODAS:
                    storageBonusMinutes += getBonusMinutes(wt, morningHour, eveningHour);
                    break;
                case DHL:
                    dhlBonusMinutes += getBonusMinutes(wt, morningHour, eveningHour);
                    break;
                default:
                    break;
            }
        }
        return new BonusMinutes(storageBonusMinutes, dhlBonusMinutes);
    }

    public long getBonusMinutes( final WorkTime wt, final double morningHour, final double eveningHour ) throws Exception {
        Calendar stime = Calendar.getInstance();
        stime.setTime(wt.getStartDate());
        Calendar etime = Calendar.getInstance();
        etime.setTime(wt.getEndDate());

        long diff = etime.getTimeInMillis() - stime.getTimeInMillis();
        if ( diff < 0 ) {
            throw new Exception("End time is before start time");
        }

        // final double morningHour = sixMorning.get(Calendar.HOUR_OF_DAY);
        // final double eveningHour = sixEvening.get(Calendar.HOUR_OF_DAY);

        Calendar normalHourCount = Calendar.getInstance();
        truncCalendar(normalHourCount);

        while ( stime.before(etime) ) {
            if ( hourIsNormal(stime.get(Calendar.HOUR_OF_DAY), morningHour, eveningHour) ) {
                normalHourCount.add(Calendar.MINUTE, MINUTE_RESOLUTION);
            }
            stime.add(Calendar.MINUTE, MINUTE_RESOLUTION);
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        minutes -= normalHourCount.get(Calendar.HOUR_OF_DAY) * 60 + normalHourCount.get(Calendar.MINUTE);
        return minutes;
    }

    public void addWorkTime( final Date s, final Date e, final WorkType wt ) {
        workTimes.add(new WorkTime(s, e, wt));
    }

    public void addWorkTime( final WorkTime wt ) {
        this.workTimes.add(wt);
    }

    public String getTimes() {
        StringBuilder sb = new StringBuilder();
        for ( WorkTime wt : workTimes ) {
            sb.append("\nType: " + wt.getWorkType() + ", start: " + wt.getStartDate() + ", end: " + wt.getEndDate() + ".");
        }
        return sb.toString();
    }

    public WorkedDay getWorkedDay() {
        if ( workTimes.isEmpty() ) {
            return null;
        }
        Date start = workTimes.get(0).getStartDate();
        Date end = workTimes.get(workTimes.size() - 1).getEndDate();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);

        if ( startCal.get(Calendar.HOUR_OF_DAY) >= endCal.get(Calendar.HOUR_OF_DAY) ) {
            endCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return new WorkedDay(startCal.getTime(), endCal.getTime());
    }

    public WorkTime getTotalWorkTime() {
        if ( workTimes.isEmpty() ) {
            return null;
        }
        Date start = workTimes.get(0).getStartDate();
        Date end = workTimes.get(workTimes.size() - 1).getEndDate();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);

        if ( startCal.get(Calendar.HOUR_OF_DAY) >= endCal.get(Calendar.HOUR_OF_DAY) ) {
            endCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return new WorkTime(startCal.getTime(), endCal.getTime(), null);
    }

    public List<WorkTime> getWorkTimes() {
        return workTimes;
    }

}
