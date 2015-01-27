package fr.coyot.notifbus.adpater.listener;

import fr.coyot.notifbus.model.Journey;

public interface JourneyAdapterListener {

	/**
	 * Interface for listener on the name of the bus line
	 */
	public void onClickJourney(Journey item, int position);
	
	public void onLongClickJourney(Journey item, int position);
	//public boolean onItemLongClick(AdapterView parent, View v, int position, long id);
}
