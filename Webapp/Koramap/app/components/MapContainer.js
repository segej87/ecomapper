let React = require('react');
let Sidebar = require('./Sidebar');
let Container = require('./Container').default;
let GeoFilterInfoPanel = require('./GeoFilterInfoPanel');
let DrawingShapeInfoPanel = require('./DrawingShapeInfoPanel');
let ShapesLayer = require('./ShapesLayer').default;
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

let appState;
let shapesLayer;

var MapContainer = React.createClass({
	mixins: [TimerMixin],
	firstListLoad: true,
	loadingLists: false,
	loadingData: false,
	
	// Set default state values
	getInitialState: function () {
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
				date: this.getInitialDates()
			},
			records: {},
			measObj: {},
			selectedPlace: {},
			geoFiltering: null,
			drawingShape: null,
			pendingOverlay: false
		};
	},
	
	getInitialDates: function () {
		var today = new Date();
		var monthago = new Date();
		monthago.setDate(today.getDate() - 30);
		monthago.setHours(0);
		monthago.setMinutes(0);
		monthago.setSeconds(0);
		monthago.setMilliseconds(0);
		
		return [monthago, today];
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
		if (newFilters.species.length == 1) {
			appState.setMeasDist(newFilters.species[0]);
		}
		
		this.setState({
			filters: newFilters,
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
		
		Serverutils.saveShape(appState.getUserInfo().userId, outShape, collection, callback);
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
		if (appState.getUserInfo().userId != null && !this.loadingLists) {
			this.loadingLists = true;
			
			let callback = function (result, success) {
				if (success) {
					this.setState(result)
				
					if (this.firstListLoad) {
						this.firstListLoad = false;
						this.loadRecords();
					}
				}
				
				this.loadingLists = false;
			}.bind(this);
			
			Serverutils.loadLists(appState.getUserInfo().userId, this.state.lists, this.state.filters, callback);
		}
	},
	
	loadRecords: function (filters = this.state.filters, override = false) {
		if (appState.getUserInfo().userId != null && !this.state.geoFiltering && !this.state.drawingShape && (!this.loadingData || override)) {
			this.loadingData = true;
			
			let callback = function (newState, newIds, success) {
				if (success) {
					appState.setRecords(newState.records);
					
					this.setState(newState);
				
					for (var i = appState.getStandIds().length-1; i >=0; i--) {
						if (!newIds.includes(appState.getStandIds()[i])) {
							appState.spliceStandId(i, 1);
							appState.spliceStandVal(i, 1);
							appState.spliceStandUnit(i, 1);
						}
					}
					
					this.standardizeUnits(newState.measObj);
				}
				
				this.loadingData = false;
			}.bind(this);
			
			Serverutils.loadRecords(appState.getUserInfo().userId, filters, this.state.records, override, callback);
		}
	},
	
	setWorkingSet: function (ids) {
		appState.setWorkingSet(ids);
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
						if (!appState.getStandIds().includes(sourceObj.ids[j])) {
							appState.addStandId(sourceObj.ids[j]);
							appState.addStandVal(JSON.parse(result)[j])
							appState.addStandUnit(sourceObj.target);
						} else if (appState.getStandVals()[appState.getStandIds().indexOf(sourceObj.ids[j])] != JSON.parse(result)[j] || appState.getStandUnits()[appState.getStandIds().indexOf(sourceObj.ids[j])] != sourceObj.target) {
							appState.getStandVals()[appState.getStandIds().indexOf(sourceObj.ids[j])] = JSON.parse(result)[j];
							appState.getStandUnits()[appState.getStandIds().indexOf(sourceObj.ids[j])] = sourceObj.target;
						}
					}
				}.bind(this);
				
				Rutils.rJson('/library/kora.scripts/R/standardizeunits/json', args, callback);
			})(i);
		}
	},
	
	resetRecords: function () {
		this.firstListLoad = true;
		this.setState(this.getInitialState());
		
		this.loadLists();
		shapesLayer.loadCollections();
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
		
		appState.setRecords(newRecords);
		
		this.setState({
			records: newRecords,
			selectedPlace: {}
		});
	},
	
	handleSelectedPlace: function (selected) {
		var selectedMeasStand;
		var selectedMeasUnit;
		
		if (selected.featureProps) {
			if (selected.featureProps.datatype == 'meas') {
				const ind = appState.getStandIds().indexOf(selected.fuid);
				appState.setMeasDist(selected.featureProps.species[0]);
				selectedMeasStand = appState.getStandVals()[ind];
				selectedMeasUnit = appState.getStandUnits()[ind];
			}
		}
		
		this.setState({
			selectedPlace: selected,
			selectedMeasStand: selectedMeasStand,
			selectedMeasUnit: selectedMeasUnit
		});
	},
	
	componentWillMount: function () {
		appState = this.props.appState;
		shapesLayer = new ShapesLayer(appState);
		this.loadLists();
		shapesLayer.loadCollections();
	},
  
	componentDidMount: function () {
		this.firstListLoad = true;
		
		this.setInterval(
			() => { this.loadRecords(); },
			30000
		);
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (appState.getUserInfo().userId && nextProps.appState.getUserInfo().userId != appState.getUserInfo().userId) {
			appState.setUserInfo(nextProps.appState.getUserInfo());
		}
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
				shapesLayer={shapesLayer}
				drawingShape={this.state.drawingShape}
				onStartDrawShape={this.onStartDrawShape}
				removePendingShape={this.removePendingShape}
				saveShape={this.saveShape}
				pendingOverlay={this.state.pendingOverlay}/>
			);
		}
		
		return (
			<div style={mapStyles.main}>
				<Sidebar 
				appState={appState}
				shapesLayer={shapesLayer}
				userInfo={appState.getUserInfo()} 
				selectedPlace={this.state.selectedPlace} 
				selectedMeasStand={this.state.selectedMeasStand}
				selectedMeasUnit={this.state.selectedMeasUnit}
				filters={this.state.filters}
				lists={this.state.lists}
				handleDelete={this.handleDelete}
				onFilterChange={this.handleFilterChange}
				onStartDrawShape={this.onStartDrawShape}
				drawingShape={this.state.drawingShape}
				toggleGeoFilter={this.toggleGeoFilter}
				/>
				<Container 
				appState={appState}
				shapesLayer={shapesLayer}
				userInfo={appState.getUserInfo()} 
				geoFiltering={this.state.geoFiltering}
				records={this.state.records} 
				filters={this.state.filters} 
				handleSelected={this.handleSelectedPlace}
				resetRecords={this.resetRecords}
				setWorkingSet={this.setWorkingSet}
				drawingShape={this.state.drawingShape}
				onStartDrawShape={this.onStartDrawShape}
				showNewShapeDialog={this.showNewShapeDialog}
				/>
				{gfp}
				{dsp}
			</div>
		)
	}
});

export default (MapContainer);