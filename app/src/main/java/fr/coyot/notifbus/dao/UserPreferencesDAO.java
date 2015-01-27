package fr.coyot.notifbus.dao;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.utils.Constants;

public class UserPreferencesDAO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BddOpenHelper lineBDD;
	
	private SQLiteDatabase sqlLite;

	/**
	 * Default constructor
	 * @param context
	 */
	public UserPreferencesDAO(Context context) {
		lineBDD = new BddOpenHelper(context);
	}
	
	public void open() {
		sqlLite = lineBDD.getWritableDatabase();
	}
	
	public void close() {
		lineBDD.close();
	}
	
	/**
	 * Retrieve from the database all journeys for a period
	 * @param isMorning : boolean that indicate if we want morning or evening journeys
	 * @return the list of journeys 
	 */
	public ArrayList<Journey> getAllJourneys(boolean isMorning) {
		ArrayList<Journey> listJourney = new ArrayList<Journey>();
		String whereClause = Constants.IS_MORNING + " = ";
		if (isMorning){
			whereClause = whereClause + "1";
		} else {
			whereClause = whereClause + "0";
		}
		
		Cursor c = sqlLite.query(Constants.TABLE_NAME_JOURNEYS, null, whereClause, null, null, null, null);
		
		if (c.getCount() != 0){
			c.moveToFirst();
			do {
				Journey current = new Journey();
				current.journeyId = c.getInt(0);
				current.lineId = c.getString(1);
				current.lineName = c.getString(2);
				current.lineColor = c.getString(3);
				current.stopId = c.getString(4);
				current.journeyDescription = c.getString(5);
				current.isMorning = c.getInt(6)>0;
				current.jumpToNextNotif = c.getInt(7)>0;
				//Log.d("UserPreferencesDAO.getAllJourneys", Constants.HAS_BEEN_STOPPED + " = " + c.getInt(7));
				listJourney.add(current);
			} while (c.moveToNext());
		}
		return listJourney;
	}
	
	/**
	 * Retrieve from the database the journey from the given journeyId 
	 * (morning or evening) 
	 * @param moment : morning or evening
	 * @return the list of journeys associated
	 */
	public Journey getOneJourney(int journeyId) {
		Journey current = new Journey();
		String whereClause = Constants.JOURNEY_ID + " = " + String.valueOf(journeyId);
		
		Cursor c = sqlLite.query(Constants.TABLE_NAME_JOURNEYS, null, whereClause, null, null, null, null);
		
		if (c.getCount() != 0){
			c.moveToFirst();
			current.journeyId = c.getInt(0);
			current.lineId = c.getString(1);
			current.lineName = c.getString(2);
			current.lineColor = c.getString(3);
			current.stopId = c.getString(4);
			current.journeyDescription = c.getString(5);
			current.isMorning = c.getInt(6)>0;
		}
		return current;
	}
	
	/**
	 * Add a journey in the database
	 * @param journey
	 * @param isMorning
	 * @return
	 */
	public long addJourney (String lineId, String lineName, String lineColor, 
			String stopId, String journeyDescription, boolean isMorning){
		ContentValues addValues = new ContentValues();
		addValues.put(Constants.LINE_ID, lineId);
		addValues.put(Constants.LINE_NAME, lineName);
		addValues.put(Constants.LINE_COLOR, lineColor);
		addValues.put(Constants.STOP_ID, stopId);
		addValues.put(Constants.JOURNEY_DESCRIPTION, journeyDescription);
		if (isMorning){
			addValues.put(Constants.IS_MORNING, 1);
		} else {
			addValues.put(Constants.IS_MORNING, 0);
		}
		return sqlLite.insert(Constants.TABLE_NAME_JOURNEYS, null, addValues);
	}
	
	/**
	 * Delete a journey in the database
	 * @param journeyId
	 * @return
	 */
	public int deleteJourney (int journeyId){
		return sqlLite.delete(Constants.TABLE_NAME_JOURNEYS, Constants.JOURNEY_ID + " = " + journeyId, null);
	}
	
	/**
	 * Update in the database all morning journeys (or evening journeys depending on the boolean isMorning)
	 * Will be called when the user stop manually the notification, the attribute jumpToNextNotif is updating to 1
	 * It will be used, when the user comes back to the application, for not display the notification (if we are in a 
	 * notification period) 
	 * @param isMorning
	 */
	public void updateStopJourney (boolean isMorning){
		// Build Where clause
		String whereClause = Constants.IS_MORNING + " = ";
		if (isMorning){
			whereClause = whereClause + "1";
		} else {
			whereClause = whereClause + "0";
		}
		
		ContentValues updateValues = new ContentValues();
		
		// Update journeys that have been stopped
		updateValues.put(Constants.HAS_BEEN_STOPPED, 1);
		sqlLite.update(Constants.TABLE_NAME_JOURNEYS, updateValues, whereClause, null);
		
		// Update next journeys (for example mornings journeys if we are evening)
		if (isMorning){
			whereClause = Constants.IS_MORNING + " = " + "0";
		} else {
			whereClause = Constants.IS_MORNING + " = " + "1";
		}
		updateValues.put(Constants.HAS_BEEN_STOPPED, 0);
		sqlLite.update(Constants.TABLE_NAME_JOURNEYS, updateValues, whereClause, null);
	}
	
	/**
	 * Initialize all journeys with jumpToNextNotif at false
	 */
	public void initJourney (){
		ContentValues updateValues = new ContentValues();

		// Update journeys that have been stopped
		updateValues.put(Constants.HAS_BEEN_STOPPED, 0);
		sqlLite.update(Constants.TABLE_NAME_JOURNEYS, updateValues, null, null);
	}
	
}
