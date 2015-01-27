package fr.coyot.notifbus.async;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import fr.coyot.notifbus.JourneyFragment;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.model.Schedule;
import fr.coyot.notifbus.receiver.AlarmReceiver;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;
import fr.coyot.notifbus.utils.GetHTTP;

public class RefreshAsync extends AsyncTask<String, Void, ArrayList<Journey>> {

	private JourneyFragment context;
	
	private FragmentActivity contextMain;
	
	private AlarmReceiver contextAlarm;
	
	private ProgressDialog dialog;
	
	private boolean isMorning;
	
	private boolean isManualRefresh = true;
	
	private ArrayList<Journey> listJourney;
	
	/**
	 * Constructor for refresh all journeys
	 * @param context
	 * @param listJourney
	 */
	public RefreshAsync(JourneyFragment context, FragmentActivity contextMain, 
			ArrayList<Journey> listJourney){
		this.context = context;
		this.contextMain = contextMain;
		this.listJourney = listJourney;
		dialog = new ProgressDialog(this.contextMain);
	}
	
	/**
	 * Constructor for refresh only one journey
	 * @param context
	 * @param journeyToRefresh
	 */
	public RefreshAsync(JourneyFragment context, FragmentActivity contextMain, 
			Journey journeyToRefresh){
		this.context = context;
		this.contextMain = contextMain;
		this.listJourney = new ArrayList<Journey>();
		this.listJourney.add(journeyToRefresh);
		dialog = new ProgressDialog(this.contextMain);
	}

	/**
	 * Constructor call by the alarm receiver when we have to display the notification
	 * @param contextAlarm
	 * @param listJourney
	 */
	public RefreshAsync(AlarmReceiver contextAlarm, ArrayList<Journey> listJourney){
		this.contextAlarm = contextAlarm;
		this.listJourney = listJourney;
		isManualRefresh = false;
	}
	
	/**
	 * Show the progress dialog while we retrieve information from the website
	 */
	@Override
	protected void onPreExecute() {
		if (this.isManualRefresh){
			this.dialog.setMessage("Mise à jour des horaires ....");
			this.dialog.show();
		}
	}
	
	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(ArrayList<Journey> listJourney) {
		if (isManualRefresh){
			if (dialog.isShowing()){
				dialog.dismiss();
			}
			if (this.isMorning){
				context.updateMorningJourneys(listJourney);
			} else{
				context.updateEveningJourneys(listJourney);
			}
		}else{
			contextAlarm.updateUI(listJourney);
		}
	}	
	
	@Override
	protected ArrayList<Journey> doInBackground(String... arg0) {
		String content = null;
		JSONObject rootObject = null;
		this.isMorning = Boolean.valueOf(arg0[0]);
		
		// For each given URL, we have to retrieve schedules 
		for(int i=0; i<listJourney.size(); i++){
			// Execute the HTTP GET to retrive data
			content = GetHTTP.getURL(Generic.buildURLSchedules(listJourney.get(i).stopId));
			Journey currentJourney = listJourney.get(i);
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
	                /*Log.d("SCHEDULE TIME", schedule.scheduleTime);
	                Log.d("SCHEDULE REAL TIME", String.valueOf(schedule.realTime));*/
	                
	                currentJourney.listSchedules.add(schedule);
	        	}
			} catch (JSONException ex){
				Log.d("REFRESH ASYNC", "Error while getting the schedule");
				Schedule scheduleError = new Schedule(null, true);
				currentJourney.listSchedules.add(scheduleError);
			}
		}
        
        
        return listJourney;
	}
}
