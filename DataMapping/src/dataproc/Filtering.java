package dataproc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import de.fhpotsdam.unfolding.marker.Marker;
import markers.MeasMarker;
import markers.PointMarker;

public class Filtering {
	
	private String geoFilterType = "";
	private boolean geoActive = false;
	private String geoFilter = "";
	
	private List<String> tagFilter = null;
	private String dtFilter = "";
	private String specFilter = "";
	private String alFilter = "";
	private Date[] dateFilters = new Date[2];
	private List<String> availAL = null;
	private List<String> availGeo = null;
	private List<String> availDT = null;
	private List<String> availSpec = null;
	private List<String> availTags = null;
    
    public Filtering(){
    }
    
	public List<Marker> getFiltered(List<PointMarker> pointMarkers, Visualization vis){
		List<Marker> filteredMarkers = new ArrayList<Marker>();
		
		//get default date filter of past month
		//TODO: Figure out where to put this. Putting here will limit user
		//from ever adding data older than a month
//		Calendar calobj = Calendar.getInstance();
//		dateFilters[1] = calobj.getTime();
//		calobj.add(Calendar.MONTH, -1);
//		dateFilters[0] = calobj.getTime();
		
		//loop through markers to see if the properties contain the filter keywords
		for (Marker mark : pointMarkers){
			if (markerFilter(mark)){
				filteredMarkers.add(mark);
			}
		}
		
		if (allMeasTest(filteredMarkers)){
			for (Marker mark : filteredMarkers){
				if (mark instanceof MeasMarker){
					((MeasMarker)mark).setCBI(true);
					vis.colorByValue((MeasMarker) mark, vis.getAllVals(filteredMarkers));
				}
			}
		} else {
			for (Marker mark : filteredMarkers){
				if (mark instanceof MeasMarker){
					((MeasMarker)mark).setCBI(false);
				}
			}
		}
		
		setDateToRange(filteredMarkers);
		setAvails(filteredMarkers);
		return filteredMarkers;
	}
	
	@SuppressWarnings("serial")
	private void setAvails(List<Marker> filteredMarkers) {
		availAL = new ArrayList<String>();
		availGeo = new ArrayList<String>();
		availDT = new ArrayList<String>();
		availSpec = new ArrayList<String>();
		availTags = new ArrayList<String>();
		
		for (Marker m : filteredMarkers){
			JSONArray read = (JSONArray) m.getProperty("tags");
			String[] ts = new String[read.length()];
			for (int i = 0; i < read.length(); i++) {
				try {
					ts[i] = read.get(i).toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (!availDT.contains(m.getProperty("datatype").toString())){
				availDT.add(m.getProperty("datatype").toString());
			}
			if (m.getProperties().containsKey("species")){
				if (!availSpec.contains(m.getProperty("species".toString()))){
					availSpec.add(m.getProperty("species").toString());
				}
			}
			if (!availAL.contains(m.getProperty("access").toString())){
				availAL.add(m.getProperty("access").toString());
			}
			if (m.getProperties().containsKey("country") && !availGeo.contains("country")){
				if (!m.getProperty("country").toString().equals("none")){
					availGeo.add("country");
				}
			}
			if (m.getProperties().containsKey("state") && !availGeo.contains("state")){
				if (!m.getProperty("state").toString().equals("none")){
					availGeo.add("state");
				}
			}
			if (ts.length > 0){
				for (String t : ts){
					if (!availTags.contains(t)){
						availTags.add(t);
					}
				}
			}
		}
		for (List<String> o : new ArrayList<List<String>>() {{add(availAL);add(availGeo);add(availDT);add(availSpec);add(availTags);}}){
			Collections.sort(o);
		}
	}

	public void updateFiltered(List<Marker> pointMarkers, Visualization vis, boolean updateDate){
		if (updateDate){
			setDateToRange(pointMarkers);
		}
		List<Marker> filteredMarkers = new ArrayList<Marker>();
		
		//loop through markers to see if the properties contain the filter keywords
		for (Marker mark : pointMarkers){
			if (markerFilter(mark)){
				mark.setHidden(false);
				filteredMarkers.add(mark);
			} else {
				mark.setHidden(true);
			}
		}
		
		//TODO: update conditional logic so datatype has to equal meas and
		//all measurements need to be same species
		if (allMeasTest(filteredMarkers)){
			for (Marker mark : pointMarkers){
				if (mark instanceof MeasMarker){
					((MeasMarker)mark).setCBI(true);
					vis.colorByValue((MeasMarker) mark, vis.getAllVals(filteredMarkers));
				}
			}
		} else {
			for (Marker mark : pointMarkers){
				if (mark instanceof MeasMarker){
					((MeasMarker) mark).setCBI(false);
					((MeasMarker) mark).resetCol();
				}
			}
		}
		
		if (updateDate){
			setDateToRange(filteredMarkers);
		}
		setAvails(filteredMarkers);
	}
	
	private boolean allMeasTest(List<Marker> filteredMarkers){
		if (filteredMarkers.size() == 0){
			return false;
		}
		for (Marker mark : filteredMarkers){
			if (!(mark instanceof MeasMarker)){
				return false;
			}
		}
		List<String> fmspec = new ArrayList<String>();
		for (Marker m : filteredMarkers){
			fmspec.add(m.getStringProperty("species"));
		}
		Collections.sort(fmspec);
		if (!fmspec.get(0).equals(fmspec.get(fmspec.size()-1))){
			return false;
		}
		return true;
	}
	
	public boolean markerFilter(Marker mark){
		boolean alCheck = false;
		boolean geoCheck = false;
		boolean dtCheck = false;
		boolean specCheck = false;
		boolean tagCheck = false;
		boolean dateCheck = false;
		
		//TODO: make date filtering interactive
		String markDateString = mark.getProperty("datetime").toString();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date markDate = new Date();
		try {
			markDate = df.parse(markDateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//TODO: after standardizing filters as lists, change this to a loop
		if (alFilter != ""){
			if (mark.getProperty("access").toString().contains(alFilter)){
				alCheck = true;
			}
		} else {alCheck = true;}
		
		if (geoFilterType != ""){
			if (mark.getProperty(geoFilterType).toString().equals(geoFilter)){
				geoCheck = true;
			}
		} else {geoCheck = true;}
		
		if (dtFilter != ""){
			if (mark.getProperty("datatype").toString().equals(dtFilter)){
				dtCheck = true;
			}
		} else {dtCheck = true;}
		
		if (specFilter != ""){
			if (mark.getProperties().containsKey("species")){
				if (mark.getProperty("species").toString().equals(specFilter)){
					specCheck = true;
				}
			}
		} else specCheck = true;
		
		if (tagFilter != null){
			for (String t : tagFilter){
				if (mark.getProperty("tags").toString().contains(t)){
					tagCheck = true;
				}
			}
		} else {tagCheck = true;}
		
		if (dateFilters[0] != null && dateFilters[1] != null){
			if (markDate.compareTo(dateFilters[0]) >= 0 && markDate.compareTo(dateFilters[1]) <= 0){
				dateCheck = true;
			}
		} else if (dateFilters[0] != null){
			if (markDate.compareTo(dateFilters[0]) >= 0){
				dateCheck = true;
			}
		} else if (dateFilters[1] != null){
			if (markDate.compareTo(dateFilters[1]) <= 0){
				dateCheck = true;
			}
		} else dateCheck = true;
		
		if (alCheck && geoCheck && dtCheck && specCheck && tagCheck && dateCheck){
			return true;
		}
		return false;
	}
	
	public Marker findFilterGeo(float[] loc, List<Marker> geoMarkers, Geo geo){
		Marker result = null;
		for (Marker g : geoMarkers){
			if (geo.geoHitCheck(loc,  g) != ""){
				setGeoFilter(geo.geoHitCheck(loc,  g));
				result = g;
				break;
			}
		}
		return result;
	}
	
	public void setDateToRange(List<Marker> filteredMarkers){
		//set up variables to calculate min and max dates in features
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date minDate = new Date();
		Date maxDate = new Date();
		
		//update date filters
		if (filteredMarkers.size() > 0){
			String markDateString = filteredMarkers.get(0).getProperty("datetime").toString();
			try {
				minDate = df.parse(markDateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}		
			maxDate = minDate;
			
			for (Marker mark : filteredMarkers){
				Date markDate = new Date();
				markDateString = mark.getProperty("datetime").toString();
				try {
					markDate = df.parse(markDateString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (markDate.before(minDate)){minDate = markDate;}
				if (markDate.after(maxDate)){maxDate = markDate;}
			}
		}
		
		dateFilters[0] = minDate;
		dateFilters[1] = maxDate;
	}
	
	//getters and setters
	public void setTarget(String t, Object i){
		if (t.equals("GeoFilterType")){
			this.geoFilterType = i.toString();
		} else if (t.equals("GeoActive")){
			this.geoActive = (boolean) i;
		} else if (t.equals("Geography")){
			this.geoFilterType = i.toString();
		} else if (t.equals("Access level")){
			this.alFilter = i.toString();
		} else if (t.equals("Datatype")){
			this.dtFilter = i.toString();
		} else if (t.equals("Measured item")){
			this.specFilter = i.toString();
		}
	}
	
	public String getFilterByType(String t){
		if (t.equals("Geography")){
			return this.geoFilterType;
		} else if (t.equals("Access level")){
			return this.alFilter;
		} else if (t.equals("Datatype")){
			return this.dtFilter;
		} else if (t.equals("Measured item")){
			return this.specFilter;
		}
		return "";
	}
	
	//getters and setters
	public void setGeoActive(boolean ga){
		this.geoActive = ga;
	}
	
	public boolean getGeoActive(){
		return this.geoActive;
	}
	
	public void setGeoFilterType(String gf){
		this.geoFilterType = gf;
	}
	
	public String getGeoFilterType(){
		return this.geoFilterType;
	}
	
	public void setGeoFilter(String cf){
		this.geoFilter = cf;
	}
	
	public String getGeoFilter(){
		return this.geoFilter;
	}
	
	public void setTagFilter(List<String> tf){
		this.tagFilter = tf;
	}
	
	public List<String> getTagFilter(){
		return this.tagFilter;
	}
	
	public void setDTFilter(String dt){
		this.dtFilter = dt;
	}
	
	public String getDTFilter(){
		return this.dtFilter;
	}
	
	public void setALFilter(String al){
		this.alFilter = al;
	}
	
	public String getALFilter(){
		return this.alFilter;
	}
	
	public void setStartDateFilter(Date d){
		this.dateFilters[0] = d;
	}
	
	public Date getStartDateFilter(){
		return this.dateFilters[0];
	}
	
	public String getDateFilterString(int ind, String form){
		SimpleDateFormat sdf = new SimpleDateFormat(form);
		String y = sdf.format(this.dateFilters[ind]);
		return y;
	}
	
	public void setEndDateFilter(Date d){
		this.dateFilters[1] = d;
	}
	
	public Date getEndDateFilter(){
		return this.dateFilters[1];
	}
	
	public List<String> getAvailsByType(String t){
		if (t.equals("Geography")){
			return this.availGeo;
		} else if (t.equals("Access level")){
			return this.availAL;
		} else if (t.equals("Datatype")){
			return this.availDT;
		} else if (t.equals("Measured item")){
			return this.availSpec;
		}
		return null;
	}
	
	public List<String> getAvailAL(){
		return this.availAL;
	}
	
	public List<String> getAvailGeos(){
		return this.availGeo;
	}
	
	public List<String> getAvailDT(){
		return this.availDT;
	}
	
	public List<String> getAvailTags(){
		return this.availTags;
	}

	public void setSpecFilter(String sf) {
		this.specFilter = sf;
	}
	
	public String getSpecFilter() {
		return this.specFilter;
	}

	public List<String> getAvailSpec() {
		return this.availSpec;
	}
}
