package popups;

import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;

public class MapMeas extends MapPopups {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String subtype = "meas";
	private MapPopups prevInt;
	public float fontSize = 20;

	public MapMeas(UnfoldingMap m, PApplet p, MapPopups i) {
		super(m, p, i.getMarker());
		this.prevInt = i;
		setQuadrant(i.getQuadrant());
		setIntWidth(i.getIntWidth());
		setIntX(i.getIntX());
		setIntHeight(4 * fontSize);
		calcIntY();
		setColor(p.color(225,225,225));
	}
	
	@Override
	public String getSubType() {
		return this.subtype;
	}

	@Override
	public void drawPop() {
		p.pushStyle();
		p.fill(getColor());
		p.stroke(getColor());
		p.rect(getIntX(), getIntY(), getIntWidth(), getIntHeight());
		addMeas();
		p.popStyle();
	}
	
	public void calcIntY(){
		float prevIntHeight = prevInt.getIntHeight();
		float measY;
		
		if (getQuadrant()[1]){
			measY = prevInt.getIntY() - getIntHeight();
		} else {
			measY = prevInt.getIntY() + prevIntHeight;
		}
		
		setIntY(measY);
	}
	
	private void addMeas() 
	{	
		p.fill(25, 25, 25);
		p.textSize(fontSize);
		float textLine = getIntX() + getIntWidth() - 10;
		float valWidth = p.textWidth(getProp("value") + " " + getProp("units"));
		float textY = getIntY() + getIntHeight()/3;
		if (getQuadrant()[1]){
			textY = getIntY() + getIntHeight()*2/3 + fontSize;
		}
		p.textAlign(RIGHT,BASELINE);
		p.text(getProp("value") + " " + getProp("units"), textLine, textY);
		
		p.textSize(fontSize - 3);
		p.textAlign(LEFT,BASELINE);
		p.text(getProp("species") + ": ", textLine - valWidth - p.textWidth(getProp("species") + ":  "), textY);
	}
	
	public float getFontSize(){
		return this.fontSize;
	}
}
