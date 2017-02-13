React = require('react');
SidebarToggle = require('./SidebarToggle');
SidebarStyles = require('../styles/map/sidebarStyles');

var MessagePane = React.createClass({
	getInitialState: function () {
		return {
			open: false
		};
	},
	
	openChange: function (result) {
		this.setState({
			open: result
		});
	},
	
	render: function () {
		if (this.state.open) {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<div style={SidebarStyles.sidebarOpen.message}></div>
					<SidebarToggle type="message" onClick = {this.openChange} open={this.state.open} />
				</div>
			);
		} else {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<SidebarToggle type="message" onClick = {this.openChange} open={this.state.open} />
				</div>
			);
		}
	}
});

module.exports = MessagePane;