package popups;

import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;

public class MapNote extends MapPopups {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String subtype = "note";
	private MapPopups prevInt;
	
	public MapNote(UnfoldingMap m, PApplet p, MapPopups i) {
		super(m, p, i.getMarker());
		this.prevInt = i;
		setQuadrant(i.getQuadrant());
		setIntWidth(i.getIntWidth());
		setIntX(i.getIntX());
		setIntHeight(7 * fontSize);
		calcIntY();
		setColor(p.color(225,225,225));
	}
	
	@Override
	public String getSubType(){
		return this.subtype;
	}

	@Override
	public void drawPop() {
		p.pushStyle();
		p.fill(getColor());
		p.stroke(getColor());
		p.rect(getIntX(), getIntY(), getIntWidth(), getIntHeight());
		writeNote();
		p.popStyle();
	}
	
	public void calcIntY(){
		float prevIntHeight = prevInt.getIntHeight();
		float noteY;
		
		if (getQuadrant()[1]){
			noteY = prevInt.getIntY() - getIntHeight();
		} else {
			noteY = prevInt.getIntY() + prevIntHeight;
		}
		
		setIntY(noteY);
	}
	
	public float getFontSize(){
		return this.fontSize;
	}
	
	private void writeNote() {
		p.textSize(this.fontSize);
		float textLine = getIntX() + 5;
		p.fill(color(36,49,168));
		p.textAlign(LEFT,TOP);
		p.text("Note: " + getProp("text"), textLine, getIntY() + 10, getIntWidth() - 10, getIntHeight());
		p.textSize(this.fontSize);
	}
}
