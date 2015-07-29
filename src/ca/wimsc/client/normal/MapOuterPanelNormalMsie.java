package ca.wimsc.client.normal;

public class MapOuterPanelNormalMsie extends MapOuterPanelNormal {

	@Override
	protected boolean allowSystemMapOverlay() {
		// MSIE doesn't support the overlay
		return false;
	}


}
