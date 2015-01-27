package fr.coyot.notifbus.model;

import java.io.Serializable;
import java.util.ArrayList;


public class Journey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Journey Id
	 */
	public Integer journeyId;
	
	/**
	 * Line Id
	 */
	public String lineId;
	
	/**
	 * Line Name
	 */
	public String lineName;
	
	/**
	 * Line Color
	 */
	public String lineColor;
	
	/**
	 * Id of the stop selected
	 */
	public String stopId;
	
	/**
	 * Description of the journey : name of the stop and destination
	 */
	public String journeyDescription;
	
	/**
	 * Is the journey for the morning or not
	 */
	public boolean isMorning;

	/**
	 * Is the user clicked on the notification for stopping it
	 */
	public boolean jumpToNextNotif;
	
	/**
	 * The list of the next schedule for this journey
	 */
	public ArrayList<Schedule> listSchedules = new ArrayList<Schedule>();
	
	/**
	 * Default constructor
	 */
	public Journey() {
		
	}
	
	/**
	 * Constructor
	 * @param lineId
	 * @param lineName
	 * @param lineColor
	 * @param operatorCode
	 * @param journeyDescription
	 * @param isMorning
	 */
	public Journey(String lineId, String lineName, String lineColor, String operatorCode,
			String journeyDescription, boolean isMorning) {
		super();
		this.lineId = lineId;
		this.lineName = lineName;
		this.lineColor = lineColor;
		this.stopId = operatorCode;
		this.journeyDescription = journeyDescription;
		this.isMorning = isMorning;
		this.jumpToNextNotif = false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Ligne ");
		builder.append(this.lineName);
		builder.append("\r\n");
		builder.append(this.journeyDescription.substring(
				this.journeyDescription.indexOf("Vers :")));
		
		return builder.toString(); 
	}

	@Override
	public boolean equals(Object o) {
		Journey journeyToCompare = (Journey)o;
		return journeyToCompare.journeyId == this.journeyId;
	}
	
	
	
}

