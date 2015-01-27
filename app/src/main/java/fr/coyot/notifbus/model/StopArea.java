package fr.coyot.notifbus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.location.Location;

public class StopArea implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The stop area Id 
	 */
	public String stopAreaId;
	
	/**
	 * The name of the stop area
	 */
	public String stopAreaName;
	
	/**
	 * Destinations of the stop
	 */
	public ArrayList<PhysicalStop> listPhysicalStops; 
		
	/**
	 * List of lines
	 */
	public HashMap<String, Line> listOfLines;
	
	/**
	 * The distance with the location of the user (only used for geotag)
	 */
	public Integer distance;
	
	/**
	 * The GPS position of the stop
	 */
	public Location gpsLocation;
		
	/**
	 * 
	 * @param stopAreaId
	 * @param stopAreaName
	 */
	public StopArea(String stopAreaId, String stopAreaName) {
		super();
		this.stopAreaId = stopAreaId;
		this.stopAreaName = stopAreaName;
		this.listPhysicalStops = new ArrayList<PhysicalStop>();
		this.listOfLines = new HashMap<String, Line>();
		this.distance = -1;
		this.gpsLocation = null;
	}
	
	@Override
	public String toString() {
		return stopAreaName;
	}
	
	
}
