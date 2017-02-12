React = require('react');
Sidebar = require('./Sidebar');
Container = require('./Container').default;
Values = require('../res/values');
mapStyles = require('../styles/map/mapStyles');
mainStyles = require('../styles/home/mainStyles');

var MapContainer = React.createClass({
	getInitialState: function () {
		return {
			openMenu: null
		};
	},
	
	render: function () {
		return (
			<div style={mapStyles.main}>
				<Sidebar />
				<Container />
			</div>
		)
	}
});

module.exports = MapContainer;