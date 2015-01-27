package fr.coyot.notifbus.dao;

import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.coyot.notifbus.model.Message;
import fr.coyot.notifbus.utils.Constants;

public class TisseoMessageDAO implements Serializable {

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
	public TisseoMessageDAO(Context context) {
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
	public Message getImportantMessages() {
		Message current = new Message();
		String whereClause = Constants.DATABASE_MESSAGE_IMPORTANCE + " = 'important'";

		Cursor c = sqlLite.query(Constants.TABLE_NAME_MESSAGES, null, whereClause, null, null, null, null);
		
		Log.d("getMessageDAO", String.valueOf(c.getCount()) + " messages récupéré(s)");
		
		if (c.getCount() != 0){
			c.moveToFirst();
			current.id = c.getInt(0);
			current.title = c.getString(1);
			current.content = c.getString(2);
			current.type = c.getString(3);
			current.importance = c.getString(4);
			current.expirationDate = c.getString(5);
			current.isAlreadyDisplay = c.getInt(6)>0;
		}
		return current;
	}
	
	/**
	 * Add a journey in the database
	 * @param journey
	 * @param isMorning
	 * @return
	 */
	public long addMessage (String title, String content, String type, 
			String importance, String expirationDate){
		Log.d("addMessageDAO", "Add a new important message in database");
		ContentValues addValues = new ContentValues();
		addValues.put(Constants.DATABASE_MESSAGE_TITLE, title);
		addValues.put(Constants.DATABASE_MESSAGE_CONTENT, content);
		addValues.put(Constants.DATABASE_MESSAGE_TYPE, type);
		addValues.put(Constants.DATABASE_MESSAGE_IMPORTANCE, importance);
		addValues.put(Constants.DATABASE_MESSAGE_EXPIRATION_DATE, expirationDate);
		addValues.put(Constants.DATABASE_MESSAGE_ALREADY_DISPLAY, 0);
		
		return sqlLite.insert(Constants.TABLE_NAME_MESSAGES, null, addValues);
	}
	
	/**
	 * Delete a message in the database
	 * @param messageId
	 * @return
	 */
	public int deleteImportantMessage (){
		Log.d("deleteMessageDAO", "Delete an important message in database");
		return sqlLite.delete(Constants.TABLE_NAME_MESSAGES, Constants.DATABASE_MESSAGE_IMPORTANCE + " = 'important'", null);
	}
	
	
}
