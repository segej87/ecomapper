package popups;

import org.json.JSONArray;
import org.json.JSONException;

import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;

public class MapTags extends MapPopups{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String subtype = "tags";
	private MapPopups prevInt;

	public MapTags(UnfoldingMap m, PApplet p, MapPopups i){
		super(m, p, i.getMarker());
		this.prevInt = i;
		setQuadrant(i.getQuadrant());
		setIntWidth(i.getIntWidth());
		setIntHeight(fontSize * 2);
		setIntX(i.getIntX());
		calcIntY();
		setColor(p.color(255,255,255));
	}
	
	public void writeTags(){
		p.textSize(fontSize);
		float textLine = getIntX() + 5;
		p.fill(color(230, 230, 230));
		p.stroke(color(150, 150, 150));
		p.strokeWeight(1);
		float xBump = (float) (textLine + p.textWidth("Tags:  ") - 1.7);
		String[] tagArray = getStringArrayFromJSONArray((JSONArray) getProp("tags"));
		for (String tag : tagArray){
			p.rect(xBump, (float) (getIntY() + 0.4 * fontSize), (float) (p.textWidth(tag) + 3.5), (float) (1.25 * fontSize), 2);
			xBump = (float) (xBump + p.textWidth(tag + "  ") - 0.2);
		}
		
		p.stroke(255);
		p.fill(0);
		p.text("Tags:  " + String.join("  ", tagArray), textLine, (float) (getIntY() + 1.3 * fontSize));
	}
	
	@Override
	public String getSubType(){
		return this.subtype;
	}
	
	@Override
	public void drawPop(){
		p.pushStyle();
		p.fill(getColor());
		p.stroke(getColor());
		p.rect(getIntX(), getIntY(), getIntWidth(), getIntHeight());
		writeTags();
		p.popStyle();
	}
	
	public void calcIntY(){
		float prevIntHeight = prevInt.getIntHeight();
		float tagY;
		
		if (getQuadrant()[1]){
			tagY = prevInt.getIntY() - getIntHeight();
		} else {
			tagY = prevInt.getIntY() + prevIntHeight;
		}
		
		setIntY(tagY);
	}
	
	public float getFontSize(){
		return this.fontSize;
	}
	
	//TODO: add calcIntHeight based on tags length and width from mapKey
	// with tags wrapping
}
