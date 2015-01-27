package fr.coyot.notifbus.utils;


public class Constants {
	
	public final static String VERSION = "2.2";
	
	/**
	 * General constants
	 */
	public final static String EXPIRATION_LINES = "lines";
	public final static String EXPIRATION_MESSAGE = "messages";
	public final static String EXPIRATION_STOP = "stops";
	public final static String NOTIFICATION_TAG_NAME = "NotifBus";
	
	/**
	 * Alert Box Title
	 */
	public final static String LOCATION_NOT_ENABLE_TITLE = "Localisation non activée";
	public final static String LOCATION_NOT_ENABLE_CONTENT = "Veuillez activer la localisation afin de profiter de cette fonctionnalitée";
	
	/**
	 * Scheduled constants
	 */
	public final static int MORNING = 1;
	public final static int EVENING = 2;
	public final static String INTENT_IS_MORNING = "isMorning";
			
	/**
	 * GeoTag constants
	 */
	public final static Double DEGREE_LATITUDE = 111102.0;
	public final static Double DEGREE_LONGITUDE = 80877.0;
	
	/**
	 * URL constants
	 */
	public final static String URL_ROOT = "http://api.tisseo.fr/v1/";
	public final static String URL_SUFFIX_LIST_LINES = "lines.json";
	public final static String URL_SUFFIX_STOP = "stop_points.json";
	public final static String URL_SUFFIX_STOP_GPS = "stop_points.json";
	public final static String URL_SUFFIX_STOP_AREA = "stop_areas.json";
	public final static String URL_SUFFIX_NEXT_DEPARTURE = "stops_schedules.json";
	public final static String URL_SUFFIX_MESSAGES = "messages.json";
	public final static String URL_DISPLAY_IMPORTANT_MESSAGES = "displayImportantOnly=1";
	public final static String URL_DISPLAY_TERMINUS = "displayTerminus=1";
	public final static String URL_DISPLAY_DESTINATIONS = "displayDestinations=1";
	public final static String URL_DISPLAY_LINES = "displayLines=1";
	public final static String URL_DISPLAY_COORDXY = "displayCoordXY=1";
	public final static String URL_TERMINUS = "terminusId=";
	public final static String URL_BBOX = "bbox=";
	public final static String URL_LINE = "lineId=";
	public final static String URL_KEY_API = "key=af15266206f22119413d5b655447a5382";
	public final static String URL_STOP = "stopPointId=";
	public final static String ET = "&";
	public final static String POINT_INTERROGATION = "?";
	public final static String BUS = "bus";
	public final static String TRAM = "tram";
	public final static String IS_MORNING = "isMorning";
	public final static int NB_MIN_IN_1_DAY = 1440;
	public final static int NB_MIN_4H30 = 270;
	public final static int NB_MIN_13H = 780;
	public final static int NB_MIN_21H15 = 1275;
	public final static int NB_MIN_22H = 1320;
	
	public static final String[] LIST_MOMENT = {"le matin", "le soir"};
	
	public final static String LIST_LINES = "listLines";
	
	public final static String ABOUT = "A propos";
	
	/**
	 * JSON constants
	 */
	public static final String JSON_LINES = "lines";
	public static final String JSON_LINE = "line";
	public static final String JSON_TRASNPORT_MODE = "transportMode";
	public static final String JSON_TRASNPORT_MODE_NAME = "name";
	public static final String JSON_ID = "id";
	public static final String JSON_SHORT_NAME = "shortName";
	public static final String JSON_LINE_COLOR = "color";
	public static final String JSON_NAME = "name";
	public static final String JSON_BUS = "bus";
	public static final String JSON_TRAM = "tramway";
	public static final String JSON_METRO = "métro";
	public static final String JSON_STOP_AREAS = "stopAreas";
	public static final String JSON_STOP_AREA = "stopArea";
	public static final String JSON_STOP = "stop";
	public static final String JSON_PHYSICAL_STOPS = "physicalStops";
	public static final String JSON_PHYSICAL_STOP = "physicalStop";
	public static final String JSON_OPERATOR_CODES = "operatorCodes";
	public static final String JSON_OPERATOR_CODE = "operatorCode";
	public static final String JSON_DESTINATIONS = "destinations";
	public static final String JSON_DESTINATION = "destination";
	public static final String JSON_TERMINUS = "terminus";
	public static final String JSON_EXPIRATION_DATE = "expirationDate";
	public static final String JSON_DEPARTURES = "departures";
	public static final String JSON_DEPARTURE = "departure";
	public static final String JSON_DATE_TIME = "dateTime";
	public static final String JSON_REAL_TIME = "realTime";
	public static final String JSON_MESSAGES = "messages";
	public static final String JSON_MESSAGE = "message";
	public static final String JSON_MESSAGE_TITLE = "title";
	public static final String JSON_MESSAGE_CONTENT = "content";
	public static final String JSON_MESSAGE_IMPORTANCE = "importanceLevel";
	public static final String JSON_MESSAGE_TYPE = "type";
	
	/**
	 * Database constants
	 */
	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "notifBus";
	public static final String TABLE_NAME_JOURNEYS = "journeys";
	public static final String JOURNEY_ID = "JourneyId";
	public static final String LINE_ID = "lineId";
	public static final String LINE_NAME = "lineName";
	public static final String LINE_COLOR = "lineColor";
	public static final String STOP_ID = "stopId";
	public static final String JOURNEY_DESCRIPTION = "journeyDescription";
	public static final String HAS_BEEN_STOPPED = "hasBeenStoppped";
	
	public static final String TABLE_NAME_MESSAGES = "messages";
	public static final String DATABASE_MESSAGE_ID = "id";
	public static final String DATABASE_MESSAGE_TITLE = "title";
	public static final String DATABASE_MESSAGE_CONTENT = "content";
	public static final String DATABASE_MESSAGE_TYPE = "type";
	public static final String DATABASE_MESSAGE_IMPORTANCE = "importance";
	public static final String DATABASE_MESSAGE_ALREADY_DISPLAY = "alreadyDisplay";
	public static final String DATABASE_MESSAGE_EXPIRATION_DATE = "expirationDate";
	
	/**
	 * Key preferences constants
	 */
	//public static final String KEY_PREF_INTERVAL = "pref_key_interval";
	
	public static final String KEY_PREF_MORNING_HOUR = "pref_key_morning_hour";
	public static final String KEY_PREF_MORNING_DURATION = "pref_key_morning_duration";
	public static final String KEY_PREF_MORNING_DAYS = "pref_key_day_morning";
	public static final String KEY_PREF_MORNING_MAX_DAY = "pref_key_max_days_morning";
	public static final String KEY_PREF_MORNING_ACTIVATE = "pref_key_activate_morning";
	public static final String KEY_PREF_MORNING_INTERVAL = "pref_key_interval";
	
	public static final String KEY_PREF_EVENING_HOUR = "pref_key_evening_hour";
	public static final String KEY_PREF_EVENING_DURATION = "pref_key_evening_duration";
	public static final String KEY_PREF_EVENING_DAYS = "pref_key_day_evening";
	public static final String KEY_PREF_EVENING_MAX_DAY = "pref_key_max_days_evening";
	public static final String KEY_PREF_EVENING_ACTIVATE = "pref_key_activate_evening";
	public static final String KEY_PREF_EVENING_INTERVAL = "pref_key_interval";
	
	public static final String KEY_PREF_GEOTAG_DISTANCE = "pref_key_distance_geotag";
	
	public static final String[] PREF_DAYS_NAME = {"dimanche","lundi", "mardi", 
		"mercredi", "jeudi", "vendredi", "samedi"};
	
}


