/*global JSON */

"use strict";

//TODO: wrap in promise?
function filterGeo(filterGeos, features, google) {
	if (filterGeos.length == 0) {
		return ({geoFilteredFeats: features, workingSet: []});
	}
	
	var geoFilteredFeats = [];
	var workingSet = [];
	for (var i = 0; i < features.length; i++) {
		const position = new google.maps.LatLng(features[i].geometry.coordinates[1], features[i].geometry.coordinates[0]);
		for (var j = 0; j < filterGeos.length; j++) {
			var testPoly;
			if (filterGeos[j] instanceof google.maps.Polygon) {
				testPoly = filterGeos[j]
			} else {
				var paths=[];
				if (filterGeos[j].geometry.getType() == 'MultiPolygon') {
					const testPolys = filterGeos[j].geometry.getArray();
					for (var k = 0; k < testPolys.length; k++) {
						testPolys[k].forEachLatLng((LatLng) => {
							paths.push(LatLng);
						});	
					}
				} else if (filterGeos[j].geometry.getType() == 'Polygon') {
					filterGeos[j].geometry.forEachLatLng((LatLng) => {
						paths.push(LatLng);
					})
				}
				testPoly = new google.maps.Polygon({paths: paths});
			}
			
			if (google.maps.geometry.poly.containsLocation(position, testPoly)) {
				geoFilteredFeats.push(features[i]);
				workingSet.push(features[i].id);
				break;
			}
		}
	}
	
	return ({geoFilteredFeats: geoFilteredFeats, workingSet});
}

exports.filterGeo = filterGeo;