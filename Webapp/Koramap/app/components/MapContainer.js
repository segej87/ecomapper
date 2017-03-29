React = require('react');
let Sidebar = require('./Sidebar');
let Container = require('./Container').default;
const GeoFilterInfoPanel = require('./GeoFilterInfoPanel');
const DrawingShapeInfoPanel = require('./DrawingShapeInfoPanel');
let Values = require('../res/values');
let mapStyles = require('../styles/map/mapStyles');
let mainStyles = require('../styles/home/mainStyles');
import TimerMixin from 'react-timer-mixin';
let Serverutils = require('../src/utils/Serverutils');
let Rutils = require('../src/utils/Rutils');
const Geoutils = require('../src/utils/Geoutils');
import { assembleShapeGeoJson } from '../src/utils/Geoutils';

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
				datatype: Object.values(Values.standards.datatypes),
				submitters: [],
				access: Object.values(Values.standards.access),
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
			shapes: Values.standards.shapes,
			selectedPlace: {},
			geoFiltering: null,
			drawingShape: null,
			pendingOverlay: false
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
		
		let vals = [];
		if (newFilters.datatype.length == 1 && newFilters.datatype[0] == 'Meas' && newFilters.species.length == 1) {
			vals = this.setMeasDist(newFilters.species[0]);
		}
		
		this.setState({
			filters: newFilters,
			selectedMeasDist: vals
		});
		
		this.loadRecords(newFilters, true);
	},
	
	onStartDrawShape: function (type) {
		this.setState({
			drawingShape: type
		});
	},
	
	showNewShapeDialog: function (overlay) {
		this.newOverlay = overlay;
		
		this.setState({
			pendingOverlay: true
		});
	},
	
	saveShape: function (props, collection) {
		let outShape = assembleShapeGeoJson(this.newOverlay, props);
		this.removePendingShape(false);
		
		let callback = function (result) {
			console.log(result);
		};
		
		Serverutils.saveShape(this.props.userInfo.userId, outShape, collection, callback);
	},
	
	removePendingShape: function (canceled) {
		if (canceled) {
			this.newOverlay.setMap(null);
		}
		
		this.newOverlay.setEditable(false);
		delete this.newOverlay;
		
		this.setState({
			pendingOverlay: false,
			drawingShape: false
		});
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
			
			let callback = function (result, success) {
				if (success) {
					this.setState(result)
				
					if (firstListLoad) {
						firstListLoad = false;
						this.loadRecords();
					}
				}
				
				loadingLists = false;
			}.bind(this);
			
			Serverutils.loadLists(this.props.userInfo.userId, this.state.lists, this.state.filters, callback);
		}
	},
	
	loadCollections: function () {
		if (this.props.userInfo.userId != null && !this.props.offline) {
			let callback = function (result, success) {
				if (success) {
					let newShapes = Values.standards.shapes
					for (var i = 0; i < Object.keys(result).length; i++) {
						if (!(Object.values(result)[i].text && Object.values(result)[i].text == 'Warning: empty query result')) {
							newShapes[Object.keys(result)[i]] = Object.values(result)[i]
						}
					}
					
					this.setState({
						shapes: newShapes
					});
				}
			}.bind(this);
			
			Serverutils.loadCollections(this.props.userInfo.userId, callback);
		}
	},
	
	loadRecords: function (filters = this.state.filters, override = false) {
		if (this.props.userInfo.userId != null && !this.props.offline && !this.state.geoFiltering && !this.state.drawingShape && (!loadingData || override)) {
			loadingData = true;
			
			let callback = function (newState, newIds, success) {
				if (success) {
					this.setState(newState);
				
					for (var i = standIds.length-1; i >=0; i--) {
						if (!newIds.includes(standIds[i])) {
							standIds.splice(i, 1);
							standVals.splice(i, 1);
							standUnits.splice(i, 1);
						}
					}
					
					this.standardizeUnits(newState.measObj);
				}
				
				loadingData = false;
			}.bind(this);
			
			Serverutils.loadRecords(this.props.userInfo.userId, filters, this.state.records, override, callback);
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
				const args={vals: sourceObj.vals, units: sourceObj.units, target: sourceObj.target, conversions: sourceObj.convs};
				
				let callback = function (result) {
					for (var j = 0; j < sourceObj.ids.length; j++) {
						if (!standIds.includes(sourceObj.ids[j])) {
							standIds.push(sourceObj.ids[j]);
							standVals.push(JSON.parse(result)[j])
							standUnits.push(sourceObj.target);
						} else if (standVals[standIds.indexOf(sourceObj.ids[j])] != JSON.parse(result)[j] || standUnits[standIds.indexOf(sourceObj.ids[j])] != sourceObj.target) {
							standVals[standIds.indexOf(sourceObj.ids[j])] = JSON.parse(result)[j];
							standUnits[standIds.indexOf(sourceObj.ids[j])] = sourceObj.target;
						}
					}
				}.bind(this);
				
				Rutils.rJson('/library/kora.scripts/R/standardizeunits/json', args, callback);
			})(i);
		}
	},
	
	resetRecords: function () {
		firstListLoad = true;
		this.setState(this.getInitialState());
		
		this.loadLists();
		this.loadCollections();
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
	
	addTestRaster: function (polys, map) {
		
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
		for (var i = 0; i < this.state.records.features.length; i++) {
			if (workingSet.includes(this.state.records.features[i].id) && this.state.records.features[i].properties.datatype == 'meas') {
				x.push(this.state.records.features[i].geometry.coordinates[0]);
				y.push(this.state.records.features[i].geometry.coordinates[1]);
				z.push(standVals[standIds.indexOf(this.state.records.features[i].id)]);
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
			alpha: 0.5
		}
		let uploadcallback = function (result) {
			console.log(result);
		}.bind(this);
		
		let callback = function (imageDat) {
			var reader = new window.FileReader();
			reader.readAsDataURL(imageDat);
			reader.onloadend = function () {
				let base64data = reader.result;
				// let startInd = base64data.indexOf('data');
				// let endInd = base64data.indexOf('base64,') + 7;
				// let data = base64data.replace(base64data.substring(startInd,endInd),'');
				// Serverutils.add_media(this.props.guid, 'test.png', data, uploadcallback);
				
				let overlay = new google.maps.GroundOverlay(base64data, bounds);
				overlay.setMap(map);
			}.bind(this);
		}.bind(this)

		Rutils.idw(command, args, callback);
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
		
		this.setState({
			selectedPlace: selected,
			selectedMeasDist: vals,
			selectedMeasStand: selectedMeasStand,
			selectedMeasUnit: selectedMeasUnit
		});
	},
	
	componentWillMount: function () {
		this.loadLists();
		this.loadCollections();
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
		
		var dsp;
		if (this.state.drawingShape || this.state.pendingOverlay) {
			dsp = (
				<DrawingShapeInfoPanel 
				drawingShape={this.state.drawingShape}
				onStartDrawShape={this.onStartDrawShape}
				removePendingShape={this.removePendingShape}
				saveShape={this.saveShape}
				pendingOverlay={this.state.pendingOverlay}
				collections={{'6b94e72a-a47f-4dd5-9b8d-049324540ed2': 'My Shapes'}}/>
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
				onStartDrawShape={this.onStartDrawShape}
				drawingShape={this.state.drawingShape}
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
				selectedMeasDist={this.state.selectedMeasDist}
				standIds={standIds}
				standVals={standVals}
				drawingShape={this.state.drawingShape}
				onStartDrawShape={this.onStartDrawShape}
				showNewShapeDialog={this.showNewShapeDialog}
				addTestRaster={this.addTestRaster}
				/>
				{gfp}
				{dsp}
			</div>
		)
	}
});

export default (MapContainer);