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
	}
	
	// Get data from the server
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
	
	//Geofiltering methods
	startGeoFilter (geoFiltering) {
		switch (geoFiltering) {
			case 'countries':
				console.log('loading countries');
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
		
		return this.selectedGeos;
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
	
	
	// Raster methods
	addTestRaster (polys) {
		// initialize the bounds
		var bounds = new google.maps.LatLngBounds();

		// iterate over the paths to get overall bounds
		polys[0].getGeometry().forEachLatLng(function(path){
			bounds.extend(path);
		});
		
		let nyColl = {type: 'FeatureCollection'};
		nyColl.features = [Geoutils.assembleDataShapeGeoJson(polys[0])];
		
		let command = '/library/kora.geo/R/shapeidw'
		
		let x = [];
		let y = [];
		let z = [];
		for (var i = 0; i < this.appState.getRecords().features.length; i++) {
			if (this.appState.getWorkingSet().includes(this.appState.getRecords().features[i].id) && this.appState.getRecords().features[i].properties.datatype == 'meas') {
				x.push(this.appState.getRecords().features[i].geometry.coordinates[0]);
				y.push(this.appState.getRecords().features[i].geometry.coordinates[1]);
				z.push(this.appState.getStandVals()[this.appState.getStandIds().indexOf(this.appState.getRecords().features[i].id)]);
			}
		}
		
		if (x.length == 0 || y.length == 0 || z.length == 0) {
			return;
		}
		
		let args = {
			shapeString: JSON.stringify(nyColl),
			x: x,
			y: y,
			z: z,
			n: 50000,
			idp: 2,
			alpha: 0.5,
			save: true,
			guid: this.appState.getUserInfo().userId,
			filename: 'thisisatestfromjavascript'
		}
		
		console.log(this.appState.getUserInfo().userId);
		
		let callback = function (imageDat) {
			var reader = new window.FileReader();
			reader.readAsDataURL(imageDat);
			reader.onloadend = function () {
				let base64data = reader.result;
				// let startInd = base64data.indexOf('data');
				// let endInd = base64data.indexOf('base64,') + 7;
				// let data = base64data.replace(base64data.substring(startInd,endInd),'');
				
				//TODO: turn into array in global scope and push new overlays. Give each id and name
				let overlay = new google.maps.GroundOverlay(base64data, bounds);
				overlay.setMap(this.appState.getMap());
			}.bind(this);
		}.bind(this)

		Rutils.idw(command, args, callback);
	}
	
	setMap (map) {
		this.map = map;
	}
	
	getShapes () {
		return this.shapes;
	}
}

export default ShapesLayer;