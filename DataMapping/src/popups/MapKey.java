package popups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PApplet;

public class MapKey extends MapPopups {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String subtype = "Key";
	private float clickX;
	private float clickY;
	private boolean multMarks = false;
	private HashMap<String, String> info = new HashMap<String, String>();
	
	public MapKey(PApplet p, UnfoldingMap m, float x, float y, Marker mark){
		super(m, p, mark);
		this.clickX = x;
		this.clickY = y;
		info.put("Type", "datatype");
		info.put("Date", "datetime");
		info.put("Name", "name");
		info.put("Country", "country");
		calcQuadrant(clickX, clickY);
		setIntWidth();
		setIntHeight(info.size());
		calcIntY();
		calcIntX();
		setColor(((markers.PointMarker) mark).col);
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
		writeText();
		p.popStyle();
	}
	
	public void setIntWidth(){
		p.textSize(fontSize);
		
		//TODO: After tag-wrapping functionality added, remove tags from this calculation
		String[] tagArray = getStringArrayFromJSONArray((JSONArray) getProp("tags"));
		String[] str = new String[info.size() + 1];
		str[0] = "Tags: " + String.join("  ", tagArray);
		int counter = 1;
		for (String key : info.keySet()){
			str[counter] = key + ": " + getProp(info.get(key));
			counter = counter + 1;
		}
		
		List<Float> stringWidth = new ArrayList<Float>();
		for (String s : str){
			stringWidth.add(p.textWidth(s));
		}
		
		float max = stringWidth.get(0);
		for (float w : stringWidth){
			if(w > max) max = w;
		}
		
		setIntWidth((int) max + 15);
	}
	
	public void setIntHeight(int numstrings){
		setIntHeight(fontSize * (2 + numstrings));
	}
	
	public void calcIntX(){
		float keyX;
		if (getQuadrant()[0]){
			keyX = clickX - getIntWidth();
		} else {
			keyX = clickX;
		}
		setIntX(keyX);
	}
	
	public void calcIntY(){
		float keyY;
		
		if (getQuadrant()[1]){
			keyY = clickY - getIntHeight();
		} else {
			keyY = clickY;
		}
		
		setIntY(keyY);
	}
	
	private void writeText(){
		p.textSize(fontSize);
		
		//Set alignment for text
		float textLine = getIntX() + 5;
		
		//write header info
		p.fill(255);
		p.textAlign(LEFT,TOP);
		
		String[] keyArray = info.keySet().toArray(new String[0]);
		for (int i = 0; i < keyArray.length; i++){
			p.text(keyArray[i] + ": " + getProp(info.get(keyArray[i])), textLine, (float) (getIntY() + fontSize * (0.5 + (1.25 * i))));
		}
	}
	
	public void addLeftSwipe(){
		p.pushStyle();
		p.fill(255, 255, 255);
		p.stroke(255, 255, 255);
		p.rect(getIntX() - 10, getIntY() + 5, 10, 20);
		
		p.stroke(75, 75, 75);
		p.strokeWeight(3);
		p.line(getIntX() - 4, getIntY() + 10, getIntX() - 8, getIntY() + 16);
		p.line(getIntX() - 4, getIntY() + 20, getIntX() - 8, getIntY() + 14);
		p.popStyle();
	}
	
	public void addRightSwipe(){
		p.pushStyle();
		p.fill(255, 255, 255);
		p.stroke(255, 255, 255);
		p.rect(getIntX() + getIntWidth(), getIntY() + 5, 10, 20);
		
		p.stroke(75, 75, 75);
		p.strokeWeight(3);
		p.line(getIntX() + getIntWidth() + 4, getIntY() + 10, getIntX() + getIntWidth() + 8, getIntY() + 16);
		p.line(getIntX() + getIntWidth() + 4, getIntY() + 20, getIntX() + getIntWidth() + 8, getIntY() + 14);
		p.popStyle();
	}
	
	public boolean inLeftSwipe(int s, float x, float y){
		return (s > 0 && x > getIntX() - 10 && x < getIntX() && y > getIntY() + 5 && y < getIntY() + 25);
	}
	
	public boolean inRightSwipe(int s, List<Marker> h, float x, float y){
		return s < h.size() - 1 && x > getIntX() + getIntWidth() && x < getIntX() + getIntWidth() + 10 && y > getIntY() + 5 && y < getIntY() + 25;
	}
	
	//getters and setters
	public boolean getMultMarks(){
		return this.multMarks;
	}
	
	public void setMultMarks(boolean mult){
		this.multMarks = mult;
	}

	public float getClickX(){
		return clickX;
	}
	
	public float getClickY(){
		return clickY;
	}
	
	public float getFontSize(){
		return this.fontSize;
	}
}
