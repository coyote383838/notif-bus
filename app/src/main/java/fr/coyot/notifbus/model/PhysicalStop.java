package fr.coyot.notifbus.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PhysicalStop implements Serializable, Comparable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The stop area Id 
	 */
	public String physicalStopId;
	
	/**
	 * The name of the stop area
	 */
	public String physicalStopName;
	
	/**
	 * List of destinations
	 */
	public ArrayList<Destination> listDestinations;
	
	/**
	 * Destinations of the stop
	 */
	public String destinationName;
	
	/**
	 * Short name of the line
	 */
	public Line line;
	
	/**
	 * List of schedules
	 */
	public ArrayList<Schedule> listSchedules;
	
	/**
	 * 
	 * @param stopAreaId
	 * @param stopAreaName
	 * @param destinationName
	 * @param line
	 */
	public PhysicalStop(String stopAreaId, String stopAreaName, String destinationName, Line line) {
		super();
		this.physicalStopId = stopAreaId;
		this.physicalStopName = stopAreaName;
		this.destinationName = destinationName;
		this.line = line;
		this.listSchedules = new ArrayList<Schedule>();
	}
	
	public PhysicalStop(String stopAreaId, String stopAreaName) {
		super();
		this.physicalStopId = stopAreaId;
		this.physicalStopName = stopAreaName;
		this.destinationName = "";
		this.line = null;
		this.listSchedules = new ArrayList<Schedule>();
	}
	
	/**
	 * Add a destination to this stopArea
	 * @param stopToAdd
	 */
	public void addDestination(Destination destinationToAdd){
		listDestinations.add(destinationToAdd);
	}
	
	
	@Override
	public String toString() {
		return physicalStopName;
	}

	@Override
	public int compareTo(Object another) {
		String otherName = ((PhysicalStop)another).physicalStopName;
		return physicalStopName.compareTo(otherName);
	}
	
	
}
