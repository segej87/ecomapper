package mapgui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import dataio.SQLReader;
import dataproc.Filtering;
import dataproc.Geo;
import dataproc.Visualization;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import markers.*;
import popups.*;
import processing.core.PApplet;
import processing.core.PFont;

public class MapDisplay extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//flag indicating whether the user is offline
	private static final boolean offline = false;
	//flag indicating whether states should be included in geofilter
	private static final boolean stateson = true;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//initialize toolbars
	private SideToolbar stb;
	private TopToolbar ttb;
	
	//initialize map
	UnfoldingMap map;
	
	//initialize processing classes
	private Filtering filter = new Filtering();
    
	//Initialize lists of standard geographic markers
	private List<Marker> countryMarkers = new ArrayList<Marker>();
	private List<Marker> stateMarkers = new ArrayList<Marker>();
	
	//initialize list of filtered markers (change to package or protected?
	private List<Marker> filteredMarkers;
	
	//initialize popup-related objects
    private boolean activeMarker = false;
	private MapKey mapKey;
	private List<MapPopups> pops = new ArrayList<MapPopups>();
	private List<Marker> visHits = new ArrayList<Marker>();
	private int selectedMark = 0;
	
	public void setup(){
		//get the current graphics device, get its size, and size the map at full screen
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int screenWidth = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
		size(screenWidth, screenHeight, OPENGL);
		
		//set up toolbars
		ttb = new TopToolbar(this, 0, 0, this.width, 50, 250);
		stb = new SideToolbar(this, filter, ttb.getTopTBX(), ttb.getTopTBY() + ttb.getTopTBHeight(), 250, this.height - (ttb.getTopTBY() + ttb.getTopTBHeight()));
		
		//Import and set the font
		PFont lato = createFont("Lato-Bold.ttf", (float) 24);
		textFont(lato);
		
		//set up the map and event dispatcher
		if (offline) {
		    map = new UnfoldingMap(this, stb.getSideTBX() + stb.getSideTBWidth(), ttb.getTopTBY() + ttb.getTopTBHeight(), width-(stb.getSideTBX() + stb.getSideTBWidth()), height-(ttb.getTopTBY() + ttb.getTopTBHeight()), new MBTilesMapProvider(mbTilesString));
		}
		else {
			map = new UnfoldingMap(this, stb.getSideTBX() + stb.getSideTBWidth(), ttb.getTopTBY() + ttb.getTopTBHeight(), width-(stb.getSideTBX() + stb.getSideTBWidth()), height-(ttb.getTopTBY() + ttb.getTopTBHeight()), new Google.GoogleTerrainProvider());
		}
		map.zoomToLevel(3);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		//set up a visualization class for setup
		Visualization vis = new Visualization(this);
		
		//read in country info from geojson and create markers
		List<Feature> countries = GeoJSONReader.loadData(this,"countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		for (Marker country : countryMarkers){
			Visualization.colorAndHide(country, color(255, 0, 0), color(255, 150, 150), true);
		}
		
		if (stateson){
			//read in state info from gejson and create markers
			List<Feature> states = GeoJSONReader.loadData(this,"us-states.geo.json");
			stateMarkers = MapUtils.createSimpleMarkers(states);
			for (Marker state : stateMarkers){
				Visualization.colorAndHide(state, color(0,0,255), color(150,150,255), true);
			}
		}
		
		//read in point data from geoJSON
//		SQLReader sqr = new SQLReader("segej87", "J5e14s87!");
//		SQLReader sqr = new SQLReader("mruth", "M@rty9!");
		//SQLReader sqr = new SQLReader("ctsege", "ctsege85");
		SQLReader sqr = new SQLReader("rsege", "redbird5");
		List<Feature> personFeats = GeoJSONReader.loadDataFromJSON(this, sqr.jsonString);
		
		//loop through features and find which country they're in by comparing to countryMarkers
		//TODO: write this info to json file, then only do for data that doesn't have this prop
		Geo.isinGeoFeats(personFeats, countryMarkers, "country");
		if (stateson){Geo.isinGeoFeats(personFeats, stateMarkers, "state");}
		
		//create markers from imported point features
		List<PointMarker> pointMarkers = new ArrayList<PointMarker>();
		vis.assignMarkers(personFeats, pointMarkers);
		
		//get the filtered markers
		filteredMarkers = filter.getFiltered(pointMarkers, new Visualization(this));
		
		//set up side toolbar elements
		stb.setup();
		
		//add the markers to the map
		//TODO: think of a way to make the geofiltering process use less memory
		map.addMarkers(countryMarkers);
		if (stateson){map.addMarkers(stateMarkers);}
		map.addMarkers(filteredMarkers);
	}
	
	public void draw(){
		//TODO: Make it so clicked info disappears when map moved or zoomed
		background(255);
		map.draw();
		if (activeMarker){addPops();}
		ttb.draw();
		stb.draw();
		
		//handle cursor change to hand over buttons
		if (isInButton()){cursor(HAND);} else {cursor(ARROW);}
	}
	
	private boolean isInButton(){
		for (Button b : stb.getButtons()){if (b.inButton(mouseX, mouseY)){return true;}}
		for (TagButton tb : stb.getTagButtons()){if (tb.inButton(mouseX, mouseY)){return true;}}
		if (activeMarker){
			if (mapKey.getMultMarks() && (mapKey.inLeftSwipe(selectedMark, mouseX, mouseY) || mapKey.inRightSwipe(selectedMark, visHits, mouseX, mouseY))){
				return true;
			}
		}
		return false;
	}
	
	public void mouseClicked(){
		//Determine which button was hit (if none, return null)
		Button button = whichButton();
		
		//perform pre button actions
		clickPreAction(button);
		
		//perform button action
		if (button != null){button.doAction(); handleNoPops();}
		
		//perform post button actions
		clickPostAction(button);
	}
	
	private Button whichButton() {
		//loop through buttons and tag buttons in the side toolbar
		//if one was hit, return it
		for (Button b: stb.getButtons()){
			if (b.inButton(mouseX, mouseY)){return b;}
		}
		for (TagButton b : stb.getTagButtons()){
			if (b.inButton(mouseX, mouseY)){return b;}
		}
		return null;
	}
	
	private void clickPreAction(Button button) {
		//loop through the dropdowns and set all to inactive if they weren't hit
		for (Button button2 : stb.getButtons()){
			if (button2 instanceof Dropdown && !button2.equals(button)){((Dropdown)button2).setActive(false);}
		}
		
		//things to do if a button was hit
		if (button != null){
			//if the geography filter button was hit and the map was previously filtered
			//by geography, clear it and deactivate geoFiltering
			if (button.getType().equals("Geography")){
				if (filter.getGeoFilterType() != ""){
					for (Marker count : countryMarkers){count.setHidden(true);}
					if (stateson){
						for (Marker state : stateMarkers){state.setHidden(true);}
					}
					filter.setGeoActive(false);
				}
			}
		}
	}

	private void clickPostAction(Button button) {
		if (button == null){
			if (filter.getGeoActive()){
					handleNoPops();
					handleGeoFilter();
				}
			else {
				//re-initialize the popups array to remove old info
				pops = new ArrayList<MapPopups>();
				if (activeMarker && mapKey.getMultMarks()){
					handleMultiPops();
				} else {
					analyzeNewHit();
					if (visHits.size() != 0){
						handleNewPops();
					} else {
						handleNoPops();
					}
				}
			}
		} else {
			if (button instanceof ToggleButton){
				filter.updateFiltered(filteredMarkers, new Visualization(this), false);
				stb.redrawTags();
			} else if (button.getType().equals("Datatype") || button.getType().equals("Access level") || button.getType().equals("Measured item")){
				refreshMapData();
			} else if (button instanceof FilterDropdown && !button.getType().contains("Geography")){
				if (!filter.getFilterByType(button.getType().replace("DD", "")).equals("")){
					refreshMapData();
				}
			} else if (button.getType().equals("Geography")){
				if (!filter.getGeoActive()){
					refreshMapData();
				}
			} else if (button instanceof TagButton){
				handleTagHit();
				filter.updateFiltered(filteredMarkers, new Visualization(this), true);
			}
			refreshDropdowns();
		}
	}
	
	private void refreshMapData() {
		filter.setTagFilter(null);
		filter.updateFiltered(filteredMarkers, new Visualization(this), true);
		stb.redrawTags();
	}

	private void refreshDropdowns() {
		for (Button b : stb.getButtons()){
			if (b instanceof FilterDropdown){
				((FilterDropdown)b).setItems(filter.getAvailsByType(b.getType().replace("DD", "")).toArray(new String[0]));
			}
		}
	}

	private void analyzeNewHit() {
		//get the marker(s) selected when the mouse was clicked
		//TODO: figure out how to group markers by location for data analysis in future functionality
		List<Marker> allHits = new ArrayList<Marker>();
		for (Marker mark : filteredMarkers){
			if (mark.isInside(map, mouseX, mouseY)){
				allHits.add(mark);
			}
		}
		
		//re-initialize vars
		visHits = new ArrayList<Marker>();
		selectedMark = 0;
		if (allHits.size() != 0){
			for (Marker mark : allHits){
				if (!mark.isHidden()){
					visHits.add(mark);
				}
			}
		}
	}
	
	private void handleGeoFilter(){
		Geo geo = new Geo(map);
		List<Marker> marks = new ArrayList<Marker>();
		if (filter.getGeoFilterType().equals("country")){
				marks = countryMarkers;
			} else if (filter.getGeoFilterType().equals("state")){
				marks = stateMarkers;
			}
		float[] loc = {mouseX, mouseY};
		Marker hitGeo = filter.findFilterGeo(loc, marks, geo);
		if (hitGeo != null){
			if (hitGeo.isHidden()){
				for (Marker geoUnit : marks){
					Visualization.colorAndHide(geoUnit, -1, color(255, 150, 150), true);
				}
				hitGeo.setHidden(false);
			} else {
				filter.updateFiltered(filteredMarkers, new Visualization(this), true);
				refreshDropdowns();
				hitGeo.setColor(color(255, 255, 255, 0));
				filter.setGeoActive(false);
				stb.redrawTags();
			}
		}
	}
	
	private void handleNewPops() {
		//make the popups and set activeMarker to true to draw
		makePops(visHits, selectedMark, mouseX, mouseY);
		activeMarker = true;
	}
	
	private void handleNoPops() {
		//remove popups and set flag to false to remove info from map
		visHits = new ArrayList<Marker>();
		pops = new ArrayList<MapPopups>();
		activeMarker = false;
	}
	
	private void handleMultiPops() {
		if (mapKey.inRightSwipe(selectedMark, visHits, mouseX, mouseY)){
			if (selectedMark < visHits.size() - 1) {selectedMark = selectedMark + 1;}
			makePops(visHits, selectedMark, mapKey.getClickX(), mapKey.getClickY());
		} else if (mapKey.inLeftSwipe(selectedMark, mouseX, mouseY)) {
			if (selectedMark > 0) {selectedMark = selectedMark - 1;}
			makePops(visHits, selectedMark, mapKey.getClickX(), mapKey.getClickY());
		} else {
			//remove popups and set flag to false to remove info from map
			selectedMark = 0;
			visHits = new ArrayList<Marker>();
			activeMarker = false;
			//TODO: Remove this after moving and panning removes popups
			mouseClicked();
		}
	}
	
	private void handleTagHit(){
		filter.setTagFilter(new ArrayList<String>());
		List<String> newTags = new ArrayList<String>();
		for (TagButton tb : stb.getTagButtons()){
			if (tb.getActiveArray()){
				newTags.add(tb.getText());
			}
		}
		filter.setTagFilter(newTags);
	}
	
	private void makePops(List<Marker> allHits, int selectedMark, float mouseX, float mouseY){
		Marker hitMarker = visHits.get(selectedMark);
		
		//TODO: allow user to choose which info is in header title (this is partly set up in MapKey)
		//String[] keyLines = {"type", "name", "datetime", "country"};
		
		//the mapKey needs to be the first thing added to the pops array
		mapKey = new MapKey(this, map, mouseX, mouseY, hitMarker);
		if (visHits.size() > 1) { mapKey.setMultMarks(true); }
		pops.add(mapKey);
		
		//If this is moved below other popup objects, the order will be changed
		pops.add(new MapTags(map, this, pops.get(pops.size()-1)));
		
		//If this is moved above the other popup objects, the order will be changed
		if (mapKey.getStringProp("datatype").equals("photo")){
			pops.add(new MapPhoto(map, this, pops.get(pops.size()-1)));
		} else if (mapKey.getStringProp("datatype").equals("note")){
			pops.add(new MapNote(map, this, pops.get(pops.size()-1)));
		} else if (mapKey.getStringProp("datatype").equals("meas")){
			pops.add(new MapMeas(map, this, pops.get(pops.size()-1)));
		}
	}
	
	private void addPops() 
	{
		pushStyle();
		for (int i=0; i < pops.size(); i++){
			pops.get(i).draw();
		}
		
		if (mapKey.getMultMarks()){
			if (selectedMark > 0) {mapKey.addLeftSwipe();}
			if (selectedMark < visHits.size() - 1) {mapKey.addRightSwipe();}
		}
		popStyle();
	}
}