package fr.coyot.notifbus.model;

public class NextNotif {
	
	/**
	 * The day of the next notif
	 */
	public int day;
	
	/**
	 * The period of the next notif (morning or afternoon)
	 */
	public int period;

	/**
	 * Will be true if we have to search the next notif period 
	 * after the current day and the current period 
	 */
	public boolean searchNextNotif;
	
	/**
	 * Empty constructor
	 */
	public NextNotif(){
		day = 0;
		period = -1;
		searchNextNotif = false;
	}

	public NextNotif(int day, int period, boolean searchNextNotif) {
		super();
		this.day = day;
		this.period = period;
		this.searchNextNotif = searchNextNotif;
	}
	

	
	

}
