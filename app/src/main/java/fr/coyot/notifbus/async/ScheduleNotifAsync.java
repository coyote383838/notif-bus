package fr.coyot.notifbus.async;

import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.AsyncTask;
import fr.coyot.notifbus.MainFragmentActivity;
import fr.coyot.notifbus.receiver.AlarmReceiver;
import fr.coyot.notifbus.utils.Generic;

public class ScheduleNotifAsync extends AsyncTask<String, Void, Calendar> {

	private MainFragmentActivity context;
	
	private boolean onCreate;
	
	private boolean jumpToNextNotif;
	
	private String expirationDate;
	
	/**
	 * Constructor used when we have to schedule the next notification 
	 * @param context
	 * @param jumpToNextNotif
	 */
	public ScheduleNotifAsync(MainFragmentActivity context, boolean jumpToNextNotif){
		this.context = context;
		this.onCreate = false;
		this.expirationDate = null;
		this.jumpToNextNotif = jumpToNextNotif;
	}
	
	/**
	 * Constructor used only when we have to schedule the next check of TISSEO message
	 * @param context
	 * @param expirationDate
	 */
	public ScheduleNotifAsync(MainFragmentActivity context,String expirationDate){
		this.context = context;
		this.onCreate = true;
		this.expirationDate = expirationDate;
		this.jumpToNextNotif = false;
	}
	
	@Override
	protected void onPostExecute(Calendar result) {
		if (result != null){
			context.displayNextNotif(result);
		}
	}

	@Override
	protected Calendar doInBackground(String... arg0) {
		Calendar nextCalendar = null;
		if (onCreate){
			if (expirationDate == null){
				// Schedule next check of tisseo important messages
				Generic.scheduleMessageNotif(context, null,false);
			}else {
				Date expirDate = Generic.parseExpirationDate(expirationDate);
				Calendar cal = Calendar.getInstance();
				cal.setTime(expirDate);
				Generic.scheduleMessageNotif(context, cal,false);
			}
		}else{
			// Schedule next schedules notification
			Intent intent = new Intent(context, AlarmReceiver.class);
	        if (Generic.isNotifHaveToBeDisplayed(context) && !jumpToNextNotif){
	        	nextCalendar = Generic.scheduleAlarm(context, intent, true);
	        } else {
	        	nextCalendar = Generic.scheduleAlarm(context, intent, false);
	        }
		}
		
		return nextCalendar;
	}

}
