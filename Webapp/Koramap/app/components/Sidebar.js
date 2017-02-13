React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');
FilterPane = require('./FilterPane');
MessagePane = require('./MessagePane');

var Sidebar = React.createClass({
	render: function () {
		return (
			<div>
				<MessagePane />
				<FilterPane />
			</div>
		);
	}
});

module.exports = Sidebar;