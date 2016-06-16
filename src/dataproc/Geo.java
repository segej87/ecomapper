package dataproc;

import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;

public class Geo {
	
	static UnfoldingMap m;
	
	public Geo(UnfoldingMap m){
		Geo.m = m;
	}

	public static boolean isInGeoFeat(PointFeature data, Marker geoFeat, String type) {
		Location checkLoc = data.getLocation();

		// check if inside country represented by MultiMarker
		if(geoFeat.getClass() == MultiMarker.class) {
			for(Marker marker : ((MultiMarker)geoFeat).getMarkers()) {
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					data.addProperty(type, geoFeat.getProperty("name"));
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)geoFeat).isInsideByLocation(checkLoc)) {
			data.addProperty(type, geoFeat.getProperty("name"));
			return true;
		}
		
		//if the feature was not in a geoMarker, add the property with value "none"
		if (!data.getProperties().containsKey(type)){data.addProperty(type, "none");}
		return false;
	}
	
	public static void isinGeoFeats(List<Feature> personFeats, List<Marker> geoMarks, String type){
		for (Feature data : personFeats){
			Location checkLoc = ((PointFeature)data).getLocation();
			
			for (Marker geoFeat : geoMarks){

				// check if inside country represented by MultiMarker
				if(geoFeat.getClass() == MultiMarker.class) {
					for(Marker marker : ((MultiMarker)geoFeat).getMarkers()) {
						if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
							data.addProperty(type, geoFeat.getProperty("name"));
						}
					}
				}
					
				// check if inside country represented by SimplePolygonMarker
				else if(((AbstractShapeMarker)geoFeat).isInsideByLocation(checkLoc)) {
					data.addProperty(type, geoFeat.getProperty("name"));
				}
				
				//if the feature was not in a geoMarker, add the property with value "none"
				if (!data.getProperties().containsKey(type)){data.addProperty(type, "none");}
			}
		}
	}
	
	public String geoHitCheck(float[] checkLoc, Marker geoMark) {
		String hitGeo;
		
		// check if inside country represented by MultiMarker
		if(geoMark.getClass() == MultiMarker.class) {
			for(Marker marker : ((MultiMarker)geoMark).getMarkers()) {
				if(((AbstractShapeMarker)marker).isInside(m, checkLoc[0], checkLoc[1])) {
					hitGeo = geoMark.getProperty("name").toString();
					return hitGeo;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)geoMark).isInside(m, checkLoc[0], checkLoc[1])) {
			hitGeo = geoMark.getProperty("name").toString();
			return hitGeo;
		}
		
		return "";
	}
	
}
