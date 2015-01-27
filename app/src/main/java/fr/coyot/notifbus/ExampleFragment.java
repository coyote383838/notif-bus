package fr.coyot.notifbus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class ExampleFragment extends Fragment{

	/**
	 * The parent activity
	 */
	private FragmentActivity fragmentActivity;
	
	/**
	 * The layout that represent the fragment
	 */
	private RelativeLayout layoutMain;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("ExampleFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ExampleFragment", "onCreateView");
		// Retrieve the parent activity
		fragmentActivity = super.getActivity();
		
		// Initialize the main linear layout
		layoutMain = (RelativeLayout) inflater.inflate
				(R.layout.fragment_lines, container, false);
		
		return layoutMain;
	}

	
	@Override
	public void onStart() {
		Log.d("ExampleFragment", "OnStart");
		super.onStart();
	}
	
	

	@Override
	public void onResume() {
		Log.d("ExampleFragment", "onResume");
		super.onResume();
	}

	
}
