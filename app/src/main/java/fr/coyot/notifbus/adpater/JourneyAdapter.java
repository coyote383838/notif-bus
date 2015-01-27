package fr.coyot.notifbus.adpater;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.coyot.notifbus.R;
import fr.coyot.notifbus.adpater.listener.JourneyAdapterListener;
import fr.coyot.notifbus.model.Journey;
import fr.coyot.notifbus.utils.Generic;

public class JourneyAdapter extends BaseAdapter {

	/**
	 * The list of the journey
	 */
	private List<Journey> myJourneyList;
	
	/**
	 * The context of the adapter
	 */
	private Context myContext;
	
	/**
	 * The layout Inflater
	 */
	private LayoutInflater myInflater;

	/**
	 * The list of listeners
	 */
	private ArrayList<JourneyAdapterListener> mListListener = new ArrayList<JourneyAdapterListener>();
	
	/**
	 * Pour ajouter un listener sur notre adapter
	 */
	public void addListener(JourneyAdapterListener aListener) {
	    mListListener.add(aListener);
	}
	
	private void sendListener(Journey item, int position, boolean isLongClick) {
		if (isLongClick){
			for(int i = mListListener.size()-1; i >= 0; i--) {
				mListListener.get(i).onLongClickJourney(item, position);
			}
		} else {
			for(int i = mListListener.size()-1; i >= 0; i--) {
				mListListener.get(i).onClickJourney(item, position);
			}
		}
		
	}
	
	public JourneyAdapter(List<Journey> myLineList, Context myContext) {
		super();
		this.myJourneyList = myLineList;
		this.myContext = myContext;
		this.myInflater = LayoutInflater.from(myContext);
	}

	@Override
	public int getCount() {
		return myJourneyList.size();
	}

	@Override
	public Object getItem(int position) {
		return myJourneyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layoutItem;
		if (convertView == null) {
			layoutItem = (LinearLayout) myInflater.inflate(R.layout.list_layout, parent, false);
		} else {
			layoutItem = (LinearLayout) convertView;
		}
		
		TextView element = (TextView) layoutItem.findViewById(R.id.Element_Id);
		element.setText(myJourneyList.get(position).lineName);
		element.setBackgroundColor(Color.parseColor(myJourneyList.get(position).lineColor));
		
		element = (TextView) layoutItem.findViewById(R.id.Element_Name);
		element.setText(myJourneyList.get(position).journeyDescription);
		
		element = (TextView) layoutItem.findViewById(R.id.Invisible_Element);
		if (myJourneyList.get(position).listSchedules.isEmpty()){
			element.setVisibility(View.GONE);
		}else if (!element.getText().equals("old")) {
			Log.d("JourneyAdapter.getView", "Element visible");
			element.setVisibility(View.VISIBLE);
			element.setText(Generic.buildNotifContent(myJourneyList.get(position), true));
		}
		
		// Add the listener for the click action
		layoutItem.setTag(position);
		layoutItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LinearLayout layoutItem, layoutSchedule;
				layoutItem = (LinearLayout) v;
				layoutSchedule = (LinearLayout) layoutItem.findViewById(R.id.layoutSchedule);
				TextView element = (TextView) layoutSchedule.findViewById(R.id.Invisible_Element);
				Integer position = (Integer)v.getTag();
				if (element.getVisibility() == View.VISIBLE){
					element.setVisibility(View.GONE);
					myJourneyList.get(position).listSchedules.clear();
				} else {
					//element.setVisibility(View.VISIBLE);
					sendListener(myJourneyList.get(position), position, false);
				}
			}
		});
		layoutItem.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// Retrieve the id of the line that have been clicked
				Integer position = (Integer)v.getTag();
				// Send to the listeners that the user has click on the TextView "Line_Description"
				sendListener(myJourneyList.get(position), position, true);
				return true;
			}
		});
		
		return layoutItem;
	}

}
