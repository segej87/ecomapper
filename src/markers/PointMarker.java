package markers;

import processing.core.*;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;

public abstract class PointMarker extends SimplePointMarker {

	protected boolean clicked = false;
	public int col;
	
	public abstract void drawMarker(PGraphics pg, float x, float y);
	
	public PointMarker(Location location) {
		super(location);
	}
	
	public PointMarker(Location location, java.util.HashMap<java.lang.String, java.lang.Object> properties){
		super(location, properties);
	}
	
	public boolean getClicked(){
		return clicked;
	}
	
	public void setClicked(boolean state){
		clicked = state;
	}
	
	public void draw(PGraphics pg, float x, float y) {
		if (!hidden){
			drawMarker(pg, x, y);
		}
	}
	
	public void setCol(int c){
		this.col = c;
	}
	
	public int getCol(){
		return this.col;
	}
}
