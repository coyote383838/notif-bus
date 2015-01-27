package fr.coyot.notifbus.async;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import fr.coyot.notifbus.MainFragmentActivity;
import fr.coyot.notifbus.model.Destination;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.GetHTTP;

public class GetLinesAsync extends AsyncTask<String, Void, ArrayList<Line>> {

	private ProgressDialog dialog;
	private MainFragmentActivity context;
	private String expirationDate;
	private Class<?> intentToLaunch;
	private boolean isOnCreate;
	
	public GetLinesAsync(MainFragmentActivity context, Class<?> intentToLaunch, boolean isOnCreate){
		this.context = context;
		this.intentToLaunch = intentToLaunch;
		this.isOnCreate = isOnCreate;
		dialog = new ProgressDialog(this.context);
	}
	
	/**
	 * Show the progress dialog while we retrieve information from the website
	 */
	@Override
	protected void onPreExecute() {
		Log.d("GetLinesAsync", "onPreExecute");
		this.dialog.setMessage("Récupération des lignes ....");
		this.dialog.show();
	}

	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(ArrayList<Line> listLines) {
		Log.d("GetLinesAsync", "onPostExecute");
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		if (isOnCreate){
			context.initViewPager(listLines, expirationDate);
		} else {
			context.updateListLines(listLines, expirationDate, true, intentToLaunch);
		}
	}	
	
	@Override
	protected ArrayList<Line> doInBackground(String... arg0) {
		Log.d("GetLinesAsync", "doInBackground");
		String content;
		JSONObject rootObject = null;
		ArrayList<Line> listLines = new ArrayList<Line>();
        content = GetHTTP.getURL(arg0[0]);
        try {
        	rootObject = new JSONObject(content);
        	// Get the expiration date
        	expirationDate = rootObject.getString(Constants.JSON_EXPIRATION_DATE);
        	// Get the JSON object which contains the array of bus lines
        	JSONObject object  = rootObject.getJSONObject(Constants.JSON_LINES);
        	// Get the array of JSON Objects 
        	JSONArray array = object.getJSONArray(Constants.JSON_LINE);
        	for (int i = 0; i < array.length()-1; i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                // For temporary lines, we do nothing
                if (jsonObject.has(Constants.JSON_TRASNPORT_MODE)){
                	String transportMode = jsonObject.
                			getJSONObject(Constants.JSON_TRASNPORT_MODE).
                			getString(Constants.JSON_TRASNPORT_MODE_NAME);
                	// TAD (Transport a la demande) lines are excluded
                	// Only subway, tramway and bus lines are added
                    if (transportMode.equals(Constants.JSON_BUS)  
                    		|| transportMode.equals(Constants.JSON_TRAM)){
                    	Line currentLine = new Line(jsonObject.getString(Constants.JSON_ID), 
                        		jsonObject.getString(Constants.JSON_SHORT_NAME), 
                        		jsonObject.getString(Constants.JSON_NAME), 
                        		jsonObject.getString(Constants.JSON_LINE_COLOR),
                        		getTerminus(jsonObject));
                    	// Add the line in the arrayList
                    	listLines.add(currentLine);
                    }
                }
        	}
        	
         } catch (Exception ex) {
         	//ex.printStackTrace();
        	 Log.d("GET LINES ERROR", "toto");
         }
        
        return listLines;
	}
	
	private Destination[] getTerminus (JSONObject object){
		Destination[] listTerminus = null;
		JSONArray arrayTerminus = null;
		try {
			// Get the array of terminus in the object
			arrayTerminus = object.getJSONArray(Constants.JSON_TERMINUS);
			listTerminus = new Destination[arrayTerminus.length()];
			if (arrayTerminus != null){
				// We have retrieve the list of terminus so we have to build
				// the array 
				for (int i = 0; i < arrayTerminus.length(); i++) {
					JSONObject jsonObject = arrayTerminus.getJSONObject(i);
					listTerminus[i] = new Destination(jsonObject.getString(Constants.JSON_ID),
							jsonObject.getString(Constants.JSON_NAME));
				}
			}
		} catch (JSONException ex){
			Log.d("GET LINES ERROR", "Error ");
		}
		
		return listTerminus;
	}
	
}
