package fr.coyot.notifbus.receiver;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import fr.coyot.notifbus.JourneyFragment;
import fr.coyot.notifbus.MainFragmentActivity;
import fr.coyot.notifbus.R;
import fr.coyot.notifbus.async.RefreshAsync;
import fr.coyot.notifbus.dao.UserPreferencesDAO;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class AlarmReceiver extends BroadcastReceiver {
	
	private UserPreferencesDAO dao;
	
	private ArrayList<Journey> listJourneys;
	
	private Context context;
	
	private Intent intent;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		this.intent = intent;
		
		// Retrieve list of journeys from the database
		// BDD initialization
		dao = new UserPreferencesDAO(context);
		dao.open();
	    listJourneys = dao.getAllJourneys(Generic.isMorning());
	    dao.close();

	    if (!listJourneys.isEmpty()){
	    	//GetScheduleAsync request = new GetScheduleAsync(this, listJourneys);
	    	RefreshAsync request = new RefreshAsync(this, listJourneys);
			// Call an asynchronous function for doing the HTTP request 
			request.execute("");
	    }
	    
	    //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	    Log.d("TOTO", "Affichage notification");
		
	}
	
	/**
	 * Display the notification when we have receive data from network
	 * @param listJourneys
	 */
	public void updateUI(ArrayList<Journey> listJourneys) {
		
		// If the first journey of the listJourneys has a schedule equal to null,
		// there was a problem during the retrieve of schedules (maybe we are in th subway)
        // Nothing is done on the notification and we scheduled the next refresh
        if (listJourneys != null && listJourneys.get(0).listSchedules.get(0).scheduleTime != null) {
            // Build the simple notification (only one line)
            // This notification will be display when we are not the first notification
            // in the notification center
            Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle("Prochains départs")
                    .setContentText(Generic.buildNotifContent(listJourneys.get(Generic.firstJourney(listJourneys)), false))
                    .setSmallIcon(R.drawable.icone);
            // Build the intent that will be launch when the user click on the notification
            Intent clickIntent = new Intent(context, JourneyFragment.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainFragmentActivity.class);
            stackBuilder.addNextIntent(clickIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            Intent stopNotifIntent = new Intent(context, StopNotifReceiver.class);
            PendingIntent stopNotifPIntent =
                    PendingIntent.getBroadcast(context, 192837, stopNotifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(R.drawable.validate, "J'y suis !", stopNotifPIntent);

            // Add the big notification that will be display if we are the first notification
            // in the notification center
            builder.setStyle(Generic.getInboxStyle(builder, listJourneys));
            // Set the auto cancel notification flag
            builder.setAutoCancel(true);

            NotificationManager notifManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notifManager.notify(Constants.NOTIFICATION_TAG_NAME, 0, builder.build());
        }
		
		// Check if the alarm must be stopped or not
		if (!Generic.isNotifHaveToBeDisplayed(context)){
			Generic.scheduleAlarm(context, intent, false);
		}
	}

}
