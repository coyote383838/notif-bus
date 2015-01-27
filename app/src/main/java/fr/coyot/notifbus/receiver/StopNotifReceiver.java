package fr.coyot.notifbus.receiver;

import java.util.Calendar;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import fr.coyot.notifbus.dao.UserPreferencesDAO;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class StopNotifReceiver extends BroadcastReceiver{
	
	private Calendar calendarNextNotif;
	
	private UserPreferencesDAO dao;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// BDD initialization
		dao = new UserPreferencesDAO(context);
		dao.open();
		
		// Update status of journeys in database, set that the user has clicked to stop it
		dao.updateStopJourney(Generic.isMorning());
		dao.close();
		
		// The user has click on the notification for stop notification
		Log.d("ALARM_RECEIVER", "User click on the notification for stopping it !");
		
		// Remove the notification that will called us
		NotificationManager notifManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancel(Constants.NOTIFICATION_TAG_NAME, 0);
		
		// Reschedule the notification for the next morning or evening hour
		Intent newIntent = new Intent(context, AlarmReceiver.class);
		calendarNextNotif = Generic.scheduleAlarm(context, newIntent, false);
		
		// Display to the user the next hour of notification
		Generic.displayNextHourNotif(context, calendarNextNotif);
	}
	
}
