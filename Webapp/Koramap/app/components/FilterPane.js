React = require('react');
SidebarToggle = require('./SidebarToggle');
FilterContent = require('./FilterContent').default;
GeoFilterButton = require('./GeoFilterButton');
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
	
	handleFilterChange: function (type, val, result) {
		this.props.onFilterChange(type, val, result);
	},
	
	toggleGeoFilter: function (result) {
		this.props.toggleGeoFilter(result);
	},
	
	render: function () {
		if (this.state.open) {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<div style={SidebarStyles.sidebarOpen.filter}>
						<FilterContent filters={this.props.filters} lists={this.props.lists} onFilterChange={this.handleFilterChange}/>
						<GeoFilterButton shapes={this.props.shapes} toggleGeoFilter={this.toggleGeoFilter}/>
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