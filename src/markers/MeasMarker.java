package markers;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class MeasMarker extends PointMarker {
	
	private int initCol;
	private PImage img;
	private boolean cbi;
	
	public MeasMarker(PointFeature feature, int c) {
		super(feature.getLocation(), feature.getProperties());
		super.col = c;
		this.initCol = c;
	}
	
	public MeasMarker(PointFeature feature, int c, PImage p) {
		super(feature.getLocation(), feature.getProperties());
		super.col = c;
		this.initCol = c;
		this.img = p;
	}
	
	public MeasMarker(Location loc, java.util.HashMap<java.lang.String, java.lang.Object> props, int c){
		super(loc, props);
		super.col = c;
	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		if (!cbi){
			if (img != null){
				pg.pushStyle();
				img.resize(0, 15);
				pg.imageMode(PConstants.CENTER);
				pg.image(img, x, y);
				pg.popStyle();
			} else {
				pg.pushStyle();
				pg.fill(col);
				pg.stroke(col);
				pg.rectMode(PConstants.CENTER);
				pg.ellipse(x, y, 10, 10);
				pg.popStyle();
			}
		} else {
			pg.pushStyle();
			pg.fill(col);
			pg.stroke(col);
			pg.rectMode(PConstants.CENTER);
			pg.ellipse(x, y, 10, 10);
			pg.popStyle();
		}
	}
	
	public void resetCol(){
		super.col = initCol;
	}
	
	public void setCBI(boolean c){
		this.cbi = c;
	}
	
	public boolean getCBI(){
		return this.cbi;
	}
}
