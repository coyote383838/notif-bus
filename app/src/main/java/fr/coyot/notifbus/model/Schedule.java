package fr.coyot.notifbus.model;

public class Schedule {
	
	/**
	 * The time of the schedule
	 */
	public String scheduleTime;
	
	/**
	 * Is this schedule is in real time or not
	 */
	public boolean realTime;
	

	public Schedule(String scheduleTime, boolean realTime) {
		super();
		this.scheduleTime = scheduleTime;
		this.realTime = realTime;
	}
	
	
	

}
