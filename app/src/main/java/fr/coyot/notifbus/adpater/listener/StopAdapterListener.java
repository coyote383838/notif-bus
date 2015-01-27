package fr.coyot.notifbus.adpater.listener;

import fr.coyot.notifbus.model.PhysicalStop;

public interface StopAdapterListener {

	/**
	 * Interface for listener on the name of the current stop
	 */
	public void onClickStop(PhysicalStop item, int position);
}
