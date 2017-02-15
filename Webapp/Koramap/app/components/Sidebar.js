React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');
FilterPane = require('./FilterPane');
MessagePane = require('./MessagePane');

var Sidebar = React.createClass({
	render: function () {
		return (
			<div>
				<MessagePane 
				userInfo={this.props.userInfo} 
				selectedPlace={this.props.selectedPlace} 
				filters={this.props.filters}
				loadRecords={this.props.loadRecords}
				handleDelete={this.props.handleDelete}
				/>
				<FilterPane 
				filters={this.props.filters}
				/>
			</div>
		);
	}
});

module.exports = Sidebar;