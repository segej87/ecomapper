let values = require('../res/values');

class AppState {
	constructor () {
		this.map = null;
		this.userInfo = values.standards.login;
		this.records = {};
		this.standIds = [];
		this.standVals = [];
		this.standUnits = [];
		this.workingSet = [];
		this.measDist = [];
	}
	
	
	//Map
	setMap (map) {
		this.map = map;
	}
	
	getMap() {
		return this.map;
	}
	
	//User info
	setUserInfo (userInfo) {
		this.userInfo = userInfo;
	}
	
	getUserInfo () { 
		return this.userInfo;
	}
	
	//Records
	setRecords (records) {
		this.records = records;
	}
	
	getRecords () {
		return this.records;
	}
	
	//Standardized Ids
	setStandIds (standIds) {
		this.standIds = standIds;
	}
	
	getStandIds () {
		return this.standIds;
	}
	
	addStandId (standId) {
		this.standIds.push(standId);
	}
	
	spliceStandId (index, i) {
		this.standIds.splice(index, i);
	}
	
	//Standardized values
	setStandVals (standVals) {
		this.standVals = standVals;
	}
	
	getStandVals () {
		return this.standVals;
	}
	
	addStandVal (standVal) {
		this.standVals.push(standVal);
	}
	
	spliceStandVal (index, i) {
		this.standVals.splice(index, i);
	}
	
	//Standardized units
	setStandUnits (standUnits) {
		this.standUnits = standUnits;
	}
	
	getStandUnits () {
		return this.standUnits;
	}
	
	addStandUnit (standUnit) {
		this.standUnits.push(standUnit);
	}
	
	spliceStandUnit (index, i) {
		this.standUnits.splice(index, i);
	}
	
	//Working set
	setWorkingSet (workingSet) {
		this.workingSet = workingSet;
	}
	
	getWorkingSet () {
		return this.workingSet;
	}
	
	addWorkingSet (workingSet) {
		this.workingSet.push(workingSet);
	}
	
	spliceWorkingSet (index, i) {
		this.workingSet.splice(index, i);
	}
	
	setMeasDist (type) {
		if (this.records.features) {
			var ids = [];
			var vals = [];
			for (var i = 0; i < this.records.features.length; i++) {
				if ((this.getWorkingSet().length == 0 || this.getWorkingSet().includes(this.records.features[i].id)) && this.records.features[i].properties.species == type) {
					ids.push(this.records.features[i].id);
				}
			}
			
			for (var i = 0; i < ids.length; i++) {
				vals.push(this.getStandVals()[this.getStandIds().indexOf(ids[i])]);
			}
			
			this.measDist = vals;
		}
	}
	
	getMeasDist () {
		return this.measDist;
	}
}

module.exports = AppState;