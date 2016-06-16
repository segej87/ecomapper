package mapgui;

import java.util.Calendar;

import dataproc.Filtering;
import processing.core.PApplet;

public class ToggleButton extends Button {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Filtering filter;
	DatePanel assocPan;

	public ToggleButton(PApplet p, Filtering f, DatePanel ap, float x, float y, float w, float h) {
		super(p);
		this.filter = f;
		setButX(x);
		setButY(y);
		setButWidth(w);
		setButHeight(h);
		this.assocPan = ap;
	}

	@Override
	public void drawButton(PApplet p) {
		//p.fill(color(50, 50, 75));
		p.fill(color(230, 230, 230));
		p.rect(getButX(), getButY(), getButWidth(), getButHeight());
		
		p.stroke(color(125, 125, 150));
		p.strokeWeight((float)0.5);
		float butvHalf = getButY() + getButHeight()/2;
		float buthHalf = getButX() + getButWidth()/2;
		float butHalfHeight = getButHeight()/2;
//		p.line(getButX(), butvHalf, getButX() + getButWidth(), butvHalf);
		p.line(getButX(), getButY(), getButX(), getButY() + getButHeight());

		p.strokeWeight(1);
		
		//draw up arrow
		p.line(buthHalf - getButWidth()/3, butvHalf - butHalfHeight/3, buthHalf, butvHalf - butHalfHeight*2/3);
		p.line(buthHalf + getButWidth()/3, butvHalf - butHalfHeight/3, buthHalf, butvHalf - butHalfHeight*2/3);
		
		//draw down arrow
		p.line(buthHalf - getButWidth()/3, butvHalf + butHalfHeight/3, buthHalf, butvHalf + butHalfHeight*2/3);
		p.line(buthHalf + getButWidth()/3, butvHalf + butHalfHeight/3, buthHalf, butvHalf + butHalfHeight*2/3);
	}

	@Override
	public void doAction() {
		int inc = incFromHalf();
		
		Calendar c = Calendar.getInstance();
		if (assocPan.getPosInd() == 0){
			c.setTime(filter.getStartDateFilter());
		} else if (assocPan.getPosInd() == 1){
			c.setTime(filter.getEndDateFilter());
		}
		if (assocPan.getDateForm() == "yyyy"){
			c.add(Calendar.YEAR, inc);
		} else if (assocPan.getDateForm() == "MM"){
			c.add(Calendar.MONTH, inc);
		} else if (assocPan.getDateForm() == "dd") {
			c.add(Calendar.DATE, inc);
		} else if (assocPan.getDateForm() == "HH"){
			c.add(Calendar.HOUR, inc);
		} else if (assocPan.getDateForm() == "mm"){
			c.add(Calendar.MINUTE, inc);
		}
		if (assocPan.getPosInd() == 0){
			filter.setStartDateFilter(c.getTime());
		} else if (assocPan.getPosInd() == 1){
			filter.setEndDateFilter(c.getTime());
		}
	}

	private int incFromHalf() {
		float butTop = getButY() + getButHeight();
		float butHalf = getButY() + getButHeight()/2;
		if (p.mouseY <  butHalf && p.mouseY > getButY()){return 1;}
		if (p.mouseY >  butHalf && p.mouseY < butTop){return -1;}
		return 0;
	}

}
