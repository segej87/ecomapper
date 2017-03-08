React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');
FilterPane = require('./FilterPane');
MessagePane = require('./MessagePane');

var Sidebar = React.createClass({
	handleFilterChange: function (type, val, result) {
		this.props.onFilterChange(type, val, result);
	},
	
	render: function () {
		// console.log(this.props.lists);
		
		return (
			<div>
				<MessagePane 
				userInfo={this.props.userInfo} 
				selectedPlace={this.props.selectedPlace} 
				filters={this.props.filters}
				handleDelete={this.props.handleDelete}
				/>
				<FilterPane 
				filters={this.props.filters}
				lists={this.props.lists}
				onFilterChange={this.handleFilterChange}
				shapes={this.props.shapes}
				toggleGeoFilter={this.props.toggleGeoFilter}
				/>
			</div>
		);
	}
});

module.exports = Sidebar;