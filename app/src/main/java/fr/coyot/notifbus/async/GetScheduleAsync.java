package fr.coyot.notifbus.async;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import fr.coyot.notifbus.AddJourneyActivity;
import fr.coyot.notifbus.JourneyFragment;
import fr.coyot.notifbus.ListLinesFragment;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.model.Schedule;
import fr.coyot.notifbus.receiver.AlarmReceiver;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;
import fr.coyot.notifbus.utils.GetHTTP;

public class GetScheduleAsync extends AsyncTask<String, Void, ArrayList<Schedule>> {

	private ProgressDialog dialog;
	
	private FragmentActivity contextMain;
	
	private ListLinesFragment context;
	
	private PhysicalStop stopSelected;
	
	/**
	 * Constructor 
	 * @param context
	 * @param listJourney
	 */
	public GetScheduleAsync(ListLinesFragment context, FragmentActivity contextMain, 
			PhysicalStop stopSelected){
		this.context = context;
		this.contextMain = contextMain;
		this.stopSelected = stopSelected;
		dialog = new ProgressDialog(this.contextMain);
	}
	
	/**
	 * Show the progress dialog while we retrieve information from the website
	 */
	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Récupération des horaires ....");
		this.dialog.show();
	}
	
	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(ArrayList<Schedule> listSchedule) {
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		context.displaySchedules(listSchedule);
	}	
	
	@Override
	protected ArrayList<Schedule> doInBackground(String... arg0) {
		String content = null;
		JSONObject rootObject = null;
		
		ArrayList<Schedule> listSchedules = new ArrayList<Schedule>();
		
		// Execute the HTTP GET to retrive data
		content = GetHTTP.getURL(Generic.buildURLSchedules(stopSelected.physicalStopId));
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
                Log.d("SCHEDULE TIME", schedule.scheduleTime);
                Log.d("SCHEDULE REAL TIME", String.valueOf(schedule.realTime));
                
                listSchedules.add(schedule);
        	}
		} catch (JSONException ex){
			Log.d("REFRESH ASYNC", "Error while getting the schedule");
			Schedule scheduleError = new Schedule(null, true);
			listSchedules.add(scheduleError);
		}
        
        
        return listSchedules;
	}
}
