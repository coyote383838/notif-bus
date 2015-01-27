package fr.coyot.notifbus.async;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import fr.coyot.notifbus.MainFragmentActivity;
import fr.coyot.notifbus.model.Message;
import fr.coyot.notifbus.receiver.TisseoMessagesReceiver;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;
import fr.coyot.notifbus.utils.GetHTTP;

public class GetTisseoMessagesAsync extends AsyncTask<String, Void, Message> {

	private TisseoMessagesReceiver context;
	
	private MainFragmentActivity contextMain;
	
	private boolean onlyImportantMessages;
	
	
	public GetTisseoMessagesAsync(TisseoMessagesReceiver context, boolean onlyImportantMessages){
		this.context = context;
		this.contextMain = null;
		this.onlyImportantMessages = onlyImportantMessages;
	}
	
	public GetTisseoMessagesAsync(MainFragmentActivity context){
		this.contextMain = context;
		this.context = null;
		this.onlyImportantMessages = true;
	}

	/**
	 * Data retrieved so the progress dialog is removed
	 */
	@Override
	protected void onPostExecute(Message currentMessage) {
		if (context != null){
			context.updateUI(currentMessage);
		} else if (contextMain != null){
			contextMain.updateTisseoMessages(currentMessage);
		}
		
	}	
	
	@Override
	protected Message doInBackground(String... arg0) {
		String content;
		JSONObject rootObject = null;
		Message tisseoMessage = new Message();
        content = GetHTTP.getURL(Generic.buildURLMessages(onlyImportantMessages));
        // First get messages from TISSEO API
        try {
        	rootObject = new JSONObject(content);
        	// Retrieve expiration date and store it in the message
        	tisseoMessage.expirationDate = rootObject.getString(Constants.JSON_EXPIRATION_DATE);
            Log.d("GetTisseoMessage", "Expiration Date => " + tisseoMessage.expirationDate);
        	// Get the JSON object which contains the array of bus lines
        	JSONArray array  = rootObject.getJSONArray(Constants.JSON_MESSAGES);
        	if (array.length() > 0){
        		JSONObject jsonObject = array.getJSONObject(0);
                jsonObject = jsonObject.getJSONObject(Constants.JSON_MESSAGE);
        		tisseoMessage.title = jsonObject.getString(Constants.JSON_MESSAGE_TITLE);
        		tisseoMessage.content = jsonObject.getString(Constants.JSON_MESSAGE_CONTENT);
        		tisseoMessage.type = jsonObject.getString(Constants.JSON_MESSAGE_TYPE);
        		tisseoMessage.importance = jsonObject.getString(Constants.JSON_MESSAGE_IMPORTANCE);
        	}
        	/*tisseoMessage.title = "TOTO";
    		tisseoMessage.content = "TOTO";
    		tisseoMessage.importance = "important";
    		
    		Calendar cal = new GregorianCalendar();
    		cal.add(Calendar.MINUTE, 2);
    		StringBuilder builder = new StringBuilder();
    		String month = String.valueOf(cal.get(Calendar.MONTH)+1);
    		if ((cal.get(Calendar.MONTH)+1) < 10){
    			month = "0" + month;
    		}
    		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    		if ((cal.get(Calendar.DAY_OF_MONTH)) < 10){
    			day = "0" + day;
    		}
    		String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
    		if ((cal.get(Calendar.HOUR_OF_DAY)) < 10){
    			hour = "0" + hour;
    		}
    		String minute = String.valueOf(cal.get(Calendar.MINUTE));
    		if ((cal.get(Calendar.MINUTE)) < 10){
    			minute = "0" + minute;
    		}
    		builder.append(cal.get(Calendar.YEAR));
    		builder.append("-" + month);
    		builder.append("-" + day);
    		builder.append(" " + hour);
    		builder.append(":" + minute);
    		
    		tisseoMessage.expirationDate = builder.toString();*/
    		//Log.d("GetTisseoMessage", "New expiration date =>" + tisseoMessage.expirationDate);
    		
         } catch (Exception ex) {
        	 Log.d("GET MESSAGE ERROR", "Error while retrieving json object");
        	 Log.d("GET MESSAGE ERROR", ex.getMessage());
         }        
        return tisseoMessage;
	}
	
}
