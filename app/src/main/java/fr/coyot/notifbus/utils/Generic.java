package fr.coyot.notifbus.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.util.Log;
import android.widget.Toast;
import fr.coyot.notifbus.R;
import fr.coyot.notifbus.dao.TisseoMessageDAO;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.model.Message;
import fr.coyot.notifbus.model.NextNotif;
import fr.coyot.notifbus.model.Schedule;
import fr.coyot.notifbus.receiver.AlarmReceiver;
import fr.coyot.notifbus.receiver.TisseoMessagesReceiver;

public class Generic {
	
	/**
	 * Return the name of the given number of day
	 * For example, if we have 7 in parameter, "Samedi" will be return
	 * @param nbDay
	 * @return
	 */
	public static String getNameOfTheDay(int nbDay){
		String nameOfTheDay = null;
		
		switch(nbDay){
			case 1 : nameOfTheDay = "dimanche"; break;
			case 2 : nameOfTheDay = "lundi"; break;
			case 3 : nameOfTheDay = "mardi"; break;
			case 4 : nameOfTheDay = "mercredi"; break;
			case 5 : nameOfTheDay = "jeudi"; break;
			case 6 : nameOfTheDay = "vendredi"; break;
			case 7 : nameOfTheDay = "samedi"; break;
		}
		
		return nameOfTheDay;
	}
	
	/**
	 * Verify if we are after expiration or not
	 * @return
	 */
	public static boolean isOutOfDate(Date expirationDate){
		boolean isOutOfDate = true;
		if (expirationDate != null){
			Calendar cal = Calendar.getInstance();
			if (expirationDate.after(cal.getTime())){
				isOutOfDate = false;
			}
		}
		return isOutOfDate;
	}

	public static Date parseExpirationDate(String expirationDate){
		Date returnDate = null;
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			returnDate = formatDate.parse(expirationDate);
		} catch (ParseException ex){
			Log.d("ERROR", "Error durant le parsing de la date");
		}
		return returnDate;
	}
	
	/**
	 * Return true if the hour of the current day is before 13h
	 * @return
	 */
	public static boolean isMorning(){
		Calendar cal = Calendar.getInstance();
        Integer currentMin = cal.get(Calendar.HOUR_OF_DAY)*60 
        		+ cal.get(Calendar.MINUTE);
        return currentMin <= Constants.NB_MIN_13H;
	}
	
	/**
	 * Return true if the hour of the current day is after 21h30
	 * @return
	 */
	public static boolean isNight(){
		Calendar cal = Calendar.getInstance();
        Integer currentMin = cal.get(Calendar.HOUR_OF_DAY)*60 
        		+ cal.get(Calendar.MINUTE);
        return ( currentMin >= Constants.NB_MIN_22H || currentMin <= Constants.NB_MIN_4H30 );
	}
	
	/**
	 * Return true if the hour of the current day is after 21h30
	 * @return
	 */
	public static boolean isBeforeNight(){
		Calendar cal = Calendar.getInstance();
        Integer currentMin = cal.get(Calendar.HOUR_OF_DAY)*60 
        		+ cal.get(Calendar.MINUTE);
        return ( currentMin >= Constants.NB_MIN_21H15 && currentMin < Constants.NB_MIN_22H );
	}
	
	public static String buildAbout(){
		StringBuilder builder = new StringBuilder();
		builder.append("NotifBus\r\n");
		builder.append("Version : ");
		builder.append(Constants.VERSION);
		return builder.toString();
	}
	
	/**
	 * Return true if the current day must have to display notification
	 * @param daysSelected
	 * @return
	 */
	public static boolean isDayForNotif(HashSet<String> daysSelected){
		boolean isDayForNotif = false;
		// Get the current day
		Calendar cal = Calendar.getInstance();
		int currentDay = cal.get(Calendar.DAY_OF_WEEK);
		Iterator<String> it = daysSelected.iterator();
		while (it.hasNext() && !isDayForNotif){
			String current = (String)it.next();
			if (Integer.parseInt(current) == currentDay){
				isDayForNotif = true;
			}
		}
		return isDayForNotif;
	}
	
	/**
	 * Get days selected by the user, or the default ones if it is not initialized 
	 * @param sharedPref
	 * @return
	 */
	private static HashSet<String> getPrefDays(SharedPreferences sharedPref, String keyName){
		HashSet<String> prefDays = (HashSet<String>) sharedPref.getStringSet(keyName, null);
        if (prefDays == null){
        	// It means that preferences are not be initialize, so we use default values
        	prefDays = new HashSet<String>();
        	prefDays.add("2");
        	prefDays.add("3");
        	prefDays.add("4");
        	prefDays.add("5");
        	prefDays.add("6");
        }
        return prefDays;
	}
	
	/**
	 * Return the next period where notifications are activated
	 * @param context
	 * @return NextNotif period
	 */
	public static NextNotif nextNotifPeriod(Context context){
		NextNotif nextNotif = new NextNotif();
		
		// Retrieve all users data to determinate the next day and hour we have to display the notification
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		
        // We check if almost one of notification period (morning or evening) is activated
        if (		sharedPref.getBoolean(Constants.KEY_PREF_MORNING_ACTIVATE, true)
        		|| 	sharedPref.getBoolean(Constants.KEY_PREF_EVENING_ACTIVATE, true)){
        	Iterator<String> it;
    		// Build the boolean tab which represent morning days active in the week
    		Boolean[] activeMorningDays = {false, false, false, false, false, false, false, false};
    		if (sharedPref.getBoolean(Constants.KEY_PREF_MORNING_ACTIVATE, true)){
    			// Retrieve from the shared preferences, the list of days active (for the afternoon)
    	        HashSet<String> prefDays = getPrefDays(sharedPref, Constants.KEY_PREF_MORNING_DAYS); 
    	        it = prefDays.iterator();
    			while (it.hasNext()){
    				String day = (String) it.next();
    				activeMorningDays[Integer.parseInt(day)] = true;
    			}
    		}
    		
    		// Build the boolean tab which represent afternoon days active in the week
    		Boolean[] activeEveningDays = {false, false, false, false, false, false, false, false};
    		if (sharedPref.getBoolean(Constants.KEY_PREF_EVENING_ACTIVATE, true)){
    			// Retrieve from the shared preferences, the list of days active (for the afternoon)
    			HashSet<String> prefDays = getPrefDays(sharedPref, Constants.KEY_PREF_EVENING_DAYS); 
    			it = prefDays.iterator();
    			while (it.hasNext()){
    				String day = (String) it.next();
    				activeEveningDays[Integer.parseInt(day)] = true;
    			}
    		}
    		
    		// Get the next period of notification after the current hour
    		Calendar cal = Calendar.getInstance();
    		Integer currentDay = cal.get(Calendar.DAY_OF_WEEK);
    		Integer currentMin = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE);
    		Integer nextHour = 0;
    		
    		if (isMorning()){
    			nextHour = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_MORNING_HOUR, "480"));
    			if (currentMin > nextHour){
    				// We are in the morning and we are after the morning hour notification
    				// So we check if notification is activated for the evening
    				if (activeEveningDays[currentDay]){
    					nextHour = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_EVENING_HOUR, "1080"));
    					nextNotif = new NextNotif(currentDay, nextHour, false);
    				} else {
    					// The notification is not activated of the current afternoon, 
    					// the nextNotif object is initialized with the next checking period that will be
    					// tomorrow morning and set the searchNextNotif to true
    					nextNotif = new NextNotif((currentDay+1)%7, Constants.MORNING, true);
    				}
    			} else {
    				// We are in the morning and we are before the morning hour notification
    				// So we check if notification is activated for the morning
    				if (activeMorningDays[currentDay]){
    					nextNotif = new NextNotif(currentDay, nextHour, false);
    				} else {
    					// The notification is not activated of the current morning, 
    					// the nextNotif object is initialized with the next checking period that will be
    					// this evening and set the searchNextNotif to true
    					nextNotif = new NextNotif(currentDay, Constants.EVENING, true);
    				}
    			}
    		} else {
    			nextHour = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_EVENING_HOUR, "1080"));
    			if (currentMin > nextHour){
    				// We are in the evening and we are after the evening hour notification
    				// So we check if notification is activated for tomorrow morning
    				if (activeMorningDays[(currentDay+1)%7]){
    					nextHour = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_MORNING_HOUR, "480"));
    					nextNotif = new NextNotif((currentDay+1)%7, nextHour, false);
    				} else {
    					// The notification is not activated for tomorrow morning
    					// the nextNotif object is initialized with the next checking period that will be
    					// tomorrow evening and set the searchNextNotif to true
    					nextNotif = new NextNotif((currentDay+1)%7, Constants.EVENING, true);
    				}
    			} else {
    				// We are in the evening and we are before the evening hour notification
    				// So we check if notification is activated for the evening
    				if (activeEveningDays[currentDay]){
    					nextNotif = new NextNotif(currentDay, nextHour, false);
    				} else {
    					// The notification is not activated of the current evening, 
    					// the nextNotif object is initialized with the next checking period that will be
    					// tomorrow moening and set the searchNextNotif to true
    					nextNotif = new NextNotif((currentDay+1)%7, Constants.MORNING, true);
    				}
    			}
    		}
    		
    		if (nextNotif.searchNextNotif){
    			// The next logical period (for example the evening if we are morning) hasn't notification activated
    			// We have to search the next period where notifications is active
    			while (nextNotif.searchNextNotif){
    				if (nextNotif.period == Constants.MORNING){
    					if (activeMorningDays[nextNotif.day]){
    						nextNotif.searchNextNotif = false;
    						nextNotif.period = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_MORNING_HOUR, "480"));
    					} else {
    						nextNotif.period = Constants.EVENING;
    					}
    				} else {
    					if (activeEveningDays[nextNotif.day]){
    						nextNotif.searchNextNotif = false;
    						nextNotif.period = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_EVENING_HOUR, "1080"));
    					} else {
    						nextNotif.period = Constants.MORNING;
    						nextNotif.day = (nextNotif.day+1)%7;
    					}
    				}
    			}
    		}
        }
		return nextNotif;
	}
	
	
	/**
	 * Function that will return if we have to display a notification or not
	 * @param Context
	 * @return boolean
	 */
	public static boolean isNotifHaveToBeDisplayed (Context context) {
		String keyHour, keyDuration, keyDaysPref, keyNotifActivated;
		Integer prefHour = -2;
		Integer prefDuration = -2;
		String defaultPrefHour;
		boolean haveToDisplay = false;
		
		// Get the current hour
		Calendar cal = Calendar.getInstance();
		int currentMin = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE);
		
		if (currentMin < Constants.NB_MIN_13H ){
			keyHour = Constants.KEY_PREF_MORNING_HOUR;
			keyDuration = Constants.KEY_PREF_MORNING_DURATION;
			keyDaysPref = Constants.KEY_PREF_MORNING_DAYS;
			keyNotifActivated = Constants.KEY_PREF_MORNING_ACTIVATE;
			defaultPrefHour = "480";
		} else {
			keyHour = Constants.KEY_PREF_EVENING_HOUR;
			keyDuration = Constants.KEY_PREF_EVENING_DURATION;
			keyDaysPref = Constants.KEY_PREF_EVENING_DAYS;
			keyNotifActivated = Constants.KEY_PREF_EVENING_ACTIVATE;
			defaultPrefHour = "1080";
		}
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPref.getBoolean(keyNotifActivated, true)) {
			// Retrieve days where notifications are active
			HashSet<String> prefDays = getPrefDays(sharedPref, keyDaysPref); 
			Log.d("ACTIVE_DAYS", prefDays.toString());
			if (isDayForNotif(prefDays)){
				// Retrieve hour and duration of active notification
		        prefHour = Integer.parseInt(sharedPref.getString(keyHour, defaultPrefHour));
		        prefDuration = Integer.parseInt(sharedPref.getString(keyDuration, "30"));
			}
	        
	        Log.d("isNotifHaveToBeDisplayed", "PrefHour = " + String.valueOf(prefHour));
	        Log.d("isNotifHaveToBeDisplayed", "PrefDuration = " + String.valueOf(prefDuration));
	        Log.d("isNotifHaveToBeDisplayed", "CurrentMin = " + String.valueOf(currentMin));
	        
	        haveToDisplay = currentMin>=prefHour && currentMin<=(prefHour+prefDuration);
		}
        
		return haveToDisplay;
	}
	
	/**
	 * Stop the alarm and rescheduled the alarm for the next hour defined
	 * in user preferences
	 * @param context
	 * @param intent
	 * @param isImmediate
	 */
	public static Calendar scheduleAlarm (Context context, Intent intent, boolean isImmediate) {
		PendingIntent sender;
		//String keyPrefDays, keyPrefMaxDay;
		//Integer nextHour, interval, period;
		Integer interval;
		
		// First thing, stop the alarm
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender = PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(sender);
        
        // Retrieve all users data to determinate the next day and hour we have to display the notification
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Retrieve the current hour (in minutes for example, 8h30 will be 510 (8*60+30))
        Calendar cal = Calendar.getInstance();
        Integer currentMin = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE);
        
        // Check if we are in the morning or not
        if (Generic.isMorning()){
        	//period = Constants.MORNING;
        	interval = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_MORNING_INTERVAL, "2"));
        } else {
        	//period = Constants.EVENING;
        	interval = Integer.parseInt(sharedPref.getString(Constants.KEY_PREF_EVENING_INTERVAL, "2"));
        }
        
        // If the isImmadiate boolean is set to true, we have to display the notification now 
        if (!isImmediate){
	        // Build the time for the next alarm trigger
	        // if nextHour is equal to -1, it will mean that we have to trigged the alarm immediatly 	
	        NextNotif nextNotif = nextNotifPeriod(context);
	        if (nextNotif.period != -1){
	        	Integer currentDay = cal.get(Calendar.DAY_OF_WEEK);
	        	Log.d("scheduleAlarm", "CurrentDay = " + String.valueOf(currentDay));
		        Log.d("scheduleAlarm", "NextDay = " + String.valueOf(nextNotif.day));
		        Log.d("scheduleAlarm", "NextHour = " + String.valueOf(nextNotif.period));
	        	if (nextNotif.day == currentDay){
	        		cal.add(Calendar.MINUTE, nextNotif.period-currentMin);
	        	} else {
	        		Integer nbDay = 0;
	        		cal.add(Calendar.MINUTE, (1440-currentMin)+nextNotif.period);
	        		if (nextNotif.day > currentDay){
		 	        	nbDay = (nextNotif.day - currentDay)-1;
		 	        } else if (nextNotif.day < currentDay){
		 	        	nbDay = ((7-currentDay) + nextNotif.day)-1;
		 	        }
	        		cal.add(Calendar.MINUTE, 1440*nbDay);
	        	}
	        }
        }
        
        // Create the new sender and intent for having an up to date context
        Intent newIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent newSender = PendingIntent.getBroadcast(context, 192837, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Schedule the alarm
        am.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis() ,interval*60000, newSender);
        
        String nextDate = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + " à " 
        		+ String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
        		+ "h" + String.valueOf(cal.get(Calendar.MINUTE));
        Log.d("Alarm", "Alarme programmée pour le "
        		+ nextDate + " !");
        
        return cal;
	}

	public static void displayNextHourNotif (Context context, Calendar calendarNextNotif) {
		// Get current calendar
		Calendar currentCalendar = Calendar.getInstance();
		
		StringBuilder hourAndMin = new StringBuilder();
		hourAndMin.append(" ");
		hourAndMin.append(calendarNextNotif.get(Calendar.HOUR_OF_DAY));
		hourAndMin.append("h");
		if (calendarNextNotif.get(Calendar.MINUTE) != 0){
			if (calendarNextNotif.get(Calendar.MINUTE) < 10){
				hourAndMin.append("0"+calendarNextNotif.get(Calendar.MINUTE));
			} else {
				hourAndMin.append(calendarNextNotif.get(Calendar.MINUTE));
			}
		}
		
		// Build the text that will be shown to the user
		StringBuilder textNotif = new StringBuilder("Prochaine notification ");
		if (currentCalendar.get(Calendar.DAY_OF_MONTH) == calendarNextNotif.get(Calendar.DAY_OF_MONTH)){
			textNotif.append("à partir de ");
			textNotif.append(hourAndMin.toString());
		} else {
			currentCalendar.add(Calendar.DAY_OF_YEAR, 1);
			if (currentCalendar.get(Calendar.DAY_OF_MONTH) == calendarNextNotif.get(Calendar.DAY_OF_MONTH)){
				textNotif.append("à partir de demain ");
				textNotif.append(hourAndMin.toString());
			} else {
				textNotif.append("à partir de ");
				textNotif.append(Generic.getNameOfTheDay(calendarNextNotif.get(Calendar.DAY_OF_WEEK)));
				textNotif.append(hourAndMin.toString());
			}
		}
		
		// Display when we are the next notification
		Toast nextNotif = Toast.makeText(context, textNotif.toString(), Toast.LENGTH_SHORT);
		nextNotif.show();
	}
	
	/**
	 * Schedule the check tisseo important message
	 * If a calendar if given, the check will be schedule at this date
	 * @param context
	 * @param givenCalendar
	 */
	public static void scheduleMessageNotif(Context context, Calendar givenCalendar, boolean checkNow){
		Intent messageIntent = new Intent(context, TisseoMessagesReceiver.class);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent newSender = PendingIntent.getBroadcast(context, 192837, messageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Retrieve a calendar instance
        Calendar cal = Calendar.getInstance();
        
        if (!checkNow){
        	if (givenCalendar == null){
            	Integer currentHour = cal.get(Calendar.HOUR_OF_DAY);
                if (((currentHour>= 17 && currentHour<= 19))
                		|| ((currentHour>= 6 && currentHour<= 9))){
                	// We are between 17h and 19h or between 6h and 9h, we check tisseo message every 15 minutes
                	cal.add(Calendar.MINUTE, 15);
                } else if (currentHour>= 10 && currentHour<= 16){
                	// We are between 10h and 16h, next check will be at 17h
                    cal.set(Calendar.HOUR_OF_DAY, 17);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                } else if (currentHour>= 20){
                	// We are between 20h and 0h, next check will be the next day at 6h
                	cal.add(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 6);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                } else if (currentHour<= 5){
                	// We are between 0h and 5h, next check will be at 6h
                    cal.set(Calendar.HOUR_OF_DAY, 6);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                }
            } else{
            	cal = givenCalendar;
            }
        }
        
        Log.d("Message Schedule", "Next check for " + cal.get(Calendar.HOUR_OF_DAY) + "h" + cal.get(Calendar.MINUTE) );
        // Schedule the alarm (which repeat every day or 1440 min)
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), newSender);
	}
	
	/**
	 * If the message is not null, it will be added to the database, otherwise we delete the message
	 * @param currentMessage
	 */
	public static void updateMessageInBD (TisseoMessageDAO dao, Message currentMessage){
		dao.open();
		if (currentMessage.title != null){
			dao.deleteImportantMessage();
			// Add the message in the database
			dao.addMessage(currentMessage.title, currentMessage.content, currentMessage.type, 
					currentMessage.importance, currentMessage.expirationDate);
			dao.getImportantMessages();
		} else {
			// Delete important message from the database
			dao.deleteImportantMessage();
		}
		dao.close();
	}
	
	/**
	 * Build the text of the big notification
	 * @param schedules
	 * @param builder
	 * @param listJourneys
	 * @return an object notification
	 */
	public static InboxStyle getInboxStyle(Builder builder, ArrayList<Journey> listJourneys){
		InboxStyle bigNotification = new InboxStyle();
		if (listJourneys.size() == 1){
			bigNotification
		    .addLine(Generic.buildNotifContent(listJourneys.get(0), false));
		} else if (listJourneys.size() == 2){
			bigNotification
		    .addLine(Generic.buildNotifContent(listJourneys.get(0), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(1), false));
		} else if (listJourneys.size() == 3){
			bigNotification
		    .addLine(Generic.buildNotifContent(listJourneys.get(0), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(1), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(2), false));
		} else if (listJourneys.size() == 4){
			bigNotification
		    .addLine(Generic.buildNotifContent(listJourneys.get(0), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(1), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(2), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(3), false));
		} else if (listJourneys.size() > 4){
			bigNotification
		    .addLine(Generic.buildNotifContent(listJourneys.get(0), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(1), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(2), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(3), false))
		    .addLine(Generic.buildNotifContent(listJourneys.get(4), false));
		}

		return bigNotification;
	}
	
	/**
	 * Build the content of a notification
	 * @param lineName
	 * @param schedules
	 * @return
	 */
	public static String buildNotifContent(Journey journey, boolean isMainActivity){
		StringBuilder notifContent = new StringBuilder();
		if (!isMainActivity){
			notifContent.append("Ligne ");
			notifContent.append(journey.lineName);
		}
		else {
			notifContent.append("Prochains départs ");
		}
		
		if (journey.listSchedules.get(0).scheduleTime != null){
			Schedule currentSchedule = journey.listSchedules.get(0);
			notifContent.append(" : ");
			notifContent.append(currentSchedule.scheduleTime);
			if (!currentSchedule.realTime){
				notifContent.append("*");
			}
			currentSchedule = journey.listSchedules.get(1);
			notifContent.append(" et ");
			notifContent.append(currentSchedule.scheduleTime);
			if (!currentSchedule.realTime){
				notifContent.append("*");
			}
		}else{
			notifContent.append(" : Données indisponibles ....");
		}
		return notifContent.toString();
	}
	
	/**
	 * Transform a string to a boolean
	 * If the string equals "yes" or "YES" a true boolean will be return
	 * A false boolean will be return in all others case
	 * @param String 
	 */
	public static boolean transformString(String toTransform){
		boolean result = false;
		if (toTransform.equalsIgnoreCase("yes")){
			result = true;
		}
		return result;
	}
	
	/**
	 * Return the id of the first journey which is valid 
	 * It means the first journey that don't have a scheduleTime equal to null
	 * @param listJourneys
	 * @return
	 */
	public static int firstJourney(ArrayList<Journey> listJourneys){
		int firstJourney = 0;
		Journey currentJourney = listJourneys.get(firstJourney);
		while (firstJourney < listJourneys.size() && 
				currentJourney.listSchedules.get(0).scheduleTime == null ){
			currentJourney = listJourneys.get(firstJourney);
			firstJourney++;
		}
		return firstJourney;
	}
	
	/**
	 * Build the URL that will retrieve the list of lines
	 * @return
	 */
	public static String buildURLLines(){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_LIST_LINES);
		url.append(Constants.POINT_INTERROGATION);
		url.append(Constants.URL_DISPLAY_TERMINUS);
		url.append(Constants.ET);
		url.append(Constants.URL_KEY_API);
		return url.toString();
	}
	
	/**
	 * Build the URL that will retrieve stops for the given lineId
	 * @return
	 */
	public static String buildURLStops(String lineId){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_STOP);
		url.append(Constants.POINT_INTERROGATION);
		url.append(Constants.URL_DISPLAY_DESTINATIONS);
		url.append(Constants.ET);
		url.append(Constants.URL_LINE);
		url.append(lineId);
		url.append(Constants.ET);
		url.append(Constants.URL_KEY_API);
		return url.toString();
	}
	
	/**
	 * Build the URL that will retrieve stops for the given lineId
	 * @return
	 */
	public static String buildURLStopsAreasGPS(String bbox){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_STOP_GPS);
		url.append(Constants.POINT_INTERROGATION);
		url.append(Constants.URL_DISPLAY_LINES);
		url.append(Constants.ET);
		url.append(Constants.URL_DISPLAY_COORDXY);
		url.append(Constants.ET);
		url.append(Constants.URL_BBOX);
		url.append(bbox);
		url.append(Constants.ET);
		url.append(Constants.URL_KEY_API);
		//Log.d("URL STOP GPS", url.toString());
		return url.toString();
	}
	
	/**
	 * Build the URL that will retrieve stops for the given lineId
	 * @return
	 */
	public static String buildURLStopArea(String lineId){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_STOP_AREA);
		url.append(Constants.POINT_INTERROGATION);
		url.append(Constants.URL_LINE);
		url.append(lineId);
		url.append(Constants.ET);
		url.append(Constants.URL_KEY_API);
		return url.toString();
	}
	
	/**
	 * Build the URL that will retrieve the schedule for the selected
	 * line, stop and destination
	 * @return
	 */
	public static String buildURLSchedules(String stopId){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_NEXT_DEPARTURE);
		url.append(Constants.POINT_INTERROGATION);
		url.append(Constants.URL_STOP);
		url.append(stopId);
		url.append(Constants.ET);
		url.append(Constants.URL_KEY_API);
		//Log.d("URL_SCHEDULE", url.toString());
		return url.toString();
	}
	
	/**
	 * Build the URL that will retrieve the schedule for the selected
	 * line, stop and destination
	 * @return
	 */
	public static String buildURLGeoTagSchedules(String stopId, String lineId){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_NEXT_DEPARTURE);
		url.append(Constants.POINT_INTERROGATION);
		url.append(Constants.URL_STOP);
		url.append(stopId);
		url.append(Constants.ET);
		url.append(Constants.URL_LINE);
		url.append(lineId);
		url.append(Constants.ET);
		url.append(Constants.URL_KEY_API);
		//Log.d("URL_SCHEDULE", url.toString());
		return url.toString();
	}
	
	/**
	 * Build the URL that will retrieve the message from Tisseo
	 * @param : if it true, will only retrieve important message 
	 * 			(message that will be disply in the home page of Tisseo website)
	 * @return : The URL for retrieve messages
	 */
	public static String buildURLMessages(boolean onlyImportantMessages){
		StringBuilder url = new StringBuilder(Constants.URL_ROOT);
		url.append(Constants.URL_SUFFIX_MESSAGES);
		url.append(Constants.POINT_INTERROGATION);
		if (onlyImportantMessages){
			url.append(Constants.URL_DISPLAY_IMPORTANT_MESSAGES);
			url.append(Constants.ET);
		}
		url.append(Constants.URL_KEY_API);
		//Log.d("URL_MESSAGE", url.toString());
		return url.toString();
	}
	
	/**
	 * Build the string of the desired color
	 * @param lineColor
	 * @return
	 */
	public static String parseColor (String lineColor) {
		String color = lineColor.substring(1);
		color = color.substring(0, color.length()-1);
		String[] rgb = color.split(",");
		StringBuilder builder = new StringBuilder();
		builder.append("#66");
		builder.append(String.format("%02X",Integer.parseInt(rgb[0])));
		builder.append(String.format("%02X",Integer.parseInt(rgb[1])));
		builder.append(String.format("%02X",Integer.parseInt(rgb[2])));
		//Log.d("COLOR", builder.toString());
		return builder.toString();
	}
	
	public static int getIdGeoTag(int id){
		int idGeoTag = 0;
		switch (id){
			case 0 : idGeoTag = R.id.SnippetLine0;
					 break;
			case 1 : idGeoTag = R.id.SnippetLine1;
			 		 break;
			case 2 : idGeoTag = R.id.SnippetLine2;
			 		 break;
			case 3 : idGeoTag = R.id.SnippetLine3;
					 break;
			case 4 : idGeoTag = R.id.SnippetLine4;
			 		 break;
			case 5 : idGeoTag = R.id.SnippetLine5;
			 		 break;
			case 6 : idGeoTag = R.id.SnippetLine6;
			 		 break;
			case 7 : idGeoTag = R.id.SnippetLine7;
			 		 break;
			case 8 : idGeoTag = R.id.SnippetLine8;
			 		 break;
			case 9 : idGeoTag = R.id.SnippetLine9;
					 break;
			case 10 : idGeoTag = R.id.SnippetLine10;
			 		 break;
			case 11 : idGeoTag = R.id.SnippetLine11;
			 		 break;
			case 12 : idGeoTag = R.id.SnippetLine12;
			 		 break;
			case 13 : idGeoTag = R.id.SnippetLine13;
				 	 break;
			case 14 : idGeoTag = R.id.SnippetLine14;
				 	 break;
			case 15 : idGeoTag = R.id.SnippetLine15;
				 	 break;
			case 16 : idGeoTag = R.id.SnippetLine16;
				 	 break;
			case 17 : idGeoTag = R.id.SnippetLine17;
				 	 break;
		}
		return idGeoTag;
	}
}
