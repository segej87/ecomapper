React = require('react');
let Sidebar = require('./Sidebar');
let Container = require('./Container').default;
const GeoFilterInfoPanel = require('./GeoFilterInfoPanel');
let Values = require('../res/values');
let mapStyles = require('../styles/map/mapStyles');
let mainStyles = require('../styles/home/mainStyles');
import TimerMixin from 'react-timer-mixin';

//TODO: REMOVE AFTER TESTING
// let testJson = require('../res/json/crashes.json');

var firstListLoad = true;
var loadingLists = false;
var loadingData = false;

var standIds = [];
var standVals = [];
var standUnits = [];
var workingSet = [];

var MapContainer = React.createClass({
	mixins: [TimerMixin],
	
	getInitialState: function () {
		var today = new Date();
		var monthago = new Date();
		monthago.setDate(today.getDate() - 30);
		monthago.setHours(0);
		monthago.setMinutes(0);
		monthago.setSeconds(0);
		monthago.setMilliseconds(0);
		
		return {
			lists: {
				datatype: ['Meas','Photo','Note'],
				submitters: [],
				access: ['Public','Private'],
				tags: [],
				species: [],
				date: ['none', 'none']
			},
			filters: {
				datatype: [],
				submitters: [],
				access: [],
				tags: [],
				species: [],
				date: [monthago, today]
			},
			records: {},
			measObj: {},
			selectedMeasDist: [],
			//TODO: LOAD SHAPES FROM DB
			shapes: {Geo: ['Countries','US States']},
			selectedPlace: {},
			geoFiltering: null
		};
	},
	
	handleFilterChange: function (type, val, result) {
		var newItems;
		switch (result) {
			case 'Remove':
				newItems = [];
				for (var i = 0; i < this.state.filters[type].length; i++) {
					if (this.state.filters[type][i] != val) {
						newItems.push(this.state.filters[type][i]);
					}
				}
				break;
			case 'Remove all others':
				newItems = [val];
				break;
			case 'Add':
				newItems = this.state.filters[type];
				if (!newItems.includes(val)) {
					newItems.push(val);
				}
				break;
			case 'Replace':
				newItems = val;
				break;
			default:
				break;
		}
		
		var newFilters = {};
		for (var i = 0; i < Object.keys(this.state.filters).length; i++) {
			newFilters[Object.keys(this.state.filters)[i]] = Object.values(this.state.filters)[i];
		}
		if (newItems) {
			if (!type.includes('Date')) {
				newFilters[type] = newItems;
			} else {
				if (type.includes('start')) {
					newFilters.date[0] = newItems;
				} else if (type.includes('end')) {
					newFilters.date[1] = newItems;
				}
			}
		}
		
		this.setState({
			filters: newFilters
		});
		
		this.loadRecords(newFilters, true);
	},
	
	toggleGeoFilter: function (type) {
		var newState;
		if (type == this.state.geoFiltering) {
			newState = null;
		} else {
			newState = type;
		}
		
		this.setState({
			geoFiltering: newState
		});
	},
	
	loadLists: function () {
		if (this.props.userInfo.userId != null && !this.props.offline && !loadingLists) {
			loadingLists = true;
			console.log('Loading lists');
			const formData='GUID=' + this.props.userInfo.userId;
		
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
					
					var accessArray = this.state.lists.access;
					if (Object.keys(result).includes('institutions') && !result.institutions.includes("Warning: institutions not found")) {
						for (var j = 0; j < result.institutions.length; j++) {
							accessArray.push(result.institutions[j]);
						}
					}
					
					var speciesArray = this.state.lists.species;
					if (Object.keys(result).includes('species') && !result.species.includes("Warning: species not found")) {
						for (var j = 0; j < result.species.length; j++) {
							speciesArray.push(result.species[j]);
						}
					}
					
					var submittersArray = this.state.lists.submitters;
					if (Object.keys(result).includes('submitters') && !result.submitters.includes("Warning: submitters not found")) {
						for (var j = 0; j < result.submitters.length; j++) {
							submittersArray.push(result.submitters[j]);
						}
					}
					
					this.setState({
						lists: {
							datatype: this.state.lists.datatype,
							submitters: submittersArray,
							access: accessArray,
							tags: tagsArray,
							species: speciesArray,
							date: this.state.filters.date
						},
					});
					
					if (firstListLoad) {
						firstListLoad = false;
						this.loadRecords();
					}
					
					loadingLists = false;
				} else {
					console.log('Status: ' + request.status);
					console.log('Status text: ' + request.statusText);
					loadingLists = false;
				}
			};

			request.open(method, url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
		}
	},
	
	loadRecords: function (filters = this.state.filters, override = false) {
		if (this.props.userInfo.userId != null && !this.props.offline && !this.state.geoFiltering && (!loadingData || override)) {
			loadingData = true;
			console.log('Loading data');
			const formData='GUID=' + this.props.userInfo.userId + '&filters=' + JSON.stringify(filters);
		
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
						if (Object.keys(this.state.records).length > 0){
							this.setState({
								records: {},
								measObj: {}
							});
							loadingData = false;
						}
					} else {
						if (Object.keys(this.state.records).length == 0) {
							console.log('Setting initial data');
							var newRecords = geoJsonIn;
							newRecords.updated = loadTime
							
							this.setState({
								records: newRecords,
								measObj: measObjIn
							});
							loadingData = false;
						} else {
							var currentRecords = this.state.records;
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
								
								for (var i = standIds.length-1; i >=0; i--) {
									if (!newIds.includes(standIds[i])) {
										standIds.splice(i, 1);
										standVals.splice(i, 1);
										standUnits.splice(i, 1);
									}
								}
								
								let newRecords = {updated: loadTime, type: currentRecords.type, features: currentFeats};
								
								this.setState({
									records: newRecords,
									measObj: measObjIn
								});
								loadingData = false;
							}
						}
					}
					
					
					this.standardizeUnits(measObjIn);
				} else {
					console.log('Status: ' + request.status);
					console.log('Status text: ' + request.statusText);
					loadingData = false;
				}
			};

			request.open(method, url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
		}
	},
	
	setWorkingSet: function (ids) {
		workingSet = ids;
	},
	
	standardizeUnits: function (unitObj) {
		if (unitObj == null) {
			return
		}
		
		const url = "http://192.168.220.128/ocpu/library/kora.scripts/R/standardizeunits/json";
		const method = "POST";
		
		for (var i = 0; i < Object.keys(unitObj).length; i++) {
			(function(i){
				const sourceObj = unitObj[Object.keys(unitObj)[i]];
				const testVals = sourceObj.vals;
				const testUnits = sourceObj.units;
				const testTarget = sourceObj.target;
				const testConvs = sourceObj.convs;
				const formData=JSON.stringify({vals: testVals, units: testUnits, target: testTarget, conversions: testConvs});
				
				var request = new XMLHttpRequest;
				
				request.onreadystatechange = (e) => {
					if (request.readyState !== 4) {
						return;
					}
					
					if (request.status === 200) {
						console.log(request.response);
						
						for (var j = 0; j < sourceObj.ids.length; j++) {
							if (!standIds.includes(sourceObj.ids[j])) {
								standIds.push(sourceObj.ids[j]);
								standVals.push(JSON.parse(request.response)[j])
								standUnits.push(testTarget);
							} else if (standVals[standIds.indexOf(sourceObj.ids[j])] != JSON.parse(request.response)[j] || standUnits[standIds.indexOf(sourceObj.ids[j])] != testTarget) {
								standVals[standIds.indexOf(sourceObj.ids[j])] = JSON.parse(request.response)[j];
								standUnits[standIds.indexOf(sourceObj.ids[j])] = testTarget;
							}
						}
					} else {
						console.log(request.status);
						console.log(request.statusText);
					}
				};
				
				request.open(method, url, true);
				request.setRequestHeader("Content-type", "application/json");
				request.send(formData);
			})(i);
		}
	},
	
	resetRecords: function () {
		firstListLoad = true;
		this.setState(this.getInitialState());
		
		this.loadLists();
	},
	
	handleDelete: function (id) {
		let currentRecords = this.state.records;
		let currentFeatures = currentRecords.features;
		
		for (var i = 0; i < currentFeatures.length; i++) {
			if (currentFeatures[i].id == id) {
				currentFeatures.splice(i, 1);
			}
		}
		
		let newRecords = {type: currentRecords.type, features: currentFeatures};
		
		this.setState({
			records: newRecords,
			selectedPlace: {}
		});
	},
	
	setMeasDist: function (type) {
		if (this.state.records.features) {
			var ids = [];
			var vals = [];
			for (var i = 0; i < this.state.records.features.length; i++) {
				if ((workingSet.length == 0 || workingSet.includes(this.state.records.features[i].id)) && this.state.records.features[i].properties.species == type) {
					ids.push(this.state.records.features[i].id);
				}
			}
			
			for (var i = 0; i < ids.length; i++) {
				vals.push(standVals[standIds.indexOf(ids[i])]);
			}
			
			return vals;
		}
	},
	
	handleSelectedPlace: function (selected) {
		var vals = [];
		var selectedMeasStand;
		var selectedMeasUnit;
		
		if (selected.featureProps) {
			if (selected.featureProps.datatype == 'meas') {
				const ind = standIds.indexOf(selected.fuid);
				vals = this.setMeasDist(selected.featureProps.species[0]);
				selectedMeasStand = standVals[ind];
				selectedMeasUnit = standUnits[ind];
			}
		}
		
		console.log(vals);
		
		this.setState({
			selectedPlace: selected,
			selectedMeasDist: vals,
			selectedMeasStand: selectedMeasStand,
			selectedMeasUnit: selectedMeasUnit
		});
	},
	
	componentWillMount: function () {
		this.loadLists();
	},
  
	componentDidMount: function () {
		firstListLoad = true;
		
		this.setInterval(
			() => { this.loadRecords(); },
			30000
		);
	},
	
	render: function () {
		var gfp;
		if (this.state.geoFiltering) {
			gfp = (
				<GeoFilterInfoPanel
				geoFiltering={this.state.geoFiltering}
				selectedGeo={this.state.selectedGeo}
				toggleGeoFilter={this.toggleGeoFilter}
				/>
			);
		}
		
		return (
			<div style={mapStyles.main}>
				<Sidebar 
				userInfo={this.props.userInfo} 
				selectedPlace={this.state.selectedPlace} 
				selectedMeasDist={this.state.selectedMeasDist}
				selectedMeasStand={this.state.selectedMeasStand}
				selectedMeasUnit={this.state.selectedMeasUnit}
				filters={this.state.filters}
				lists={this.state.lists}
				handleDelete={this.handleDelete}
				onFilterChange={this.handleFilterChange}
				shapes={this.state.shapes}
				toggleGeoFilter={this.toggleGeoFilter}
				/>
				<Container 
				userInfo={this.props.userInfo} 
				geoFiltering={this.state.geoFiltering}
				records={this.state.records} 
				filters={this.state.filters} 
				handleSelected={this.handleSelectedPlace}
				resetRecords={this.resetRecords}
				setWorkingSet={this.setWorkingSet}
				/>
				{gfp}
			</div>
		)
	}
});

export default (MapContainer);