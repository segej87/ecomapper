let Rutils = require('../src/utils/Rutils');
const Geoutils = require('../src/utils/Geoutils');
const Serverutils = require('../src/utils/Serverutils');
const Values = require('../res/values');

class ShapesLayer {
	constructor(appState) {
		this.appState = appState;
		this.shapes = Values.standards.shapes;
		this.selectedGeos = [];
		this.selectedGeoNames = [];
		this.addedShapes = [];
	}
	
	// Get a list of available shape collections from the server
	loadCollections () {
		if (this.appState.getUserInfo().userId != null) {
			let callback = function (result, success) {
				if (success) {
					let newShapes = Values.standards.shapes
					for (var i = 0; i < Object.keys(result).length; i++) {
						if (!(Object.values(result)[i].text && Object.values(result)[i].text == 'Warning: empty query result')) {
							newShapes[Object.keys(result)[i]] = Object.values(result)[i]
						}
					}
					
					this.shapes = newShapes;
				}
			}.bind(this);
			
			Serverutils.loadCollections(this.appState.getUserInfo().userId, callback);
		}
	}
	
	//Show a shape
	showShape (shape)  {
		this.addedShapes.push(this.map.data.addGeoJson(shape)[0]);
	}
	
	//Geofiltering methods
	startGeoFilter (geoFiltering) {
		switch (geoFiltering) {
			case 'countries':
				this.startGeoActive(require('../res/json/countries.geo.json'));
				break;
			case 'usstates':
				this.startGeoActive(require('../res/json/us-states.geo.json'));
				break;
			default:
				let callback = function (result) {
					this.startGeoActive(result);
				}.bind(this);
				Serverutils.loadShapes(geoFiltering, callback);
		}			
	}
	
	startGeoActive(geos) {
		this.setupActiveGeos();
		
		this.geoFilters = this.map.data.addGeoJson(geos);
	}
	
	setupActiveGeos() {
		this.map.data.setStyle(function(feature) {
			var color = 'gray';
			if (feature.getProperty('isColorful')) {
				color = 'green';
			}
			return /** @type {google.maps.Data.StyleOptions} */({
				fillColor: color,
				strokeColor: color,
				strokeWeight: 2,
				clickable: true
			});
		});

		this.clickListener = this.map.data.addListener('click', function(event) {
			event.feature.setProperty('isColorful', !event.feature.getProperty('isColorful'));
			
			var testArray = [];
			for (var i = 0; i < this.selectedGeos.length; i++) {
				testArray.push(this.selectedGeos[i].getId());
			}
			
			if (testArray.includes(event.feature.getId())) {
				this.selectedGeos.splice(testArray.indexOf(event.feature.getId()), 1);
				this.selectedGeoNames.splice(testArray.indexOf(event.feature.getId()), 1);
			} else {
				this.selectedGeos.push(event.feature);
				this.selectedGeoNames.push(event.feature.getProperty('name'))
			}
			
			document.getElementById('geoinfodescrip').innerHTML = this.selectedGeoNames.join(", ");
		}.bind(this));

		this.hoverListener = this.map.data.addListener('mouseover', function(event) {
			this.map.data.revertStyle();
			this.map.data.overrideStyle(event.feature, {strokeWeight: 8});
		});

		this.leaveListener = this.map.data.addListener('mouseout', function(event) {
			this.map.data.revertStyle();
		});
	}
	
	stopGeoFilter() {
		let selectedGeoIds = [];
		for (var i = 0; i < Object.keys(this.selectedGeos).length; i++) {
			selectedGeoIds.push(this.selectedGeos[Object.keys(this.selectedGeos)[i]].getId())
		}
		
		for (var i = 0; i < this.geoFilters.length; i++) {
			if (!selectedGeoIds.includes(this.geoFilters[i].getId())) {
				this.map.data.remove(this.geoFilters[i]);
			}
		}
		
		this.setupInactiveGeos();
		
		this.geoFilters = null;
		
		console.log(this.selectedGeos);
		
		// return this.selectedGeos;
	}
	
	setupInactiveGeos() {
		// Color each shape gray. Change the color when the isColorful property
		// is set to true.
		this.map.data.setStyle(function(feature) {
			return /** @type {google.maps.Data.StyleOptions} */({
				fillColor: 'rgba(0,0,0,0)',
				strokeColor: 'red',
				strokeWeight: 2,
				clickable: false
			});
		});
		
		google.maps.event.removeListener(this.clickListener);
		google.maps.event.removeListener(this.hoverListener);
		google.maps.event.removeListener(this.leaveListener);
	}
	
	
	//Drawing shapes methods
	startDrawingShape(type, showNewShapeDialog, onStartDrawShape) {
		this.drawingManager = new google.maps.drawing.DrawingManager({
			drawingMode: google.maps.drawing.OverlayType[type.toUpperCase()],
			drawingControl: false,
			circleOptions: {
				fillColor: '#ffff00',
				fillOpacity: 1,
				strokeWeight: 5,
				clickable: false,
				editable: true,
				zIndex: 1
			},
			polygonOptions: {
				fillColor: '#009cde',
				fillOpacity: 0.5,
				strokeColor: '#009cde',
				editable: true
			},
			polylineOptions: {
				strokeWeight: 5
			}
		});
		
		let completion = function (e) {
			this.stopDrawingShape(onStartDrawShape);
			let overlay = e.overlay;
			overlay.type = e.type;
			showNewShapeDialog(overlay);
		}.bind(this);
		
		let cancel = function (e) {
			console.log(e.code)
			if (e.code == '0x0001') {
				this.stopDrawingShape(onStartDrawShape);
			}
		}.bind(this);
		
		google.maps.event.addListener(this.drawingManager, 'overlaycomplete', completion);
		
		this.drawingManager.setMap(this.map);
	}
	
	stopDrawingShape(onStartDrawShape) {
		this.drawingManager.setMap(null);
		onStartDrawShape(null);
		// google.maps.event.removeListener(cancelShape);
	}
	
	
	// Map
	setMap (map) {
		this.map = map;
	}
	
	// All shapes
	getShapes () {
		return this.shapes;
	}
	
	// Shapes selected for geo filtering
	getSelectedGeos () {
		return this.selectedGeos;
	}
}

export default ShapesLayer;