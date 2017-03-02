React = require('react');
let Sidebar = require('./Sidebar');
let Container = require('./Container').default;
let Values = require('../res/values');
let mapStyles = require('../styles/map/mapStyles');
let mainStyles = require('../styles/home/mainStyles');
import TimerMixin from 'react-timer-mixin';

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
			firstListLoad: true,
			lists: {
				datatype: ['Meas','Photo','Note'],
				submitters: [],
				access: ['Public','Private'],
				tags: [],
				species: [],
				date: ['none', 'none']
			},
			filters: {
				datatype: ['Meas','Photo','Note'],
				submitters: [],
				access: ['Public','Private'],
				tags: [],
				species: [],
				date: ['none', 'none']
			},
			records: {},
			selectedPlace: {}
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
		}
		
		var newFilters = {};
		for (var i = 0; i < Object.keys(this.state.filters).length; i++) {
			newFilters[Object.keys(this.state.filters)[i]] = Object.values(this.state.filters)[i];
		}
		if (newItems) {
			newFilters[type] = newItems;
		}
		
		this.setState({
			filters: newFilters
		});
		
		this.loadRecords(newFilters);
	},
	
	loadLists: function () {
		if (this.props.userInfo.userId != null && !this.props.offline) {
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
							datatype: this.state.filters.datatype,
							submitters: submittersArray,
							access: accessArray,
							tags: tagsArray,
							species: speciesArray,
							date: this.state.filters.date
						}
					});
					
					if (this.state.firstListLoad) {
						this.setState({
							filters: {
								datatype: this.state.filters.datatype,
								submitters: submittersArray,
								access: accessArray,
								tags: tagsArray,
								species: speciesArray,
								date: this.state.filters.date
							},
							firstListLoad: false
						});
						
						this.loadRecords();
					}
				} else {
					console.log('Status: ' + request.status);
					console.log('Status text: ' + request.statusText);
				}
			};

			request.open(method, url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
		}
	},
	
	loadRecords: function (filters = this.state.filters) {
		if (this.props.userInfo.userId != null && !this.props.offline) {
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
					const geoJsonIn = JSON.parse(request.responseText);
					
					if (Object.keys(geoJsonIn).includes('text') && geoJsonIn.text == "Warning: geojson not found") {
						if (Object.keys(this.state.records).length > 0){
							this.setState({
								records: {}
							});
						}
					} else {
						if (Object.keys(this.state.records).length == 0) {
							console.log('Setting initial data');
							this.setState({
								records: geoJsonIn
							});
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
								
								let newRecords = {type: currentRecords.type, features: currentFeats};
								
								this.setState({
									records: newRecords
								});
							}
						}
					}
				} else {
					console.log('Status: ' + request.status);
					console.log('Status text: ' + request.statusText);
				}
			};

			request.open(method, url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
		}
	},
	
	resetRecords: function () {
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
	
	handleSelectedPlace: function (selected) {
		this.setState({
			selectedPlace: selected
		});
	},
	
	componentWillMount: function () {
		this.loadLists();
	},
  
	componentDidMount: function () {
		this.setInterval(
			() => { this.loadRecords(); },
			30000
		);
	},
	
	render: function () {
		return (
			<div style={mapStyles.main}>
				<Sidebar 
				userInfo={this.props.userInfo} 
				selectedPlace={this.state.selectedPlace} 
				filters={this.state.filters}
				lists={this.state.lists}
				handleDelete={this.handleDelete}
				onFilterChange={this.handleFilterChange}
				/>
				<Container 
				offline={this.props.offline}
				userInfo={this.props.userInfo} 
				loadRecords={this.loadRecords} 
				records={this.state.records} 
				filters={this.state.filters} 
				handleSelected={this.handleSelectedPlace}
				resetRecords={this.resetRecords}
				/>
			</div>
		)
	}
});

export default (MapContainer);