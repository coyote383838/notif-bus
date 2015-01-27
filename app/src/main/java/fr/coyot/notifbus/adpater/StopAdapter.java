package fr.coyot.notifbus.adpater;

import java.util.ArrayList;
import java.util.List;

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
import fr.coyot.notifbus.adpater.listener.StopAdapterListener;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.model.PhysicalStop;

public class StopAdapter extends BaseAdapter {

	/**
	 * The list of the physical stop 
	 */
	private List<PhysicalStop> myStopList;
	
	/**
	 * The line selected
	 */
	private Line selectedLine;
	
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
	private ArrayList<StopAdapterListener> mListListener = new ArrayList<StopAdapterListener>();
	
	/**
	 * Pour ajouter un listener sur notre adapter
	 */
	public void addListener(StopAdapterListener aListener) {
	    mListListener.add(aListener);
	}
	
	private void sendListener(PhysicalStop item, int position) {
	    for(int i = mListListener.size()-1; i >= 0; i--) {
	    	mListListener.get(i).onClickStop(item, position);
	    }
	}
	
	public StopAdapter(List<PhysicalStop> stopList, Line selectedLine, Context context) {
		super();
		this.myStopList = stopList;
		this.selectedLine = selectedLine;
		this.myContext = context;
		this.myInflater = LayoutInflater.from(myContext);
	}

	@Override
	public int getCount() {
		return myStopList.size();
	}

	@Override
	public Object getItem(int position) {
		return myStopList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layoutItem;
		if (convertView == null) {
			layoutItem = (LinearLayout) myInflater.inflate(R.layout.list_layout_stops, parent, false);
		} else {
			layoutItem = (LinearLayout) convertView;
		}
		TextView element = (TextView) layoutItem.findViewById(R.id.SearchStopLineId);
		element.setText(selectedLine.lineShortName);
		element.setBackgroundColor(Color.parseColor(selectedLine.lineColor));
		element = (TextView) layoutItem.findViewById(R.id.SearchStopName);
		element.setText(myStopList.get(position).physicalStopName);
		
		// Add the listener for the click action
		layoutItem.setTag(position);
		layoutItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Retrieve the id of the line that have been clicked
				Integer position = (Integer)v.getTag();
				// Send to the listener that the user has click on a layout that represent a line
				sendListener(myStopList.get(position), position);
				
			}
		});
		
		return layoutItem;
	}

}
