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
import fr.coyot.notifbus.ListLinesFragment;
import fr.coyot.notifbus.model.Destination;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.GetHTTP;

public class GetStopAsync extends AsyncTask<String, Void, ArrayList<PhysicalStop>> {

	private ProgressDialog dialog;
	private AddJourneyActivity context;
	private FragmentActivity contextMain;
	private ListLinesFragment contextFragment;
	
	public GetStopAsync(AddJourneyActivity context){
		this.context = context;
		this.contextMain = null;
		dialog = new ProgressDialog(this.context);
	}
	
	public GetStopAsync(FragmentActivity contextMain, ListLinesFragment contextFragment){
		this.contextMain = contextMain;
		this.contextFragment = contextFragment;
		this.context = null;
		dialog = new ProgressDialog(this.contextMain);
	}
	
	/**
	 * Show the progress dialog while we retrieve information from the website
	 */
	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Récupération des arrêts ....");
		this.dialog.show();
	}

	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(ArrayList<PhysicalStop> listStops) {
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		if (context != null){
			context.displayDestination(listStops);
		} else {
			contextFragment.displayDestinations(listStops);
		}
		
	}	
	
	@Override
	protected ArrayList<PhysicalStop> doInBackground(String... arg0) {
		String content = null;
		JSONObject rootObject = null;
		ArrayList<PhysicalStop> listStopsArea = new ArrayList<PhysicalStop>();
		
        // Retrieve the list of stops
		content = GetHTTP.getURL(arg0[0]);
		//Log.d("URL PHYSICAL STOPS", arg0[1]);
        try {
        	rootObject = new JSONObject(content);
        	// Get the JSON object which contains the array of bus lines
        	JSONObject object  = rootObject.getJSONObject(Constants.JSON_PHYSICAL_STOPS);
        	// Get the array of JSON Objects 
        	JSONArray array = object.getJSONArray(Constants.JSON_PHYSICAL_STOP);
        	for (int i = 0; i < array.length(); i++) {
        		JSONObject jsonObject = array.getJSONObject(i);
        		PhysicalStop current = new PhysicalStop(jsonObject.getString(Constants.JSON_ID), 
        				jsonObject.getString(Constants.JSON_NAME));
        		//Log.d("STOP", current.stopAreaName);
        		current.listDestinations = getDestination(jsonObject);
        		listStopsArea.add(current);
        	}
        	
		} catch (Exception ex){
			Log.d("PHYSICAL STOP", "Error");
		}
        
        return listStopsArea;
	}
	
	/**
	 * Return all destinations available for the given physical stop
	 * @param object
	 * @param stopAreaName
	 * @return
	 */
	private ArrayList<Destination> getDestination(JSONObject object){
		ArrayList<Destination> listOfDestinations = new ArrayList<Destination>();
		
		try {
			// Get the array of terminus in the object
			JSONArray arrayDestination = object.getJSONArray(Constants.JSON_DESTINATIONS);
			for (int i=0; i<arrayDestination.length(); i++){
				JSONObject jsonObject = arrayDestination.getJSONObject(i);
				Destination current = new Destination(jsonObject.getString(Constants.JSON_ID), 
						jsonObject.getString(Constants.JSON_NAME));
				//Log.d("DESTINATION", current.destinationName);
				listOfDestinations.add(current);
			}
		} catch (JSONException ex) {
			Log.d("GET DEST", "Error");
		}
		
		return listOfDestinations;
	}
	
}
