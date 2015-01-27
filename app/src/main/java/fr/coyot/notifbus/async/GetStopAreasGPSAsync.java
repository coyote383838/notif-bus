package fr.coyot.notifbus.async;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import fr.coyot.notifbus.GeoTagActivity;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.model.StopArea;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;
import fr.coyot.notifbus.utils.GetHTTP;

public class GetStopAreasGPSAsync extends AsyncTask<String, Void, HashMap<String, StopArea>>{

	private ProgressDialog dialog;
	
	private GeoTagActivity context;
	
	private Location myLocation;
	
	private HashMap<String, StopArea> listStopsArea = new HashMap<String, StopArea>();
	
	public GetStopAreasGPSAsync(GeoTagActivity context, Location location){
		this.context = context;
		this.myLocation = location;
		this.dialog = new ProgressDialog(context);
	}

	/**
	 * Show the progress dialog while we retrieve information from the website
	 */
	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Récupération des arrêts a proximité ....");
		this.dialog.show();
	}
	
	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(HashMap<String, StopArea> listStops) {
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		context.displayGeoTagStopArea(listStops);
	}	
	
	@Override
	protected HashMap<String, StopArea> doInBackground(String... arg0) {
		String content = null;
		JSONObject rootObject = null;
		String bbox;
		
		// Calculation of the area (radius of 500m) around the user for searching stops
		bbox = calculateBbox();
		
		// Retrieve the list of the stop area (a stop area is a stop for all destinations)
		content = GetHTTP.getURL(Generic.buildURLStopsAreasGPS(bbox));
        try {
        	rootObject = new JSONObject(content);
        	// Get the JSON object which contains the array of bus lines
        	JSONObject object  = rootObject.getJSONObject(Constants.JSON_PHYSICAL_STOPS);
        	// Get the array of JSON Objects 
        	JSONArray array = object.getJSONArray(Constants.JSON_PHYSICAL_STOP);
        	for (int i = 0; i < array.length(); i++) {
        		JSONObject jsonObject = array.getJSONObject(i);
        		
        		// First thing we retrieve the stopArea
        		JSONObject jsonStopArea = jsonObject.getJSONObject(Constants.JSON_STOP_AREA);
        		String currentStopAreaId = jsonStopArea.getString(Constants.JSON_ID);
        		if (!listStopsArea.containsKey(currentStopAreaId)){
        			// This stopArea is not in the hashmap, so we add it
        			StopArea currentStopArea = new StopArea(currentStopAreaId, 
        					jsonStopArea.getString(Constants.JSON_NAME));
					// Calculate the distance between the current location and the current StopArea
        			// Update gpsLocation and distance 
        			currentStopArea.gpsLocation = new Location(myLocation);
        			currentStopArea.gpsLocation.reset();
        			currentStopArea.gpsLocation.setLongitude(jsonStopArea.getDouble("x"));
        			currentStopArea.gpsLocation.setLatitude(jsonStopArea.getDouble("y"));
        			currentStopArea.distance = (int)myLocation.distanceTo(currentStopArea.gpsLocation);
        			listStopsArea.put(currentStopAreaId, currentStopArea);
        			Log.d("STOP ARREA GPS", currentStopArea.stopAreaName);
        		}
        		
        		// Add all couple destinationName / line short name as a physical stop in the stop area
        		addPhysicalStops(jsonObject.getJSONArray(Constants.JSON_DESTINATIONS), currentStopAreaId, 
        				jsonObject.getString(Constants.JSON_NAME), jsonObject.getString(Constants.JSON_ID));
        	}
        	
		} catch (JSONException ex){
			Log.d("STOP AREA GPS", "Error");
			Log.d("STOP AREA GPS", ex.getMessage());
		}
        
        return listStopsArea;
	}
	
	/**
	 * Retrieve destinations and lines from the given physical stop
	 * @param jsonObject
	 */
	private void addPhysicalStops(JSONArray arrayDestinations, String stopAreaId, String physicalStopName, 
			String physicalStopId){
		// Update the boolean to know if we are to display evening lines or not
		boolean isNight = Generic.isNight();
		boolean isBeforeNight = Generic.isBeforeNight();

		try{
			// Retrieve all destination for the given physical stop
			for (int i=0; i < arrayDestinations.length(); i++){
				PhysicalStop currentPhysicalStop;
				JSONObject jsonObject = arrayDestinations.getJSONObject(i);
				String destinationName = jsonObject.getString(Constants.JSON_NAME);
				
				// Get the array which contains all lines for the current destination
				JSONArray arrayLines = jsonObject.getJSONArray(Constants.JSON_LINE);
				for (int j=0; j < arrayLines.length(); j++){
					boolean isAddLine = false;
					JSONObject jsonObjectLine = arrayLines.getJSONObject(j);
						Line currentLine = new Line(jsonObjectLine.getString(Constants.JSON_ID), 
								jsonObjectLine.getString(Constants.JSON_SHORT_NAME), jsonObjectLine.getString(Constants.JSON_NAME), 
								jsonObjectLine.getString(Constants.JSON_LINE_COLOR));
						currentPhysicalStop = new PhysicalStop(physicalStopId, physicalStopName, 
								destinationName, currentLine);
					// If it the subway (ie lineShortName equal A or B), we don't add it to the physical stops
					// because we can't have schedules for theses lines
					if (!(currentLine.lineShortName.equals("A") || currentLine.lineShortName.equals("B"))){
						// If we are before 21h30, we don't have to include the night lines 
						// Night lines are the shortName that finished with "s" for example 2s
						if (currentLine.lineShortName.matches(".*s$") || currentLine.lineShortName.equals("NOCT")){
							if (isBeforeNight || isNight){
								// Add this physical stop to the stop area
								this.listStopsArea.get(stopAreaId).listPhysicalStops.add(currentPhysicalStop);
								isAddLine = true;
							}
						}else if (currentLine.lineShortName.matches("^L.*") || currentLine.lineShortName.equals("AERO")){
							// Lineo lines, ie lineShortName that begins by L, for example L16 and airport shuttle (AERO)
							// are lines which are active day and night so we active them all the time
							this.listStopsArea.get(stopAreaId).listPhysicalStops.add(currentPhysicalStop);
							isAddLine = true;
						}else {
							if (!isNight){
								// Add this physical stop to the stop area
								this.listStopsArea.get(stopAreaId).listPhysicalStops.add(currentPhysicalStop);
								isAddLine = true;
							}
						}
						
					} else {
						isAddLine = true;
					}
					
					// Verify if this line exist in the given list of lines
					// But we add the subway line to the list of the lines for display it to the user in the marker info
					if (isAddLine){
						if (!this.listStopsArea.get(stopAreaId).listOfLines.containsKey(currentLine.lineShortName)){
							this.listStopsArea.get(stopAreaId).listOfLines.put(currentLine.lineShortName, currentLine);
						}
					}
				}
				
			}
		} catch (JSONException ex){
			Log.d("DESTINATION GPS", "Error");
			Log.d("DESTINATION GPS", ex.getMessage());
		}
		
	}
	
	/**
	 * Calculate the bbox with a 500 meters radius
	 * @return
	 */
	private String calculateBbox(){
		Double latA, longA, latB, longB, radius;
		// Retrieve radius from the preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		radius = Double.valueOf(sharedPref.getString(Constants.KEY_PREF_GEOTAG_DISTANCE, "500.0"));
		
		StringBuilder bbox = new StringBuilder();
		//Log.d("StopAreasGPS.calculateBbox", "myPosition = " + this.myLocation.getLongitude() + "," + this.myLocation.getLatitude());
		//Log.d("StopAreasGPS.calculateBbox", "diff = " + String.valueOf(radius/Constants.DEGREE_LATITUDE));
		latA = this.myLocation.getLatitude() - (radius/Constants.DEGREE_LATITUDE);
		latB = this.myLocation.getLatitude() + (radius/Constants.DEGREE_LATITUDE);
		longA = this.myLocation.getLongitude() - (radius/Constants.DEGREE_LONGITUDE);
		longB = this.myLocation.getLongitude() + (radius/Constants.DEGREE_LONGITUDE);
		
		bbox.append(Double.toString(longA) + ",");
		bbox.append(Double.toString(latA) + ",");
		bbox.append(Double.toString(longB) + ",");
		bbox.append(Double.toString(latB));
		Log.d("StopAreasGPS.calculateBbox", "bbox = " + bbox.toString());
		
		return bbox.toString();
	}

}
