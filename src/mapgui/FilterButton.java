package mapgui;

import dataproc.Filtering;
import processing.core.PApplet;

public class FilterButton extends Button {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Filtering filter;
	private Object target;

	public FilterButton(PApplet p) {
		super(p);
	}
	
	public FilterButton(PApplet p, Filtering f, String ty, float x, float y, float w, float h) {
		super(p);
		setButX(x);
		setButY(y);
		setButWidth(w);
		setButHeight(h);
		this.filter = f;
		setType(ty);
		setTarget();
	}

	protected void setTarget() {
		if (getType() == "Access level"){
			this.target = filter.getALFilter();
		} else if (getType() == "Geography"){
			this.target = filter.getGeoFilterType();
		} else if (getType() == "Datatype"){
			this.target = filter.getDTFilter();
		} else if (getType() == "Measured item"){
			this.target = filter.getSpecFilter();
		}
	}
	
	public Object getTarget() {
		return this.target;
	}

	@Override
	public void drawButton(PApplet p) {
		if ((target.equals("") || target == null) && !inButton(p.mouseX, p.mouseY)){
			p.fill(25, 25, 50);
			p.stroke(25, 25, 50);
		} else if ((!target.equals("") && target != null) || inButton(p.mouseX, p.mouseY)) {
			p.fill(100, 100, 125);
			p.stroke(100, 100, 125);
		}
		p.rect(getButX(), getButY(), getButWidth(), getButHeight());
		
		p.fill(255,255,255);
		p.textSize(14);
		p.textAlign(LEFT, CENTER);
		String filterText = "all";
		if (target != ""){
			filterText = target.toString();
		}
		p.text(getType() + ": " + filterText, getButX() + 10, getButY() + getButHeight()/2);
	}

	@Override
	public void doAction() {
		if (target == ""){
			
		} else {
			filter.setTarget(getType(), "");
			setTarget();
		}
	}

}
