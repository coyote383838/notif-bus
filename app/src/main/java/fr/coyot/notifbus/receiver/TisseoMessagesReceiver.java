package fr.coyot.notifbus.receiver;

import java.util.Calendar;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import fr.coyot.notifbus.MainFragmentActivity;
import fr.coyot.notifbus.R;
import fr.coyot.notifbus.async.GetTisseoMessagesAsync;
import fr.coyot.notifbus.dao.TisseoMessageDAO;
import fr.coyot.notifbus.model.Message;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class TisseoMessagesReceiver extends BroadcastReceiver {
	
	private TisseoMessageDAO dao;
	
	private Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		
		Log.d("MessageReceiver", "Check TISSEO important message");
		
		// BDD initialisation
		dao = new TisseoMessageDAO(context);
		
		// Verify if there is a TISSEO message
		GetTisseoMessagesAsync request = new GetTisseoMessagesAsync(this, true); 
		request.execute("");
		
	}
	
	/**
	 * Function that will be called when we received messages from tisseo API
	 * and disply a notification if a message is returned
	 * @param currentMessage
	 */
	public void updateUI (Message currentMessage){
		// Manage message in the database
		Generic.updateMessageInBD(dao, currentMessage);
		
		if (currentMessage.title != null){
			// Build the simple notification (only one line) 
			// This notification will be display when we are not the first notification 
			// in the notification center
			Builder builder = new NotificationCompat.Builder(context)
			    	.setContentTitle(currentMessage.title)
			    	.setContentText(currentMessage.content)
			    	.setSmallIcon(R.drawable.icone_alert);
			// Build the intent that will be launch when the user click on the notification
			Intent clickIntent = new Intent(context, MainFragmentActivity.class);
			clickIntent.putExtra("displayMessage", true);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addParentStack(MainFragmentActivity.class);
			stackBuilder.addNextIntent(clickIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(resultPendingIntent);
			
			// Add the big notification that will be display if we are the first notification
			// in the notification center
			InboxStyle bigNotification = new InboxStyle();
			bigNotification.addLine(currentMessage.content);
			builder.setStyle(bigNotification);
			// Set the auto cancel notification flag
			builder.setAutoCancel(true);
	
			NotificationManager notifManager = 
					(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			notifManager.notify(Constants.NOTIFICATION_TAG_NAME, 0, builder.build());
			
			// Schedule next check at the date of expiration of this message
			Date expirationDate = Generic.parseExpirationDate(currentMessage.expirationDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(expirationDate);
			Generic.scheduleMessageNotif(context, cal,false);
			
		}else{
			Generic.scheduleMessageNotif(context, null,false);
		}

	}
	
}
