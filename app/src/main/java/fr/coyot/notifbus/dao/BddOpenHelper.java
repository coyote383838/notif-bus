package fr.coyot.notifbus.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import fr.coyot.notifbus.utils.Constants;

public class BddOpenHelper extends SQLiteOpenHelper {

	public BddOpenHelper(Context context) {
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("BDDOpenHelper.OnCreate", "Create database");
		String createTable = buildTableJourney();
		db.execSQL(createTable);
		createTable = buildTableMessages();
		db.execSQL(createTable);
	}
	
	private String buildTableJourney (){
		StringBuilder createTable = new StringBuilder("CREATE TABLE ");
		createTable.append(Constants.TABLE_NAME_JOURNEYS).append(" (");
		createTable.append(Constants.JOURNEY_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		createTable.append(Constants.LINE_ID).append(" TEXT, ");
		createTable.append(Constants.LINE_NAME).append(" TEXT, ");
		createTable.append(Constants.LINE_COLOR).append(" TEXT, ");
		createTable.append(Constants.STOP_ID).append(" TEXT, ");
		createTable.append(Constants.JOURNEY_DESCRIPTION).append(" TEXT, ");
		createTable.append(Constants.IS_MORNING).append(" INTEGER, ");
		createTable.append(Constants.HAS_BEEN_STOPPED).append(" INTEGER);");
		return createTable.toString();
	}
	
	private String buildTableMessages (){
		StringBuilder createTable = new StringBuilder("CREATE TABLE ");
		createTable.append(Constants.TABLE_NAME_MESSAGES).append(" (");
		createTable.append(Constants.DATABASE_MESSAGE_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		createTable.append(Constants.DATABASE_MESSAGE_TITLE).append(" TEXT, ");
		createTable.append(Constants.DATABASE_MESSAGE_CONTENT).append(" TEXT, ");
		createTable.append(Constants.DATABASE_MESSAGE_TYPE).append(" TEXT, ");
		createTable.append(Constants.DATABASE_MESSAGE_IMPORTANCE).append(" TEXT, ");
		createTable.append(Constants.DATABASE_MESSAGE_EXPIRATION_DATE).append(" TEXT, ");
		createTable.append(Constants.DATABASE_MESSAGE_ALREADY_DISPLAY).append(" INTEGER);");
		return createTable.toString();
	}
	
	private String buildAlterTableJourneys(){
		StringBuilder alterTable = new StringBuilder("ALTER TABLE ");
		alterTable.append(Constants.TABLE_NAME_JOURNEYS);
		alterTable.append(" ADD COLUMN ");
		alterTable.append(Constants.HAS_BEEN_STOPPED).append(" INTEGER;");
		return alterTable.toString();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("BDDOpenHelper.OnUpgrade", "OldVersion => " + String.valueOf(oldVersion));
		Log.d("BDDOpenHelper.OnUpgrade", "NewVersion => " + String.valueOf(newVersion));
		if (oldVersion <= 1){
			Log.d("BDD OnUpgrade", "Create Table Messages");
			String createTable = buildTableMessages();
			db.execSQL(createTable);
		}
		if (oldVersion <=2){
			Log.d("BDD OnUpgrade", "Modify Table journeys");
			String alterTable = buildAlterTableJourneys();
			db.execSQL(alterTable);
		}
	}
	
	
}
