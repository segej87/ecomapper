package markers;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class PhotoMarker extends PointMarker {
	
	PImage img;
	
	public PhotoMarker(PointFeature feature, int c) {
		super(feature.getLocation(), feature.getProperties());
		super.col = c;
	}
	
	public PhotoMarker(PointFeature feature, int c, PImage img) {
		super(feature.getLocation(), feature.getProperties());
		super.col = c;
		this.img = img;
	}
	
	public PhotoMarker(Location loc, java.util.HashMap<java.lang.String, java.lang.Object> props, int c){
		super(loc, props);
		super.col = c;
	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();
		
		if (img != null){
			img.resize(15, 0);
			pg.imageMode(PConstants.CENTER);
			pg.image(img, x, y);
		} else {
			pg.fill(col);
			pg.stroke(col);
			pg.rectMode(PConstants.CENTER);
			pg.rect(x, y, 10, 7);
			
			pg.fill(0);
			pg.ellipse(x, y, 5, 5);
			
			pg.rectMode(PConstants.CORNER);
			pg.stroke(0);
			pg.rect(x - 5, y - 6,  4,  2);
		}
		
		pg.popStyle();
	}


}
