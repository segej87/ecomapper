React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');
FilterPane = require('./FilterPane');
MessagePane = require('./MessagePane');
ShapesPane = require('./ShapesPane');

var Sidebar = React.createClass({
	handleFilterChange: function (type, val, result) {
		this.props.onFilterChange(type, val, result);
	},
	
	onStartDrawShape: function (type) {
		this.props.onStartDrawShape(type);
	},
	
	render: function () {
		return (
			<div>
				<FilterPane 
				filters={this.props.filters}
				lists={this.props.lists}
				onFilterChange={this.handleFilterChange}
				shapesLayer={this.props.shapesLayer}
				toggleGeoFilter={this.props.toggleGeoFilter}
				/>
				<MessagePane 
				appState={this.props.appState}
				selectedPlace={this.props.selectedPlace} 
				filters={this.props.filters}
				handleDelete={this.props.handleDelete}
				selectedMeasDist={this.props.selectedMeasDist}
				selectedMeasStand={this.props.selectedMeasStand}
				selectedMeasUnit={this.props.selectedMeasUnit}
				/>
				<ShapesPane
				onStartDrawShape={this.onStartDrawShape}
				drawingShape={this.props.drawingShape}
				/>
			</div>
		);
	}
});

module.exports = Sidebar;