React = require('react');
SidebarToggle = require('./SidebarToggle');
SidebarStyles = require('../styles/map/sidebarStyles');

var FilterPane = React.createClass({
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
					<div style={SidebarStyles.sidebarOpen.filter}>
						<h1 style={{color: 'white'}}>Filtering</h1>
					</div>
					<SidebarToggle type="filter" onClick={this.openChange} open={this.state.open} />
				</div>
			);
		} else {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<SidebarToggle type="filter" onClick = {this.openChange} open={this.state.open} />
				</div>
			);
		}
	}
});

module.exports = FilterPane;