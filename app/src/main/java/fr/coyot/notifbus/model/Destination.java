package fr.coyot.notifbus.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Destination implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Destination Id
	 */
	public String destinationId;
	
	/**
	 * Destination Name
	 */
	public String destinationName;
	
	/**
	 * The short name of the line 
	 */
	public ArrayList<Line> listOfLines;
	
	public Destination(String destinationId, String destinationName) {
		super();
		this.destinationId = destinationId;
		this.destinationName = destinationName;
		this.listOfLines = new ArrayList<Line>();
	}

}
