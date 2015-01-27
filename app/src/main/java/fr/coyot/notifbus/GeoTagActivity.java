package fr.coyot.notifbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.coyot.notifbus.adpater.GeoTagAdapter;
import fr.coyot.notifbus.adpater.listener.GeoTagAdapterListener;
import fr.coyot.notifbus.async.GetSchedulesGeoTagAsync;
import fr.coyot.notifbus.async.GetStopAreasGPSAsync;
import fr.coyot.notifbus.model.Line;
import fr.coyot.notifbus.model.PhysicalStop;
import fr.coyot.notifbus.model.StopArea;
import fr.coyot.notifbus.utils.Constants;
import fr.coyot.notifbus.utils.Generic;

public class GeoTagActivity extends Activity implements LocationListener, 
						OnMapClickListener, InfoWindowAdapter, OnInfoWindowClickListener, 
						GeoTagAdapterListener, OnMyLocationButtonClickListener{
	
	private ProgressDialog dialogGeoTag;
	
	private Location myLocation;
	
	private Location myCurrentLocation;
	
	private GoogleMap map;
	
	private HashMap<String, StopArea> listStopArea;
	
	private ArrayList<PhysicalStop> listPhysicalStops;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geo_tag);
		// Display progress bar to the user for doing geotag
		dialogGeoTag = new ProgressDialog(this);
		dialogGeoTag.setMessage("Géolocalisation en cours ....");
		dialogGeoTag.show();
		// Retrieve user location
		geotag();

		// Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        
        map.setMyLocationEnabled(true);
        
        map.setOnMyLocationButtonClickListener(this);
        
        // Disable built-in zoom controls in the bottom right hand corner of the map
        map.getUiSettings().setZoomControlsEnabled(false);

	}

	/**
	 * Retrieve user location from GPS (if activated) or from the network
	 */
	public void geotag(){
		LocationManager lManager;
		Log.d("Localisation", "Retrieve LOCATION_SERVICE");
		lManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
			Log.d("Localisation", "Retrieve GPS_PROVIDER");
		} else if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
			Log.d("Localisation", "Retrieve NETWORK_PROVIDER");
		} else {
			// The location has not be activated by the user
			// Inform the user that this functionnality 
			if (this.dialogGeoTag.isShowing()){
				this.dialogGeoTag.dismiss();
			}
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(Constants.LOCATION_NOT_ENABLE_TITLE);
			alertDialogBuilder.setMessage(Constants.LOCATION_NOT_ENABLE_CONTENT);
			alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	onDestroy();
	            }
	        });
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			
		}
		
	}
	
	/**
	 * Display to the user all stop areas that are around him
	 */
	public void displayGeoTagStopArea(HashMap<String, StopArea> listStopAreas){
		// Remove progress dialog
		if (this.dialogGeoTag.isShowing()){
			this.dialogGeoTag.dismiss();
		}
		
		this.listStopArea = listStopAreas;
		
		map.setOnMapClickListener(this);
		
		map.setInfoWindowAdapter(this);
		
		map.setOnInfoWindowClickListener(this);
		
		LatLng currentLatLng;
		Iterator<String> it = this.listStopArea.keySet().iterator();
		
		while (it.hasNext()){
			String stopAreaId = it.next();
			StopArea current = this.listStopArea.get(stopAreaId);
			currentLatLng = new LatLng(current.gpsLocation.getLatitude(), 
					current.gpsLocation.getLongitude());
			map.addMarker(new MarkerOptions()
	        	.title(current.stopAreaName)
	        	.snippet(String.valueOf(current.distance) + " m|" + stopAreaId)
	        	.position(currentLatLng));
		}
		
	}
	
	public void updateSchedulesUI(ArrayList<PhysicalStop> listPhysicalStop){
		// Update the list we the return physical stop
		this.listPhysicalStops = listPhysicalStop;
		
		// Update of the list view
		GeoTagAdapter adapter = new GeoTagAdapter(listPhysicalStops, this);
		GridView listViewLines = (GridView)findViewById(R.id.gridViewGeoTag);
		adapter.addListener(this);
		listViewLines.setAdapter(adapter);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onLocationChanged(Location location) {
		Log.d("onLocationChanged", "Location ok");
		
		this.myLocation = new Location(location);
		this.myCurrentLocation = new Location(location);
		
		// Stop location update
		LocationManager lManager;
		lManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lManager.removeUpdates(this);
		
		// Set the maps camera on my location
		LatLng myPosition = new LatLng(this.myLocation.getLatitude(), this.myLocation.getLongitude());
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, Float.valueOf("14.8")));
		
		// Location Roseraie
		//this.myLocation.setLatitude(Double.parseDouble("43.619758"));
		//this.myLocation.setLongitude(Double.parseDouble("1.469032"));
		
		// Location Jean Jaurès
		/*this.myLocation.setLatitude(Double.parseDouble("43.606661"));
    	this.myLocation.setLongitude(Double.parseDouble("1.449896"));*/
		
		Log.d("MY LOCATION", String.valueOf(this.myLocation.getLongitude()) + "  " + String.valueOf(this.myLocation.getLatitude()));
		Log.d("MY LOCATION", "Precision ==> " + String.valueOf(this.myLocation.getAccuracy()));
		
		Log.d("MY CURRENT LOCATION", String.valueOf(this.myCurrentLocation.getLongitude()) + "  " + String.valueOf(this.myCurrentLocation.getLatitude()));
		Log.d("MY CURRENT LOCATION", "Precision ==> " + String.valueOf(this.myCurrentLocation.getAccuracy()));
		
		// Call the asynchronous command for retrieve proximity stops
		GetStopAreasGPSAsync stopGPS = new GetStopAreasGPSAsync(this, this.myLocation);
		stopGPS.execute("");
	}

	@Override
	public void onProviderDisabled(String provider) {
		LocationManager lManager;
		lManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lManager.removeUpdates(this);
		Log.d("onProviderDisabled", "");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.d("onProviderEnabled", "");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.d("onStatusChanged", "");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LocationManager lManager;
		lManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lManager.removeUpdates(this);
		Log.d("onDestroy", "Geolocalisation stoppée");
	}

	@Override
	public void onMapClick(LatLng arg0) {
		/*LinearLayout geoTagLayout = (LinearLayout) findViewById(R.id.layoutStopGeoTag);
		geoTagLayout.setVisibility(View.GONE);*/
		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutSelectedMarker);
		layout.setVisibility(View.GONE);
		
		// Clear the map
		map.clear();
		
		// Show to the user where he was clicked
		map.addMarker(new MarkerOptions()
	    	.title("Vous avez cliqué ici")
	    	.snippet("0")
	    	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
	    	.position(arg0));
		
		// Retrieve stops arround the point click by the user
		this.myLocation.setLatitude(arg0.latitude);
		this.myLocation.setLongitude(arg0.longitude);
		// Call the asynchronous command for retrieve proximity stops
		GetStopAreasGPSAsync stopGPS = new GetStopAreasGPSAsync(this, this.myLocation);
		stopGPS.execute("");
		
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// Another marker has been click, so we mask physical stops of the previous marker
		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutSelectedMarker);
		layout.setVisibility(View.GONE);
		
		View v = getLayoutInflater().inflate(R.layout.snippet_maps, null);
		TextView txtView = (TextView) v.findViewById(R.id.SnippetTitle);
		txtView.setText(arg0.getTitle());

		if (!arg0.getSnippet().equals("0")){
			String[] listInfos = arg0.getSnippet().split("\\|");
			HashMap<String, Line> listLines = this.listStopArea.get(listInfos[1]).listOfLines;
			
			txtView = (TextView) v.findViewById(R.id.SnippetName);
			txtView.setText(listInfos[0]);
			
			Iterator<String> it = listLines.keySet().iterator();
			
			int i = 0;
			while (it.hasNext() && i<18){
				Line currentLine = listLines.get(it.next());
				if (i == 6){
					LinearLayout layoutSnippet = (LinearLayout) v.findViewById(R.id.layoutSnippet2);
					layoutSnippet.setVisibility(View.VISIBLE);
				} else if (i == 12){
					LinearLayout layoutSnippet = (LinearLayout) v.findViewById(R.id.layoutSnippet3);
					layoutSnippet.setVisibility(View.VISIBLE);
				}
				txtView = (TextView) v.findViewById(Generic.getIdGeoTag(i));
				txtView.setText(currentLine.lineShortName);
				txtView.setBackgroundColor(Color.parseColor(currentLine.lineColor));
				i++;
			}
		}
		return v;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// If the user click on the marker that represent where he has clicked
		// there is no action to do
		if (!arg0.getSnippet().equals("0")){
			LinearLayout layout = (LinearLayout) findViewById(R.id.layoutSelectedMarker);
			layout.setVisibility(View.VISIBLE);
			// Retrieve the id of the current StopArea and retrive of the list of the lines
			String[] listInfos = arg0.getSnippet().split("\\|");
			listPhysicalStops = this.listStopArea.get(listInfos[1]).listPhysicalStops;
			
			TextView txt = (TextView)findViewById(R.id.stopAreaSelected);
			txt.setText(this.listStopArea.get(listInfos[1]).stopAreaName);
			
			GetSchedulesGeoTagAsync scheduleGeoTag = new GetSchedulesGeoTagAsync(this, this.listPhysicalStops);
			scheduleGeoTag.execute("");
		}
		
	}

	@Override
	public void onClickPhysicalStop(PhysicalStop item, int position) {
		Log.d("onClickPhysicalStop", item.destinationName);
		
	}

	@Override
	public void onBackPressed() {
		// User has clicked on back button, if some schedules are display it will remove from the sceen
		// In all other cases it will return on the main activity
		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutSelectedMarker);
		if (layout.getVisibility() == View.VISIBLE){
			layout.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// Clear the map
		map.clear();
		GetStopAreasGPSAsync stopGPS = new GetStopAreasGPSAsync(this, this.myCurrentLocation);
		stopGPS.execute("");
		return false;
	}

}
