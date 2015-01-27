package fr.coyot.notifbus;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;
import fr.coyot.notifbus.adpater.LineAdapter;
import fr.coyot.notifbus.adpater.listener.LineAdapterListener;
import fr.coyot.notifbus.async.GetStopAsync;
import fr.coyot.notifbus.dao.UserPreferencesDAO;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class AddJourneyActivity extends Activity implements LineAdapterListener {

	boolean isMorning;
	
	private Line selectedLine;
	
	private ArrayList<Line> listOfLines;
	
	private ArrayList<PhysicalStop> listOfStops;
	
	//private StopArea stopAreaSelected;
	
	private UserPreferencesDAO dao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_journey);
		Intent intent = getIntent();
		isMorning = intent.getBooleanExtra(Constants.IS_MORNING, false);
		Bundle bundle = intent.getExtras();
		listOfLines = (ArrayList<Line>)bundle.get(Constants.LIST_LINES);
		
		// Database init
		dao = new UserPreferencesDAO(this);
		
		// Retrieve all lines bus information
		displayLines(listOfLines);
	}

	@Override
	public void onClickLine(Line item, int position) {
		selectedLine = item;
		// Retrieve stops of the selected line
		GetStopAsync request = new GetStopAsync(this);
        /*request.execute(Generic.buildURLStopArea(selectedLine.lineId), 
        		Generic.buildURLStops(selectedLine.lineId));*/
        request.execute(Generic.buildURLStops(selectedLine.lineId));
	}
	
	/**
	 * Display the list of the line and add a clickListener for each line
	 * @param listLines
	 */
	public void displayLines(ArrayList<Line> listLines) {
		
		LineAdapter adapter = new LineAdapter(listLines, this);
		
 		// Add the listener on the list
        adapter.addListener(this);
        // Retrieve the listView component
 		ListView list = (ListView)findViewById(R.id.ListViewLines);
 		// Put the list in the listView
 		list.setAdapter(adapter);
	}
	
	/**
	 * Called be the GetStopAsync class when we have retrieve all physical stops for 
	 * the line selected by the user
	 * Ask the user to choice the destination of the line
	 * @param listStops
	 */
	public void displayDestination(ArrayList<PhysicalStop> listStops){
		// Update the class member with the list of Stops that is return
		this.selectedLine.listOfStops = listStops;
		
		// Display an alert dialog for choice the destination
		String[] listDestination = new String[selectedLine.listOfTerminus.length];
		for (int i=0; i<selectedLine.listOfTerminus.length; i++){
			listDestination[i] = "Vers " + selectedLine.listOfTerminus[i].destinationName;
		}
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Sélectionner la destination");
		alertDialogBuilder.setItems(listDestination, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AddJourneyActivity.this.displayDialogStops(which);
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	/**
	 * Display all stops of line selected for the direction selected
	 * @param stopSelected
	 */
	public void displayDialogStops(int destinationSelected){
		// Get the destination Id selected by the user
		String destinationId = selectedLine.listOfTerminus[destinationSelected].destinationId;
		this.selectedLine.terminusSelected = destinationSelected;
		
		// Retrieve the list of stops for the destination chosen by the user 
		this.listOfStops = this.selectedLine.getStops(destinationId);
		Collections.sort(this.listOfStops);
		String[] listOfStopsName = new String[this.listOfStops.size()];
		for (int i=0; i<this.listOfStops.size(); i++){
			listOfStopsName[i] = this.listOfStops.get(i).physicalStopName;
		}
		
		// Build the AlertDialog and show it
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Sélectionner un arrêt");
		alertDialogBuilder.setItems(listOfStopsName, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AddJourneyActivity.this.addJourney(which);
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	/**
	 * Add the journey in the database
	 * @param stopSelected
	 */
	public void addJourney(int indexStopSelected){
		// Open the connection to the database
		dao.open();
		// Build the description of the journey
		StringBuilder builder = new StringBuilder();
		builder.append("Arrêt : ");
		builder.append(this.listOfStops.get(indexStopSelected));
		builder.append("\r\nVers : ");
		builder.append(this.selectedLine.listOfTerminus[this.selectedLine.terminusSelected].destinationName);
		// Add journey in database
		dao.addJourney(selectedLine.lineId, selectedLine.lineShortName, 
				selectedLine.lineColor, this.listOfStops.get(indexStopSelected).physicalStopId, 
				builder.toString(), isMorning);
		// CLose the connection to the database
		dao.close();
		// Display a toast message for indicate that the journey has been added
		Toast.makeText(this, "Votre trajet a été ajouté", Toast.LENGTH_LONG).show();
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_line, menu);
		return true;
	}

}
