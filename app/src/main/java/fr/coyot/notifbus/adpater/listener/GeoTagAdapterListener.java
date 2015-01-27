package fr.coyot.notifbus.adpater.listener;

import fr.coyot.notifbus.model.PhysicalStop;

public interface GeoTagAdapterListener {

	/**
	 * Interface for listener on the name of the bus line
	 */
	public void onClickPhysicalStop(PhysicalStop item, int position);
	
	//public void onLongClickJourney(Journey item, int position);
	//public boolean onItemLongClick(AdapterView parent, View v, int position, long id);
}
