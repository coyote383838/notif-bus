package fr.coyot.notifbus.adpater;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.coyot.notifbus.R;
import fr.coyot.notifbus.adpater.listener.GeoTagAdapterListener;
import fr.coyot.notifbus.model.PhysicalStop;

public class GeoTagAdapter extends BaseAdapter {

	/**
	 * The list of the lines
	 */
	private ArrayList<PhysicalStop> listPhysicalStops;
	
	/**
	 * The layout Inflater
	 */
	private LayoutInflater myInflater;
	
	/**
	 * The list of listeners
	 */
	private ArrayList<GeoTagAdapterListener> mListListener = new ArrayList<GeoTagAdapterListener>();
	
	/**
	 * Pour ajouter un listener sur notre adapter
	 */
	public void addListener(GeoTagAdapterListener aListener) {
	    mListListener.add(aListener);
	}
	
	private void sendListener(PhysicalStop item, int position) {
		for(int i = mListListener.size()-1; i >= 0; i--) {
			mListListener.get(i).onClickPhysicalStop(item, position);
		}
		
	}
	
	public GeoTagAdapter(ArrayList<PhysicalStop> myListPhysicalStops, Context myContext) {
		super();
		this.listPhysicalStops = myListPhysicalStops;
		this.myInflater = LayoutInflater.from(myContext);
	}

	@Override
	public int getCount() {
		return listPhysicalStops.size();
	}

	@Override
	public Object getItem(int position) {
		return listPhysicalStops.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layoutItem;
		if (convertView == null) {
			layoutItem = (LinearLayout) myInflater.inflate(R.layout.list_layout_geotag, parent, false);
		} else {
			layoutItem = (LinearLayout) convertView;
		}
		
		PhysicalStop currentStop = (PhysicalStop)getItem(position);
		
		TextView element = (TextView) layoutItem.findViewById(R.id.GeoTagShortName);
		element.setText(currentStop.line.lineShortName);
		element.setBackgroundColor(Color.parseColor(currentStop.line.lineColor));
		
		element = (TextView) layoutItem.findViewById(R.id.GeoTagDesc);
		
		if (currentStop.listSchedules.get(0).scheduleTime != null){
			StringBuilder txtDestination = new StringBuilder("Vers : " + currentStop.destinationName);
			txtDestination.append("\r\n");
			txtDestination.append(currentStop.listSchedules.get(0).scheduleTime);
			if (currentStop.listSchedules.size() > 1){
				txtDestination.append(" et ");
				txtDestination.append(currentStop.listSchedules.get(1).scheduleTime);
			}
			element.setText(txtDestination);
		} else {
			element.setText("Aucun bus a cette  heure");
		}
		
		// Add the listener for the click action
		layoutItem.setTag(position);
		layoutItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Integer position = (Integer)v.getTag();
				sendListener(listPhysicalStops.get(position), position);
			}
		});

		return layoutItem;
	}

}
