package fr.coyot.notifbus;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import fr.coyot.notifbus.dao.UserPreferencesDAO;
import fr.coyot.notifbus.utils.Constants;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Initialize jumpToNextPeriod to 0 for all journeys for recalculate the next period 
		// with the new values that the user has entered
		UserPreferencesDAO dao = new UserPreferencesDAO(this);
		dao.open();
		dao.initJourney();
		dao.close();

		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		// TODO Auto-generated method stub
		loadHeadersFromResource(R.xml.pref_headers, target);
		//Log.d("BUILD_HEADER", "toto");
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	public static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = 
			new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String summary = "";
			String key = preference.getKey();
			if (key.equals(Constants.KEY_PREF_MORNING_DAYS)
				|| key.equals(Constants.KEY_PREF_EVENING_DAYS)){
				HashSet<String> daysSelected = (HashSet<String>)value;
				summary = buildSummaryDaysNotif(daysSelected);
			} else if (key.equals(Constants.KEY_PREF_GEOTAG_DISTANCE)){
				String distance = (String)value;
				if (distance.length() > 5){
					summary = distance.substring(0, 1) + "," + distance.substring(1, 4) + " km"; 
				} else {
					summary = distance.substring(0, 3) + " m";
				}
			} else {
				String stringValue = value.toString();
				summary = buildSummaryPrefNotif(Integer.parseInt(stringValue));
			}
			preference.setSummary(summary);
			return true;
		}
	};
	
	private static String buildSummaryDaysNotif (HashSet<String> daysSelected){
		StringBuilder summary = new StringBuilder();
		Boolean[] activeDays = {false, false, false, false, false, false, false};
		Iterator<String> it = daysSelected.iterator();
		while (it.hasNext()){
			String currentDay = (String) it.next();
			activeDays[Integer.parseInt(currentDay)-1] = true;
		}
		for (int i=0; i<7; i++){
			if (activeDays[i]){
				summary.append(Constants.PREF_DAYS_NAME[i]);
				summary.append(" ");
			}
		}
		return summary.toString();
	}
	
	private static String buildSummaryPrefNotif (Integer minutes){
		String summary = "";
		if (minutes == -2){
			// This preferences is not defined
			summary = "Non définie";
		}else {
			Integer hours = minutes/60;
			if (hours == 0){
				summary = String.valueOf(minutes) + "min";
			} else {
				summary = String.valueOf(hours) + "h";
				Integer nbMinutes = minutes-(hours*60);
				if (nbMinutes != 0) {
					summary = summary + String.valueOf(nbMinutes);
				}
			}
		}
		
		return summary;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.activity_setings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.validateSetings){
			finish();
		}
		return true;
	}

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	public static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		if (preference.getKey().equals(Constants.KEY_PREF_MORNING_DAYS)
				|| preference.getKey().equals(Constants.KEY_PREF_EVENING_DAYS)){
			HashSet<String> defValues = new HashSet<String>(); 
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getStringSet(preference.getKey(),
							defValues));
		} else {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString(preference.getKey(),
							""));
		}
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notifications_morning);
			
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_MORNING_INTERVAL));
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_MORNING_HOUR));
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_MORNING_DURATION));
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_MORNING_DAYS));
		}
	}
	
	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationEveningPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notifications_evening);
			
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_EVENING_INTERVAL));
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_EVENING_HOUR));
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_EVENING_DURATION));
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_EVENING_DAYS));
		}
	}
	
	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeoTagFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_geo_tag);
			
			bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_GEOTAG_DISTANCE));
		}
	}

}
