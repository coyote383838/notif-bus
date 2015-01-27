package fr.coyot.notifbus.adpater.listener;

import fr.coyot.notifbus.model.Destination;

public interface DestinationAdapterListener {

	/**
	 * Interface for listener on the name of the current destination
	 */
	public void onClickDestination(Destination item, int position);
}
