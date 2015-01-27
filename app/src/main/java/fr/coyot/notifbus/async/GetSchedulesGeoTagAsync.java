package fr.coyot.notifbus.async;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import fr.coyot.notifbus.GeoTagActivity;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.model.Schedule;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;
import fr.coyot.notifbus.utils.GetHTTP;

public class GetSchedulesGeoTagAsync extends AsyncTask<String, Void, ArrayList<PhysicalStop>> {
	
	private GeoTagActivity context;
	
	private ProgressDialog dialog;
	
	private ArrayList<PhysicalStop> listPhysicalStop;
	
	/**
	 * Constructor for refresh all journeys
	 * @param context
	 * @param listJourney
	 */
	public GetSchedulesGeoTagAsync(GeoTagActivity context, ArrayList<PhysicalStop> listPhysicalStops){
		this.context = context;
		this.listPhysicalStop = listPhysicalStops;
		dialog = new ProgressDialog(this.context);
	}
	
	/**
	 * Show the progress dialog while we retrieve information from the website
	 */
	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Mise à jour des horaires ....");
		this.dialog.show();
	}
	
	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(ArrayList<PhysicalStop> listPhysicalStop) {
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		context.updateSchedulesUI(listPhysicalStop);
	}	
	
	@Override
	protected ArrayList<PhysicalStop> doInBackground(String... arg0) {
		String content = null;
		JSONObject rootObject = null;
		
		for (int i=0; i<this.listPhysicalStop.size(); i++){
			// Execute the HTTP GET to retrive data
			content = GetHTTP.getURL(Generic.buildURLGeoTagSchedules(listPhysicalStop.get(i).physicalStopId, 
					listPhysicalStop.get(i).line.lineId));
	        try {
	        	rootObject = new JSONObject(content);
	        	// Get the JSON object which contains the array of bus lines
	        	JSONObject object  = rootObject.getJSONObject(Constants.JSON_DEPARTURES);
	        	// Get the array of JSON Objects 
	        	JSONArray array = object.getJSONArray(Constants.JSON_DEPARTURE);
	        	// Get the line name
	        	for (int j = 0; j < 2; j++) {
	        		JSONObject jsonObject = array.getJSONObject(j);
	                String scheduleTime = jsonObject.getString(Constants.JSON_DATE_TIME);
	                boolean realTime = Generic.transformString(jsonObject.getString(Constants.JSON_REAL_TIME));
	                scheduleTime = scheduleTime.substring(scheduleTime.indexOf(" "));
	                scheduleTime = scheduleTime.substring(1, scheduleTime.length()-3);
	                
	                // Construct the schedule object
	                Schedule schedule = new Schedule(scheduleTime, realTime);
	               /* Log.d("SCHEDULE TIME", schedule.scheduleTime);
	                Log.d("SCHEDULE REAL TIME", String.valueOf(schedule.realTime));*/
	                
	                listPhysicalStop.get(i).listSchedules.add(schedule);
	        	}
			} catch (JSONException ex){
				Log.d("REFRESH ASYNC GEO TAG", "Error while getting the schedule");
				Schedule scheduleError = new Schedule(null, true);
				listPhysicalStop.get(i).listSchedules.add(scheduleError);
			}
		}
        
        return listPhysicalStop;
	}
}
