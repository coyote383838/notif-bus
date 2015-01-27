package fr.coyot.notifbus;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import fr.coyot.notifbus.adpater.JourneyAdapter;
import fr.coyot.notifbus.adpater.listener.JourneyAdapterListener;
import fr.coyot.notifbus.async.RefreshAsync;
import fr.coyot.notifbus.async.ScheduleNotifAsync;
import fr.coyot.notifbus.dao.UserPreferencesDAO;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.utils.Generic;

public class JourneyFragment extends Fragment implements JourneyAdapterListener {
	
	/**
	 * Object for connect to the database
	 */
	private UserPreferencesDAO dao;
	
	/**
	 * List of morning journeys
	 */
	private ArrayList<Journey> listMorningJourneys;
	
	/**
	 * List of evening journeys
	 */
	private ArrayList<Journey> listEveningJourneys;

	/**
	 * The parent activity 
	 */
	private FragmentActivity fragmentActivity;
	
	/**
	 * The layout that represent this fragment
	 */
	private LinearLayout layoutMain;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("JourneyFragment", "OnCreateView");
		
		// Retrieve the parent activity
		fragmentActivity = super.getActivity();
		
		// Initialize the main linear layout
		layoutMain = (LinearLayout) inflater.inflate
				(R.layout.fragment_journeys, container, false);
		
		return layoutMain;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("JourneyFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		// Force orientation to portrait
		fragmentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// BDD initialisation
		dao = new UserPreferencesDAO(fragmentActivity);
		
		// Initialize all journeys
		dao.open();
		dao.initJourney();
		dao.close();
	}


	@Override
	public void onStart() {
		Log.d("JourneyFragment", "OnStart");
		super.onStart();
		
		// Retrieve user journeys in database
		boolean isJourneyInDB = getJourney(false);
		
		if (isJourneyInDB){
			boolean jumpToNextNotif = false;
			// Verify if the user has not clicked to stop notification
			if (Generic.isMorning() && !this.listMorningJourneys.isEmpty()){
				jumpToNextNotif = this.listMorningJourneys.get(0).jumpToNextNotif;
			} else if (!this.listEveningJourneys.isEmpty()){
				jumpToNextNotif = this.listEveningJourneys.get(0).jumpToNextNotif;
			}
			// Schedule next alarm in an async task
			ScheduleNotifAsync scheduleNotif = new ScheduleNotifAsync(
					(MainFragmentActivity)fragmentActivity, jumpToNextNotif);
			scheduleNotif.execute("");
		}
		
	}
	
	/**
	 * Retrieve all user's journey from the database
	 * @param isManualRefresh
	 * @return true if there is journeys in database or false if not
	 */
	private boolean getJourney(boolean isManualRefresh) {
		// Open the connection to the database
		dao.open();
		
		// Retrieve the list of journeys for the morning
		listMorningJourneys = dao.getAllJourneys(true);
		updateListView(R.id.ListViewJourney, true);
		
 		// Retrieve the list of journeys for the morning
		listEveningJourneys = dao.getAllJourneys(false);
		updateListView(R.id.ListViewJourneyEvening, false);
 		
 		// Close the connection to the database
 		dao.close();
 		
 		return (listMorningJourneys.size() > 0) || (listEveningJourneys.size() > 0);
	}
	
	/**
	 * Function that will be called by the async function when we are retrieve schedules
	 * @param listJourneysUpdated
	 */
	public void updateMorningJourneys(ArrayList<Journey> listJourneysUpdated){
		if (listJourneysUpdated.size() == 1){
			// Only 1 journey has been updated, replaced this one in this list
			int index = this.listMorningJourneys.indexOf(listJourneysUpdated.get(0));
			this.listMorningJourneys.set(index, listJourneysUpdated.get(0));
		}else {
			this.listMorningJourneys = listJourneysUpdated;
		}
 		updateListView(R.id.ListViewJourney, true);
	}
	
	/**
	 * Function that will be called by the async function when we are retrieve schedules
	 * @param listJourneysUpdated
	 */
	public void updateEveningJourneys(ArrayList<Journey> listJourneysUpdated){
		if (listJourneysUpdated.size() == 1){
			// Only 1 journey has been updated, replaced this one in this list
			int index = this.listEveningJourneys.indexOf(listJourneysUpdated.get(0));
			this.listEveningJourneys.set(index, listJourneysUpdated.get(0));
		}else {
			this.listEveningJourneys = listJourneysUpdated;
		}
		updateListView(R.id.ListViewJourneyEvening, false);
	}
	
	/**
	 * Update the content of the listview 
	 * @param idView
	 * @param isMorning
	 */
	private void updateListView(int idView, boolean isMorning){
		JourneyAdapter adapter;
		if (isMorning){
			adapter = new JourneyAdapter(listMorningJourneys, fragmentActivity);
		} else {
			adapter = new JourneyAdapter(listEveningJourneys, fragmentActivity);
		}
 		// Add the listener on the list
        adapter.addListener(this);
        // Retrieve the listView component
 		ListView listMoring = (ListView)fragmentActivity.findViewById(idView);
 		// Put the list in the listView
 		listMoring.setAdapter(adapter);
	}

	
	//*******************************************************************************************
	//									ANDROID FUNCTIONS									*****
	//*******************************************************************************************
	
	@Override
	public void onClickJourney(Journey item, int position) {
		// TODO Auto-generated method stub
		RefreshAsync refresh;
		refresh = new RefreshAsync(this, fragmentActivity, item);
		// Launch async task for refresh schedules for the current journey
		refresh.execute(String.valueOf(item.isMorning));
	}
	
	@Override
	public void onLongClickJourney(Journey item, int position) {
		final Journey currentItem = item;
		
		String[] listActions = {"Rafraîchir les horaires", "Supprimer cette ligne"};
		
		new AlertDialog.Builder(fragmentActivity)
			.setTitle(item.toString())
			.setItems(listActions, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0){
						// User clicked on refresh
						RefreshAsync refresh;
						refresh = new RefreshAsync(JourneyFragment.this, fragmentActivity, currentItem);
						// Launch async task for refresh schedules for the current journey
						refresh.execute(String.valueOf(currentItem.isMorning));
					} else {
						// User clicked on delete the line
						new AlertDialog.Builder(fragmentActivity)
						 .setTitle("Confirmation")
						 .setMessage("Voulez vous supprimer ce trajet")
						 .setPositiveButton("OUI", new DialogInterface.OnClickListener()
						 {
							  public void onClick(DialogInterface dialog, int whichButton)
							  {
								  dao.open();
								  dao.deleteJourney(currentItem.journeyId);
								  dao.close();
								  getJourney(false);
							  }
						 })
						 .setNegativeButton("NON", new DialogInterface.OnClickListener() {
						 public void onClick(DialogInterface dialog, int whichButton) {
							  //User clicked Cancel so do some stuff 
						 }
					 })
						 .show();
					}
					
				}
			}).show();
	}
	

	
}
