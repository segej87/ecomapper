/*global JSON */

"use strict";

let Values = require('../../res/values');

function loadLists(userId, lists, filters, callback) {
		console.log('Loading lists');
		const formData='GUID=' + userId;
	
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/get_lists.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}

			if (request.status === 200) {
				const result = JSON.parse(request.responseText);
				
				var tagsArray = [];
				if (Object.keys(result).includes('tags') && !result.tags.includes("Warning: tags not found")) {
					tagsArray = result.tags;
				}
				
				var accessArray = lists.access;
				if (Object.keys(result).includes('institutions') && !result.institutions.includes("Warning: institutions not found")) {
					for (var j = 0; j < result.institutions.length; j++) {
						if (!accessArray.includes(result.institutions[j])) accessArray.push(result.institutions[j]);
					}
					let fullAcc = result.institutions.concat(Values.standards.access);
					for (var j = accessArray.length-1; j >= 0; j--) {
						if (!result.institutions.concat(Values.standards.access).includes(accessArray[j])) accessArray.splice(j, 1);
					}
				}
				
				var speciesArray = lists.species;
				if (Object.keys(result).includes('species') && !result.species.includes("Warning: species not found")) {
					for (var j = 0; j < result.species.length; j++) {
						if (!speciesArray.includes(result.species[j])) speciesArray.push(result.species[j]);
					}
					for (var j = speciesArray.length-1; j >= 0; j--) {
						if (!result.species.includes(speciesArray[j])) speciesArray.splice(j, 1);
					}
				}
				
				var submittersArray = lists.submitters;
				if (Object.keys(result).includes('submitters') && !result.submitters.includes("Warning: submitters not found")) {
					for (var j = 0; j < result.submitters.length; j++) {
						if (!submittersArray.includes(result.submitters[j])) submittersArray.push(result.submitters[j]);
					}
					for (var j = submittersArray.length-1; j >= 0; j--) {
						if (!result.submitters.includes(submittersArray[j])) submittersArray.splice(j, 1);
					}
				}
				
				let newState = {
					lists: {
						datatype: lists.datatype,
						submitters: submittersArray,
						access: accessArray,
						tags: tagsArray,
						species: speciesArray,
						date: filters.date
					},
				};
				callback(newState, true);
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	};
	
	function loadCollections(userId, callback) {
		console.log('Loading collections');
		const formData='GUID=' + userId;
	
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/get_collections.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}

			if (request.status === 200) {
				const result = JSON.parse(request.responseText);
				
				callback(result, true);
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	};
	
	function loadRecords(userId, filters, records, override = false, callback) {
		console.log('Loading data');
		const formData='GUID=' + userId + '&filters=' + JSON.stringify(filters);
	
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/get_geojson.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}

			if (request.status === 200) {
				const geoJsonIn = JSON.parse(request.responseText).collection;
				const measObjIn = JSON.parse(request.responseText).measObj;
				const loadDate = new Date();
				const loadTime = loadDate.getTime();
				
				if (Object.keys(geoJsonIn).includes('text') && geoJsonIn.text == "Warning: geojson not found") {
					if (Object.keys(records).length > 0){
						let newState = {
							records: {},
							measObj: {}
						};
						
						callback(newState, [], true)
					}
				} else {
					if (Object.keys(records).length == 0) {
						console.log('Setting initial data');
						var newRecords = geoJsonIn;
						newRecords.updated = loadTime
						
						let newState = {
							records: newRecords,
							measObj: measObjIn
						};
						
						callback(newState, [], true);
					} else {
						var currentRecords = records;
						var currentFeats = currentRecords.features;
						const newFeats = geoJsonIn.features;
						
						if (currentFeats) {
							var currentIds = [];
							for (var i = 0; i < currentFeats.length; i++) {
								currentIds.push(currentFeats[i].id);
							}
							
							var newIds =[]
							for (var i = 0; i < geoJsonIn.features.length; i++) {
								newIds.push(geoJsonIn.features[i].id);
							}
							
							for (var i = 0; i < newIds.length; i++) {
								if (!currentIds.includes(newIds[i])) {
									currentFeats.push(newFeats[i]);
								}
							}
							
							for (var i = currentFeats.length-1; i >=0; i--) {
								if (!newIds.includes(currentFeats[i].id)) {
									currentFeats.splice(i, 1);
								}
							}
							
							let newRecords = {updated: loadTime, type: currentRecords.type, features: currentFeats};
							
							let newState = {
								records: newRecords,
								measObj: measObjIn
							};
							
							callback(newState, newIds, true);
						}
					}
				}
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
				loadingData = false;
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	};
	
	function saveShape(userId, shape, collection, callback) {
		const formData="GUID=" + userId + "&shape=" + JSON.stringify(shape) + "&collection=" + collection;
	
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/add_shape.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}

			if (request.status === 200) {
				const result = request.responseText;
				
				callback(result);
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	};
	
	function loadShapes(collection, callback) {
		const formData="collection=" + collection;
	
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/get_shapes.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}

			if (request.status === 200) {
				const result = JSON.parse(request.responseText);
				callback(result);
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	};
	
	exports.loadLists = loadLists;
	exports.loadCollections = loadCollections;
	exports.loadRecords = loadRecords;
	exports.saveShape = saveShape;
	exports.loadShapes = loadShapes;