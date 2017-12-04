package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NightPerson extends Person {
    public static final double DHL_RATE = 420;
    public static final double DHL_NIGHT_RATE = 210;

    protected ExtraContainer workBonuses;
    protected ExtraContainer nightlyBonuses;
    // private double bonus;
    // private double bonusHours = 0;
    // private long bonusMinutes = 0;
    //
    // private double dhlBonus;
    // private double dhlBonusHours = 0;
    // private long dhlBonusMinutes = 0;

    protected boolean eligibleForNightlyBonus;

    protected List<NightWorkedDay> nightWorkedDays;

    public NightPerson( final String name, final int col ) {
        super(name, col);
        nightWorkedDays = new ArrayList<NightWorkedDay>();
        workBonuses = new ExtraContainer();
        nightlyBonuses = new ExtraContainer();
        // nightlyBonuses = new ArrayList<Extra>();
        // nightlyBonuses.add(new Extra(WorkType.RAKODAS));
        // nightlyBonuses.add(new Extra(WorkType.DHL));

        // workBonuses = new ArrayList<Extra>();
        // workBonuses.add(new Extra(WorkType.RAKODAS));
        // workBonuses.add(new Extra(WorkType.DHL));
    }

    @Override
    public void countBonusHours() throws Exception {
        if ( isEligible() ) {
            for ( NightWorkedDay nwd : getNightWorkedDays() ) {

                workBonuses.addExtraMinutes(nwd.getExtendedBonusMinutes(NightWorkedDay.sixMorning.get(Calendar.HOUR_OF_DAY),
                    NightWorkedDay.sixEvening.get(Calendar.HOUR_OF_DAY)));

                System.out.println(nwd.getDay() + " day has " + workBonuses.getWorkExtra().getBonusMinutes() + " STORAGE bonus minutes, that is "
                    + 1.0 * workBonuses.getWorkExtra().getBonusMinutes() / 60 + " bonus hours. " + nwd.getTimes());
                System.out.println(nwd.getDay() + " day has " + workBonuses.getDhlExtra().getBonusMinutes() + " DHL bonus	 minutes, that is "
                    + 1.0 * workBonuses.getDhlExtra().getBonusMinutes() / 60 + " bonus hours. " + nwd.getTimes());
            }
            workBonuses.getWorkExtra().calculateBonus(RATE);
            workBonuses.getDhlExtra().calculateBonus(DHL_RATE);

        } else {
            for ( NightWorkedDay nwd : getNightWorkedDays() ) {
                if ( isEligibleForNightlyBonus(nwd) ) {
                    nightlyBonuses.addExtraMinutes(nwd.getExtendedBonusMinutes(NightWorkedDay.sixMorning.get(Calendar.HOUR_OF_DAY),
                        NightWorkedDay.tenEvening.get(Calendar.HOUR_OF_DAY)));
                    System.out.println(nwd.getDay() + " day has " + workBonuses.getWorkExtra().getBonusMinutes() + " STORAGE bonus minutes, that is "
                        + 1.0 * workBonuses.getWorkExtra().getBonusMinutes() / 60 + " bonus hours. " + nwd.getTimes());
                    System.out.println(nwd.getDay() + " day has " + workBonuses.getDhlExtra().getBonusMinutes() + " DHL bonus	 minutes, that is "
                        + 1.0 * workBonuses.getDhlExtra().getBonusMinutes() / 60 + " bonus hours. " + nwd.getTimes());
                }
            }
            nightlyBonuses.getWorkExtra().calculateBonus(RATE);
            nightlyBonuses.getDhlExtra().calculateBonus(DHL_NIGHT_RATE);
        }
    }

    public boolean isEligibleForNightlyBonus( final NightWorkedDay nwd ) throws Exception {
        boolean eligible = false;

        WorkTime wt = nwd.getTotalWorkTime();
        // WorkTime wt = new WorkTime(wd.getStartTime(), wd.getEndTime(), null);
        long bonusMinutes =
            nwd.getBonusMinutes(wt, NightWorkedDay.sixMorning.get(Calendar.HOUR_OF_DAY), NightWorkedDay.tenEvening.get(Calendar.HOUR_OF_DAY));
        if ( bonusMinutes > 60 ) {
            eligible = true;
        }

        return eligible;
    }

    public List<NightWorkedDay> getNightWorkedDays() {
        return nightWorkedDays;
    }

    public void addNightWorkedDay( final NightWorkedDay nwd ) {
        this.nightWorkedDays.add(nwd);
    }

    @Override
    public List<WorkedDay> getDaysWorked() {
        if ( daysWorked == null || daysWorked.isEmpty() ) {
            for ( NightWorkedDay nwd : nightWorkedDays ) {
                WorkedDay wd = nwd.getWorkedDay();
                if ( wd != null ) {
                    daysWorked.add(wd);
                }
            }
        }
        return daysWorked;
    }

    @Override
    public double getBonus() {
        return bonus;
    }

    public void setBonus( final double bonus ) {
        this.bonus = bonus;
    }

    @Override
    public double getBonusHours() {
        return bonusHours;
    }

    @Override
    public void setBonusHours( final double bonusHours ) {
        this.bonusHours = bonusHours;
    }

    public ExtraContainer getWorkBonuses() {
        return workBonuses;
    }

    public void setWorkBonuses( final ExtraContainer workBonuses ) {
        this.workBonuses = workBonuses;
    }

    public ExtraContainer getNightlyBonuses() {
        return nightlyBonuses;
    }

    public void setNightlyBonuses( final ExtraContainer nightlyBonuses ) {
        this.nightlyBonuses = nightlyBonuses;
    }

    // public double getDhlBonus() {
    // return dhlBonus;
    // }
    //
    // public void setDhlBonus(double dhlBonus) {
    // this.dhlBonus = dhlBonus;
    // }
    //
    // public double getDhlBonusHours() {
    // return dhlBonusHours;
    // }
    //
    // public void setDhlBonusHours(double dhlBonusHours) {
    // this.dhlBonusHours = dhlBonusHours;
    // }

}
