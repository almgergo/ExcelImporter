package model;

import java.util.Date;

public class WorkTime {

	protected Date startDate;
	protected Date endDate;
	protected WorkType workType;

	public WorkTime(Date s, Date e, WorkType wt) {
		this.startDate = s;
		this.endDate = e;
		this.workType = wt;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public WorkType getWorkType() {
		return workType;
	}

	public void setWorkType(WorkType workType) {
		this.workType = workType;
	}
}
