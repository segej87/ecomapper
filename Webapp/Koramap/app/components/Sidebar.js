React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');
FilterPane = require('./FilterPane');

var Sidebar = React.createClass({
	render: function () {
		return (
			<div style={SidebarStyles.sidebar}>
				<FilterPane open={false} />
			</div>
		);
	}
});

module.exports = Sidebar;