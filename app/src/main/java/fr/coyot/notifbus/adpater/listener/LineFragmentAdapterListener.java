package fr.coyot.notifbus.adpater.listener;

import fr.coyot.notifbus.model.Line;

public interface LineFragmentAdapterListener {

	/**
	 * Interface for listener on the name of the bus line
	 */
	public void onClickLine(Line item, int position);
}
