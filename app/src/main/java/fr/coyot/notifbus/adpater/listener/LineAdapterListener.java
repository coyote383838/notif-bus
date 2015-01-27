package fr.coyot.notifbus.adpater.listener;

import fr.coyot.notifbus.model.Line;

public interface LineAdapterListener {

	/**
	 * Interface for listener on the name of the bus line
	 */
	public void onClickLine(Line item, int position);
}
