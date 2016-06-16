package mapgui;

import dataproc.Filtering;
import processing.core.PApplet;

public class DatePanel extends Panel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String panText = "2016";
	String dateForm = "yyyy";
	float fontSize = 13;
	int posInd = 0;
	Filtering filter;

	public DatePanel(PApplet p, float x, float y, float w, float h) {
		super(p);
		setPanX(x);
		setPanY(y);
		setPanWidth(w);
		setPanHeight(h);
	}
	
	public DatePanel(PApplet p, Filtering f, String startorend, String df, float x, float y) {
		super(p);
		setPanX(x);
		setPanY(y);
		p.textSize(fontSize);
		setPanWidth(p.textWidth(f.getDateFilterString(posInd,df)) + 4);
		setPanHeight((float) (fontSize * 1.5));
		if (startorend == "START"){
			posInd = 0;
		} else if (startorend == "END"){
			posInd = 1;
		}
		this.filter = f;
		this.dateForm = df;
	}

	@Override
	public void drawPanel(PApplet p) {
		p.fill(color(50, 50, 75));
		p.strokeWeight(0);
		p.rect(getPanX(), getPanY(), getPanWidth(), getPanHeight());
		
		p.textSize(fontSize);
		p.fill(255);
		p.textAlign(LEFT, TOP);
		p.text(filter.getDateFilterString(posInd,dateForm), getPanX() + 2, getPanY() + 2);
		
		if (dateForm == "yyyy"){
			String titleString = "Start: ";
			if (posInd == 1){
				titleString = "End: ";
			}
			p.text(titleString, getPanX() - p.textWidth(titleString), getPanY() + 2);
		}
		
		p.textSize(10);
		if (posInd == 1){
			p.text(dateForm, getPanX() + 2, getPanY() + getPanHeight() + 1);
		}
	}
	
	
	public void setText(String t){
		this.panText = t;
	}
	
	public String getText(){
		return this.panText;
	}

	public void setPanText(String s) {
		this.panText = s;
	}
	
	public String getDateForm(){
		return this.dateForm;
	}
	
	public int getPosInd(){
		return this.posInd;
	}
}
