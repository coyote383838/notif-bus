package fr.coyot.notifbus;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.coyot.notifbus.adpater.DestinationAdapter;
import fr.coyot.notifbus.adpater.LineFragmentAdapter;
import fr.coyot.notifbus.adpater.StopAdapter;
import fr.coyot.notifbus.adpater.listener.DestinationAdapterListener;
import fr.coyot.notifbus.adpater.listener.LineAdapterListener;
import fr.coyot.notifbus.adpater.listener.StopAdapterListener;
import fr.coyot.notifbus.async.GetScheduleAsync;
import fr.coyot.notifbus.async.GetStopAsync;
import fr.coyot.notifbus.model.Destination;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.model.Schedule;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class ListLinesFragment extends Fragment 
implements LineAdapterListener, StopAdapterListener, DestinationAdapterListener{

	/**
	 * The parent activity
	 */
	private FragmentActivity fragmentActivity;
	
	/**
	 * The layout that represent the fragment
	 */
	private RelativeLayout layoutMain;
	
	/**
	 * List of all TISSEO lines
	 */
	private ArrayList<Line> listOfLines;
	
	/**
	 * Selected line
	 */
	private Line selectedLine;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("ListLinesFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ListLinesFragment", "onCreateView");
		// Retrieve the parent activity
		fragmentActivity = super.getActivity();
		
		// Initialize the main linear layout
		layoutMain = (RelativeLayout) inflater.inflate
				(R.layout.fragment_lines, container, false);
		
		// Retrieve the list of lines
		Bundle bundle = this.getArguments();
		if (bundle != null){
			listOfLines = (ArrayList<Line>)bundle.get(Constants.LIST_LINES);
		}
		
		return layoutMain;
	}

	
	@Override
	public void onStart() {
		Log.d("ListLinesFragment", "OnStart");
		super.onStart();
		displayLines(listOfLines);
	}
	
	

	@Override
	public void onResume() {
		Log.d("ListLinesFragment", "onResume");
		super.onResume();
	}

	@Override
	public void onClickLine(Line item, int position) {
		Log.d("ListLinesFragment", "onClickLine");
		this.selectedLine = item;
		
		// Makes invisible textView of destSelected or stopSelected
		TextView textSelectedDest = (TextView)fragmentActivity.findViewById(R.id.TextSelectedDest);
		textSelectedDest.setVisibility(View.INVISIBLE);
		TextView textSelectedStop = (TextView)fragmentActivity.findViewById(R.id.TextSelectedStop);
		textSelectedStop.setVisibility(View.INVISIBLE);
		TextView textNextSchedule = (TextView)fragmentActivity.findViewById(R.id.TextNextSchedules);
		textNextSchedule.setVisibility(View.INVISIBLE);
		TextView textSchedules = (TextView)fragmentActivity.findViewById(R.id.TextSchedules);
		textSchedules.setVisibility(View.INVISIBLE);
		
		// Retrieve the list of physicals stops for the selected line
		GetStopAsync getStops = new GetStopAsync(fragmentActivity, this);
		getStops.execute(Generic.buildURLStops(this.selectedLine.lineId));
	}	
	
	@Override
	public void onClickDestination(Destination item, int position) {
		Log.d("ListLinesFragment", "onClickDestination");
		this.selectedLine.terminusSelected = position;
		
		// Update the text of the line with the selected destination
		TextView textSelectedDest = (TextView)fragmentActivity.findViewById(R.id.TextSelectedDest);
		textSelectedDest.setVisibility(View.VISIBLE);
		textSelectedDest.setText("Vers " + item.destinationName);
		textSelectedDest.setBackgroundColor(Color.parseColor(this.selectedLine.lineColor));
		
		// Update the textView for choice stops
		TextView textChoice = (TextView)fragmentActivity.findViewById(R.id.TextChoiceDestOrStop);
		textChoice.setText(R.string.choiceStop);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textChoice.getLayoutParams();
		layoutParams.addRule(RelativeLayout.BELOW, R.id.TextSelectedDest);
		textChoice.setLayoutParams(layoutParams);
		
		ListView listViewStops = (ListView)fragmentActivity.findViewById(R.id.ListViewStopLineSelected);
		
		// Fill the listview with the list of stops of the selected line
		String destinationId = selectedLine.listOfTerminus[this.selectedLine.terminusSelected].destinationId;
		StopAdapter adapter = new StopAdapter(this.selectedLine.getStops(destinationId), 
				selectedLine, fragmentActivity);
        adapter.addListener(this);
        listViewStops.setAdapter(adapter);
		
	}
	
	@Override
	public void onClickStop(PhysicalStop item, int position) {
		Log.d("ListLinesFragment", "onClickStop");
		
		// Display the selected stop and makes invisible the textView choiceStopOrDest
		TextView textSelectedStop = (TextView)fragmentActivity.findViewById(R.id.TextSelectedStop);
		textSelectedStop.setVisibility(View.VISIBLE);
		textSelectedStop.setBackgroundColor(Color.parseColor(this.selectedLine.lineColor));
		textSelectedStop.setText("Arrêt : " + item.physicalStopName);
		TextView textChoice = (TextView)fragmentActivity.findViewById(R.id.TextChoiceDestOrStop);
		textChoice.setVisibility(View.INVISIBLE);
		ListView listStops = (ListView)fragmentActivity.findViewById(R.id.ListViewStopLineSelected);
		listStops.setVisibility(View.INVISIBLE);
		
		// Retrieve the schedules for the selected stop
		GetScheduleAsync scheduleAsync = new GetScheduleAsync(this, fragmentActivity, item);
		scheduleAsync.execute();
	}
	
	/**
	 * Display the list of the line and add a clickListener for each line
	 * @param listLines
	 */
	public void displayLines(ArrayList<Line> listLines) {
		listOfLines = listLines;
		
		LineFragmentAdapter adapter = new LineFragmentAdapter(listOfLines, fragmentActivity);
		
 		// Add the listener on the list
        adapter.addListener(this);
        // Retrieve the listView component
 		GridView list = (GridView)layoutMain.findViewById(R.id.GridViewLines);
 		// Put the list in the listView
 		list.setAdapter(adapter);
	}
	
	/**
	 * Will be called by the GetStopAsync function when stops for the selected line
	 * have been retrieve from the TISSEO API
	 * @param listStops
	 */
	public void displayDestinations(ArrayList<PhysicalStop> listStops){
		Log.d("ListLinesFragment", "displayDestinations");
		this.selectedLine.listOfStops = listStops;

		// The user search the stop by the list of lines, so the search by id must be disappear
		TextView textSearchID = (TextView)fragmentActivity.findViewById(R.id.TextSearchStopId);
		textSearchID.setVisibility(View.INVISIBLE);
		EditText editSearchID = (EditText)fragmentActivity.findViewById(R.id.SearchStopId);
		editSearchID.setVisibility(View.INVISIBLE);
		Button buttonSearchId = (Button)fragmentActivity.findViewById(R.id.SearchStopButton);
		buttonSearchId.setVisibility(View.INVISIBLE);

		// The textView and listView of stops of the selected line are visible now
		TextView textSelectedLine = (TextView)fragmentActivity.findViewById(R.id.TextSelectedLine);
		textSelectedLine.setVisibility(View.VISIBLE);
		textSelectedLine.setText("Ligne " + this.selectedLine.lineShortName);
		textSelectedLine.setBackgroundColor(Color.parseColor(this.selectedLine.lineColor));
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textSelectedLine.getLayoutParams();
		layoutParams.addRule(RelativeLayout.BELOW);
		textSelectedLine.setLayoutParams(layoutParams);
		TextView textChoice = (TextView)fragmentActivity.findViewById(R.id.TextChoiceDestOrStop);
		textChoice.setVisibility(View.VISIBLE);
		ListView listViewStops = (ListView)fragmentActivity.findViewById(R.id.ListViewStopLineSelected);
		listViewStops.setVisibility(View.VISIBLE);
		
		// The list of lines are shifted down 
		TextView textSearchByLine = (TextView)fragmentActivity.findViewById(R.id.TextSearchByLine);
		layoutParams = (RelativeLayout.LayoutParams)textSearchByLine.getLayoutParams();
		layoutParams.addRule(RelativeLayout.BELOW, R.id.ListViewStopLineSelected);
		textSearchByLine.setLayoutParams(layoutParams);
		
		// Display the destinations
		DestinationAdapter adapter = new DestinationAdapter(this.selectedLine.listOfTerminus, 
				this.selectedLine, fragmentActivity);
		adapter.addListener(this);
		listViewStops.setAdapter(adapter);
		
	}
	
	/**
	 * Will be called by the GetScheduleAsync function when schedules for the selected stop will
	 * be retrieved from the TISSEO API
	 * @param listSchedule
	 */
	public void displaySchedules(ArrayList<Schedule> listSchedule){
		Log.d("ListLinesFragment", "displaySchedules");
		
		// Build the text with schedules to dislay
		StringBuilder builder = new StringBuilder();
		Schedule current = null;
		if (listSchedule.size() >= 1){
			current = listSchedule.get(0);
			if (current.scheduleTime != null){
				builder.append(current.scheduleTime);
				if (!current.realTime){
					builder.append("*");
				}
			}
		}else {
			builder.append("Pas de prochains passages");
		}
		if (listSchedule.size() >= 2){
			current = listSchedule.get(1);
			if (current.scheduleTime != null){
				builder.append(" et " + current.scheduleTime);
				if (!current.realTime){
					builder.append("*");
				}
			}
		}
		
		// Display the retrieve schedules
		TextView textNextSchedules = (TextView)fragmentActivity.findViewById(R.id.TextNextSchedules);
		textNextSchedules.setVisibility(View.VISIBLE);
		TextView textschedules = (TextView)fragmentActivity.findViewById(R.id.TextSchedules);
		textschedules.setVisibility(View.VISIBLE);
		textschedules.setText(builder.toString());
		
		// Display the list of lines if user want to have another schedule
		TextView textSearchByLine = (TextView)fragmentActivity.findViewById(R.id.TextSearchByLine);
		textSearchByLine.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textSearchByLine.getLayoutParams();
		layoutParams.addRule(RelativeLayout.BELOW, R.id.TextSchedules);
		textSearchByLine.setLayoutParams(layoutParams);
		GridView gridListLines = (GridView)fragmentActivity.findViewById(R.id.GridViewLines);
		gridListLines.setVisibility(View.VISIBLE);
		
	}
	
	@Override
	public void onDestroy() {
		Log.d("ListLinesFragment", "onDestroy");
		super.onDestroy();
	}
	
}
