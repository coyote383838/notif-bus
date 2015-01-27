package fr.coyot.notifbus.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Line implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The line Id 
	 */
	public String lineId;
	
	/**
	 * The name of the line
	 */
	public String lineShortName;
	
	/**
	 * The description of the line
	 */
	public String lineDescription;
	
	/**
	 * The color of the line
	 */
	public String lineColor;
	
	/**
	 * Terminus of the line
	 */
	public Destination[] listOfTerminus;
	
	/**
	 * The id of the terminus selected
	 */
	public int terminusSelected;
	
	/**
	 * The list of stop for the current line
	 */
	public ArrayList<PhysicalStop> listOfStops;

	
	/**
	 * Default constructor
	 * @param lineId
	 * @param lineShortName
	 * @param lineDescription
	 * @param lineColor
	 * @param listOfTerminus
	 */
	public Line(String lineId, String lineShortName, String lineDescription, String lineColor, 
			Destination[] listOfTerminus) {
		super();
		this.lineId = lineId;
		this.lineShortName = lineShortName;
		this.lineDescription = lineDescription;
		parseColor(lineColor);
		this.listOfTerminus = listOfTerminus;
		this.listOfStops = new ArrayList<PhysicalStop>();
	}
	
	public Line(String lineId, String lineShortName, String lineDescription, String lineColor) {
		super();
		this.lineId = lineId;
		this.lineShortName = lineShortName;
		this.lineDescription = lineDescription;
		parseColor(lineColor);
		this.listOfTerminus = null;
		this.listOfStops = new ArrayList<PhysicalStop>();
	}
	
	/**
	 * Return an arrayList of stops which have the given destination
	 * @param destinationId
	 * @return
	 */
	public ArrayList<PhysicalStop> getStops (String destinationId){
		ArrayList<PhysicalStop> listStops = new ArrayList<PhysicalStop>();
		
		// Build the list of stops that have the given destinationId 
		for (int i=0; i<this.listOfStops.size(); i++){
			PhysicalStop current = this.listOfStops.get(i);
			for (int j=0; j<current.listDestinations.size(); j++){
				if (current.listDestinations.get(j).destinationId.equals(destinationId)){
					listStops.add(current);
				}
			}
		}
		
		return listStops;
	}
	
	@Override
	public String toString() {
		return lineShortName + "\r\n" + lineDescription;
	}
	
	/**
	 * Parse given color
	 * @param lineColor
	 */
	private void parseColor (String lineColor) {
		String color = lineColor.substring(1);
		color = color.substring(0, color.length()-1);
		String[] rgb = color.split(",");
		StringBuilder builder = new StringBuilder();
		builder.append("#66");
		builder.append(String.format("%02X",Integer.parseInt(rgb[0])));
		builder.append(String.format("%02X",Integer.parseInt(rgb[1])));
		builder.append(String.format("%02X",Integer.parseInt(rgb[2])));
		//Log.d("COLOR", builder.toString());
		this.lineColor = builder.toString();
	}
	
}
