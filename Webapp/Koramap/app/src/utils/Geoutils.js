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
				if (filterGeos[j].getGeometry().getType() == 'MultiPolygon') {
					const testPolys = filterGeos[j].getGeometry().getArray();
					for (var k = 0; k < testPolys.length; k++) {
						testPolys[k].forEachLatLng((LatLng) => {
							paths.push(LatLng);
						});	
					}
				} else if (filterGeos[j].getGeometry().getType() == 'Polygon') {
					filterGeos[j].getGeometry().forEachLatLng((LatLng) => {
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

function assembleShapeGeoJson(overlay, props) {
	let outShape = {type: 'Feature', properties: props, geometry: {type: overlay.type}};
	
	let geom = [];
	overlay.getPaths().forEach((p, i) => {
		p.forEach((l, i) => {
			geom.push([l.lng(), l.lat()]);
		});
	});
	
	if (overlay.type.toLowerCase() == 'polygon' && (geom[geom.length-1][0] != geom[0][0] || geom[geom.length-1][1] != geom[0][1])) {
		geom.push(geom[0]);
	}
	
	outShape.geometry.coordinates = [geom];
	
	return outShape;
}

function assembleDataShapeGeoJson(overlay, props) {
	let outShape = {type: 'Feature', properties: props, geometry: {type: overlay.getGeometry().getType()}};
	
	let geom = [];
	overlay.getGeometry().getArray().forEach((a, i) => {
		let innerArray = [];
		a.forEachLatLng((p, i) => {
			innerArray.push([p.lng(), p.lat()]);
		});
		geom.push(innerArray);
	});
	
	if (overlay.getGeometry().getType().toLowerCase() == 'polygon' && (geom[0][geom[0].length-1][0] != geom[0][0][0] || geom[0][geom[0].length-1][1] != geom[0][0][1])) {
		geom[0].push(geom[0][0]);
	}
	
	outShape.geometry.coordinates = geom;
	
	return outShape;
}

exports.filterGeo = filterGeo;
exports.assembleShapeGeoJson = assembleShapeGeoJson;
exports.assembleDataShapeGeoJson = assembleDataShapeGeoJson;