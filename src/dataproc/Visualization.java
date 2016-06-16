package dataproc;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import markers.MeasMarker;
import markers.NoteMarker;
import markers.PhotoMarker;
import markers.PointMarker;
import processing.core.PApplet;

public class Visualization extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	PApplet p;
	
	public Visualization(PApplet p){
		this.p = p;
	}
	
	public List<Float> getAllVals(List<Marker> marks){
		//TODO: incorporate unit conversion
		List<Float> allVals = new ArrayList<Float>();
		for (Marker m : marks){
			if (!m.isHidden()){allVals.add(Float.valueOf(m.getProperty("value").toString()));}
		}
		return allVals;
	}
	
	public void colorByValue(MeasMarker m, List<Float> vals){
		//TODO: add more logic to color marker based on meas value
		float val = Float.valueOf(m.getProperty("value").toString());
		float vMean = Stats.getMean(vals);
		float vSig = Stats.getSig(vals, vMean);
		float mRes = (val - vMean)/vSig;
		int colorLevel = (int) map(mRes, -2, 2, 10, 255);
		m.setCol(color(colorLevel, 0, 255 - colorLevel));
	}
	
	public static void colorAndHide(Marker m, int strokecolor, int fillcolor, boolean hidden){
		if (strokecolor != -1){
		m.setStrokeColor(strokecolor);
		}
		if (fillcolor != -1){
			m.setColor(fillcolor);	
		}
		m.setHidden(hidden);
	}
	
	public void assignMarker(Feature feat, List<PointMarker> pointMarkers){
		if (feat.getProperty("datatype").equals("photo")){
			pointMarkers.add(new PhotoMarker((PointFeature) feat,p.color(194, 34, 39), p.loadImage("PhotoImage.png", "png")));
		} else if (feat.getProperty("datatype").equals("note")){
			pointMarkers.add(new NoteMarker((PointFeature) feat, p.color(46, 49, 146), p.loadImage("NoteImage.png", "png")));
		} else if (feat.getProperty("datatype").equals("meas")){
			pointMarkers.add(new MeasMarker((PointFeature) feat, p.color(0, 104, 56), p.loadImage("MeasImage.png", "png")));
		}
	}
	
	public void assignMarkers(List<Feature> personFeats, List<PointMarker> pointMarkers){
		for (Feature feat : personFeats){
			if (feat.getProperty("datatype").equals("photo")){
				pointMarkers.add(new PhotoMarker((PointFeature) feat,p.color(194, 34, 39), p.loadImage("PhotoImage.png", "png")));
			} else if (feat.getProperty("datatype").equals("note")){
				pointMarkers.add(new NoteMarker((PointFeature) feat, p.color(46, 49, 146), p.loadImage("NoteImage.png", "png")));
			} else if (feat.getProperty("datatype").equals("meas")){
				pointMarkers.add(new MeasMarker((PointFeature) feat, p.color(0, 104, 56), p.loadImage("MeasImage.png", "png")));
			}
		}
	}
}
