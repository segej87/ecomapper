package markers;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class NoteMarker extends PointMarker{
	
	PImage img;
	
	public NoteMarker(PointFeature feature, int c) {
		super(feature.getLocation(), feature.getProperties());
		super.col = c;
	}
	
	public NoteMarker(PointFeature feature, int c, PImage p) {
		super(feature.getLocation(), feature.getProperties());
		super.col = c;
		this.img = p;
	}
	
	public NoteMarker(Location loc, java.util.HashMap<java.lang.String, java.lang.Object> props, int c){
		super(loc, props);
		super.col = c;
	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
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
	}

}
