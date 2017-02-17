React = require('react');
Sidebar = require('./Sidebar');
Container = require('./Container').default;
Values = require('../res/values');
mapStyles = require('../styles/map/mapStyles');
mainStyles = require('../styles/home/mainStyles');

var MapContainer = React.createClass({
	getInitialState: function () {
		var today = new Date();
		var monthago = new Date();
		monthago.setDate(today.getDate() - 30);
		monthago.setHours(0);
		monthago.setMinutes(0);
		monthago.setSeconds(0);
		monthago.setMilliseconds(0);
		
		return {
			filters: {
				access: ['test'],
				tags: ['test', 'test2','test3','test4','test5','test6'],
				date: ['none', 'none']
			},
			records: {},
			selectedPlace: {}
		};
	},
	
	loadRecords: function () {
		if (this.props.userInfo.userId != null && !this.props.offline) {
			console.log('Loading data');
			const formData='GUID=' + this.props.userInfo.userId + '&filters=' + JSON.stringify(this.state.filters);
		
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
						
						console.log(Object.keys(this.state.records).length);
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
							
							var currentIds = [];
							for (var i = 0; i < currentFeats.length; i++) {
								currentIds.push(currentFeats[i].id);
							}
							
							for (var i = 0; i < geoJsonIn.features.length; i++) {
								if (!currentIds.includes(geoJsonIn.features[i].id)) {
									console.log('Adding feature');
									currentFeats.push(geoJsonIn.features[i]);
								}
							}
							
							newRecords = {type: currentRecords.type, features: currentRecords.features};
							
							this.setState({
								records: newRecords
							});
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
		this.setState({
			records: {}
		});
		
		this.loadRecords();
	},
	
	handleDelete: function (id) {
		currentRecords = this.state.records;
		currentFeatures = currentRecords.features;
		
		for (i = 0; i < currentFeatures.length; i++) {
			if (currentFeatures[i].id == id) {
				currentFeatures.splice(i, i);
			}
		}
		
		newRecords = {type: currentRecords.type, features: currentFeatures};
		
		this.setState({
			records: newRecords,
			selectedPlace: {}
		});
	},
	
	handleFilterChange: function (filters) {
		this.setState({
			filters: filters
		});
	},
	
	handleSelectedPlace: function (selected) {
		this.setState({
			selectedPlace: selected
		});
	},
	
	render: function () {
		return (
			<div style={mapStyles.main}>
				<Sidebar 
				userInfo={this.props.userInfo} 
				loadRecords={this.loadRecords} 
				selectedPlace={this.state.selectedPlace} 
				filters={this.state.filters}
				handleDelete={this.handleDelete}
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

module.exports = MapContainer;