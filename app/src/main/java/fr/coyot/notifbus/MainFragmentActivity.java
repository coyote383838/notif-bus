package fr.coyot.notifbus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import fr.coyot.notifbus.async.GetLinesAsync;
import fr.coyot.notifbus.async.GetTisseoMessagesAsync;
import fr.coyot.notifbus.async.ScheduleNotifAsync;
import fr.coyot.notifbus.dao.TisseoMessageDAO;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.model.Message;
import fr.coyot.notifbus.receiver.AlarmReceiver;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class MainFragmentActivity extends FragmentActivity{
	
	/**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

	/**
	 * Object for connect to the database
	 */
	private TisseoMessageDAO daoMessage;
    
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    
	/**
	 * List of all TISSEO lines
	 */
	private ArrayList<Line> listOfLines;
	
	/**
	 * TISSEO Important messages (message that is display in the home page of tisseo.fr)
	 */
	private Message tisseoImportantMessage = new Message();
	
	/**
	 * List of expiration date for lines, messages and stop
	 */
	private HashMap<String,Date> listOfExpirationDate = new HashMap<String, Date>();
	
	/**
	 * Boolean that indicate if we add a morning journey or not
	 */
	private boolean isMorningJourney = false;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d("MainFragmentActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // BDD initialisation
     	daoMessage = new TisseoMessageDAO(this);
        
		// Retrieve TISSEO messages from database
		checkNetworkTisseoMessage();
		
    	// Schedule for next TISSEO messages notification
    	ScheduleNotifAsync scheduleNotif; 
    	if (tisseoImportantMessage.title == null){
    		scheduleNotif = new ScheduleNotifAsync(this, null);
    	}else {
    		scheduleNotif = new ScheduleNotifAsync(this, tisseoImportantMessage.expirationDate);
    	}
    	scheduleNotif.execute("");
    	
    	// Update list of TISSEO lines
    	updateLines(null, true);
    }
    
    /**
     * Initialize the object ViewPager
     * Will be called by the GetLinesAsync when the list of lines has been correctly retrieve
     * @param listLines : 		the new list of lines
	 * @param expirationDate : 	the new expiration date of the list of lines
     */
    public void initViewPager (ArrayList<Line> listLines, String expirationDate){
    	Log.d("MainFragmentActivity", "initViewPager");
    	
        this.listOfLines = listLines;
		// Update expiration date in the local object
		this.listOfExpirationDate.put(Constants.EXPIRATION_LINES, 
				Generic.parseExpirationDate(expirationDate));
    	
    	 // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
            }
        });
    }
    
	@Override
	protected void onStart() {
		Log.d("MainFragmentActivity", "onStart");
		super.onStart();
		
		// Display message
		displayMessages();
		
		// If we are outisde working hours (ie between 10h and 17h or after 20h) we check TISSEO messages
		if (tisseoImportantMessage.title == null){
			Calendar cal = Calendar.getInstance();
			int currentHour = cal.get(Calendar.HOUR_OF_DAY);
			if ((currentHour > 10 && currentHour < 17) || currentHour > 19){
				GetTisseoMessagesAsync messageAsync = new GetTisseoMessagesAsync(this);
				messageAsync.execute();
			}
		}
	}
	
	/**
	 * Display the next hour notification to the user
	 * @param calendarNextNotif
	 */
	public void displayNextNotif(Calendar calendarNextNotif){
		Generic.displayNextHourNotif(this, calendarNextNotif);
	}

	/**
	 * Check if the list of TISSEO lines is up to date (by checking the expiration date)
	 * If not, update the list by retrieving data from TISSEO API
	 * @param intentToLaunch : 	the name of the activity that we want to launch once the updating
	 * 							of lines is done
	 * @param isOnCreate	 :  true is the call of this function come from the onCreate function
	 * 							false otherwise
	 */
	public void updateLines(Class<?> intentToLaunch, boolean isOnCreate){
		if (Generic.isOutOfDate(this.listOfExpirationDate.get(Constants.EXPIRATION_LINES))){
			GetLinesAsync request = new GetLinesAsync(this, intentToLaunch, isOnCreate);
	        request.execute(Generic.buildURLLines());
		} else if (intentToLaunch != null){
			updateListLines(null, null, false, intentToLaunch);
		} else {
			updateListLines(null, null, false, null);
		}
	}
	

	/**
	 * Retrieve TISSEO Network message from database
	 * If there is no message, the next check of this message is scheduled
	 * If there is message in DB, the expiration date is checked and the message is updated if needed
	 */
	private void checkNetworkTisseoMessage(){
		Message oldMessage = tisseoImportantMessage;
		// Open the connection to the database
		daoMessage.open();
		//daoMessage.deleteImportantMessage();
		tisseoImportantMessage = daoMessage.getImportantMessages();
		
		if (tisseoImportantMessage.title != null){
			// Check the expiration date of the message
			Calendar calCurrent = new GregorianCalendar();
			Date expirationDate = Generic.parseExpirationDate(tisseoImportantMessage.expirationDate);
			Calendar calExpiration = new GregorianCalendar();
			calExpiration.setTime(expirationDate);
			if (calCurrent.after(calExpiration)){
				// The TISSEO message in database is out of date, we have to delete it
				Log.d("MainFragmentActivity", "Delete the TISSEO message because it's out of date");
				Log.d("MainFragmentActivity", "CurrentDate => " + calCurrent.toString() + " ExpirationDate => " + calExpiration.toString());
				daoMessage.deleteImportantMessage();
				// Check on the TISSEO API if there is a new network message
				GetTisseoMessagesAsync messageAsync = new GetTisseoMessagesAsync(this);
				messageAsync.execute();
			} else {
				if (oldMessage.title != null){
					tisseoImportantMessage.isAlreadyDisplay = oldMessage.isAlreadyDisplay;
				}
			}
		}
		
		// Close the connection to the database
		daoMessage.close();
	}
    
	/**
	 * Will be called by the GetLinesAsync for updating the list of lines
	 * @param listLines : 	if call by getLinesAsync, will have the new list of lines
	 * 						null otherwise
	 * @param expirationDate : 	if call by getLinesAsync, will have the new expiration date of the list of lines
	 * 							null otherwise
	 * @param isAsync : true if this function is call by getLinesAsync (and we have to update the local list of lines
	 * 					false otherwise
	 * @param intentToLaunch : 	the class that will be used in the intent for start the new activity
	 * 							if it's equal to null, no new activity will be launch
	 */
	public void updateListLines(ArrayList<Line> listLines, String expirationDate, 
			boolean isAsync, Class<?> intentToLaunch){
		if (isAsync){
			this.listOfLines = listLines;
			// Update expiration date in the local object
			this.listOfExpirationDate.put(Constants.EXPIRATION_LINES, 
					Generic.parseExpirationDate(expirationDate));
		}
		if (intentToLaunch != null){
			// Launch the activity for add a journey
			Intent intent = new Intent(this, intentToLaunch);
			intent.putExtra(Constants.LIST_LINES, listOfLines);
			intent.putExtra(Constants.IS_MORNING, isMorningJourney);
			startActivity(intent);
		}
	}
	
	/**
	 * Will be called by the GetTisseoMessagesAsync when this class is instancied by the MainActivity class
	 * @param currentMessage
	 */
	public void updateTisseoMessages(Message currentMessage){
		if (currentMessage.title != null){
			// Store the message in the database
			Generic.updateMessageInBD(daoMessage, currentMessage);
			Date expirationDate = Generic.parseExpirationDate(currentMessage.expirationDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(expirationDate);
			Generic.scheduleMessageNotif(this, cal ,false);
			displayMessages();
		} else {
			Generic.scheduleMessageNotif(this, null ,false);
		}
	}
	
	/**
	 * Will be called :
	 * 		- by the GetTisseoMessagesAsync for display messages
	 * 		- by the main activity if local messages are up to date (with expirationDate = null)
	 * @return
	 */
	public void displayMessages(){
		// Retrieve TISSEO messages from database
		checkNetworkTisseoMessage();
		
		/*if (tisseoImportantMessage.title != null){
			Log.d("displayMessage", tisseoImportantMessage.title);
		}*/
		if (tisseoImportantMessage.title != null && !tisseoImportantMessage.isAlreadyDisplay){
			// Display the trafic info to the user
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(tisseoImportantMessage.title);
			alertDialogBuilder.setMessage(tisseoImportantMessage.content);
			alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            }
	        });
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			
			TextView networkProblems = (TextView) this.
					findViewById(R.id.NetworkProblems);
			networkProblems.setText(tisseoImportantMessage.title);
			networkProblems.setVisibility(View.VISIBLE);
			// Set to true for not display the message all times
			tisseoImportantMessage.isAlreadyDisplay = true;
		} else if (tisseoImportantMessage.title == null) {
			TextView networkProblems = (TextView) this.
					findViewById(R.id.NetworkProblems);
			networkProblems.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Will be call when the user click on the text with network problems
	 * Display an alert box for see the details of the problem
	 * @param view
	 */
	public void onClickTISSEOMessage(View view) {
		Log.d("onClickTISSEOMessage", "Click !");
		// Display the trafic info to the user
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(tisseoImportantMessage.title);
		alertDialogBuilder.setMessage(tisseoImportantMessage.content);
		alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	//*******************************************************************************************
	//									FRAGMENT CLASSE									*****
	//*******************************************************************************************
    
    /**
     * A simple pager adapter that represents 2 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    	
    	private JourneyFragment journeyFragment;
    	
    	private ListLinesFragment listLinesFragment;
    	
    	//private ExampleFragment exampleFragment;
    	
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            journeyFragment = new JourneyFragment();
            listLinesFragment = new ListLinesFragment();
            //exampleFragment = new ExampleFragment();
        }

        @Override
        public Fragment getItem(int position) {
        	Log.d("ScreenSlidePagerAdapter", "getItem => " + position);
        	if (position == 0){
        		// It's the main fragment with the list of journeys saved by the user
        		return journeyFragment;
        	} else if (position == 1){
        		Bundle bundle = new Bundle();
        		bundle.putSerializable(Constants.LIST_LINES, listOfLines);
        		listLinesFragment.setArguments(bundle);
        		return listLinesFragment;
        	} else {
        		//return ScreenSlidePageFragment.create(1);
        		//return exampleFragment;
        		return journeyFragment;
        	}
        }

        
        
        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }
	
	//*******************************************************************************************
	//									ANDROID FUNCTIONS									*****
	//*******************************************************************************************
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(this, AddJourneyActivity.class);
		
		switch (item.getItemId()) {
			case R.id.addJourneyItem:
			case R.id.addJourneyItemActionBar:
				// Display an alert box for user choice if the journey is for morning or evening
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle("Ajouter un trajet pour :");
				alertDialogBuilder.setItems(Constants.LIST_MOMENT, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Save the period (morning or afternoon) when the user want to
						// ad a journey
						if (which == 0){
							isMorningJourney = true;
						} else {
							isMorningJourney = false;
						}
						// Verify if we have the last update of lines
						MainFragmentActivity.this.updateLines(AddJourneyActivity.class, false);	
					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				intent = null;
				break;
			case R.id.settings:
			case R.id.settingsActionBar: 
				intent = new Intent(this, SettingsActivity.class);
				break;
			case R.id.geolocalisationActionBar:
				intent = new Intent(this, GeoTagActivity.class);
				break;
			case R.id.about:
				AlertDialog.Builder dialogBuilderAbout = new AlertDialog.Builder(this);
				dialogBuilderAbout.setView(getLayoutInflater().inflate(R.layout.dialog_about, null));
				dialogBuilderAbout.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				AlertDialog dialogAbout = dialogBuilderAbout.create();
				dialogAbout.show();
				intent = null;
				break;
			case R.id.exit:
				intent = null;
				finish();
				break;
			default:
				intent = null;
		}
		
		if (intent != null) {
			startActivity(intent);
		}
		
		return true;
	}

	/**
	 * Will display a confirmation for exit the application
	 */
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
        	.setTitle("Quitter NotifBus ?")
        	.setMessage("Attention ! Toutes les notifications seront désactivées")
        	.setPositiveButton("Oui", new DialogInterface.OnClickListener()
        	{
		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		            finish();    
		        }
		
		    })
		    .setNegativeButton("Non", null)
		    .show();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Cancel all defined alarms
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intentToCancel = new Intent(this, AlarmReceiver.class);
		PendingIntent pendingIntentToCancel = PendingIntent.getBroadcast(this, 192837, 
				intentToCancel, PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(pendingIntentToCancel);
		Log.d("Alarm", "Alarme arrete");
		
		// Remove all existing notifications
		NotificationManager notifManager = 
				(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancelAll();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
