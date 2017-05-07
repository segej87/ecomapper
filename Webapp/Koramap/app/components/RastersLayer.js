let Rutils = require('../src/utils/Rutils');
const Geoutils = require('../src/utils/Geoutils');
const Serverutils = require('../src/utils/Serverutils');
const Values = require('../res/values');

class RastersLayer {
	constructor(appState) {
		this.appState = appState;
	}
	
	setGoogle (google) {
		this.google = google;
	}
	
	// Raster methods
	idw (polys) {
		// initialize the bounds
		var bounds = new this.google.maps.LatLngBounds();

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
				let overlay = new this.google.maps.GroundOverlay(base64data, bounds);
				overlay.setMap(this.appState.getMap());
			}.bind(this);
		}.bind(this)

		Rutils.idw(command, args, callback);
	}
}

export default RastersLayer;