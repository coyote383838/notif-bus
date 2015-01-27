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
import fr.coyot.notifbus.adpater.listener.LineAdapterListener;
import fr.coyot.notifbus.model.Line;

public class LineFragmentAdapter extends BaseAdapter {

	/**
	 * The list of the lines
	 */
	private List<Line> myLineList;
	
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
	private ArrayList<LineAdapterListener> mListListener = new ArrayList<LineAdapterListener>();
	
	/**
	 * Pour ajouter un listener sur notre adapter
	 */
	public void addListener(LineAdapterListener aListener) {
	    mListListener.add(aListener);
	}
	
	private void sendListener(Line item, int position) {
	    for(int i = mListListener.size()-1; i >= 0; i--) {
	    	mListListener.get(i).onClickLine(item, position);
	    }
	}
	
	public LineFragmentAdapter(List<Line> myLineList, Context myContext) {
		super();
		this.myLineList = myLineList;
		this.myContext = myContext;
		this.myInflater = LayoutInflater.from(myContext);
	}

	@Override
	public int getCount() {
		return myLineList.size();
	}

	@Override
	public Object getItem(int position) {
		return myLineList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layoutItem;
		if (convertView == null) {
			layoutItem = (LinearLayout) myInflater.inflate(R.layout.list_grid_lines, parent, false);
		} else {
			layoutItem = (LinearLayout) convertView;
		}
		TextView element = (TextView) layoutItem.findViewById(R.id.Element_Id);
		element.setText(myLineList.get(position).lineShortName);
		element.setBackgroundColor(Color.parseColor(myLineList.get(position).lineColor));
/*		element = (TextView) layoutItem.findViewById(R.id.Element_Name);
		element.setText(myLineList.get(position).lineDescription);*/
		
		// Add the listener for the click action
		layoutItem.setTag(position);
		layoutItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Retrieve the id of the line that have been clicked
				Integer position = (Integer)v.getTag();
				// Send to the listener that the user has click on a layout that represent a line
				sendListener(myLineList.get(position), position);
				
			}
		});
		
		return layoutItem;
	}

}
