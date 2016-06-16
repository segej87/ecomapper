package mapgui;

import dataproc.Filtering;
import processing.core.PApplet;

public class FilterDropdown extends Dropdown {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Filtering filter;
	private String filterVal = "";
	private FilterButton button;

	public FilterDropdown(PApplet p) {
		super(p);
	}
	
	public FilterDropdown(PApplet p, Filtering f, String[] i, FilterButton b, float x, float y, float w, float h) {
		super(p);
		setItems(i);
		setButX(x);
		setButY(y);
		setButWidth(w);
		setButHeight(h);
		setType(b.getType() + "DD");
		this.filter= f;
		this.button = b;
	}
	
	@Override
	protected boolean inSubItem(float x, float y){
		for (int i = 0; i < items.length; i++){
			if (x > getButX() + getButWidth() && x < getButX() + getButWidth() + getMaxTextWidth(items, fontSize) + 15 && y > getButY() + 10 + (10 + fontSize) * i && y < getButY() + 10 + (10 + fontSize) * i + fontSize){
				this.filterVal = items[i];
				return true;
			}
		}
		return false;
	}

	@Override
	public void doTypeAction() {
		if (!getActive()){
			setActive(true);
		} else {
			filter.setTarget(button.getType(), filterVal);
			if (getType().equals("GeographyDD")){
				filter.setGeoActive(true);
			}
			button.setTarget();
			setActive(false);
		}
	}
}
