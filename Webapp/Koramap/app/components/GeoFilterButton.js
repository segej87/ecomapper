const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');
const AddDdn = require('./AddDdn').default;

const GeoFilterButton = React.createClass({
	getInitialState: function () {
		return ({
			filteringBy: null,
			highlighted: false,
			showingDdn: false
		});
	},
	
	handleMouseEnter: function () {
		this.setState({highlighted: true});
	},
	
	handleMouseLeave: function () {
		this.setState({highlighted: false});
	},
	
	handleClick: function () {
		this.setState({showingDdn: !this.state.showingDdn});
	},
	
	closeAdd: function () {
		this.setState({
			showingDdn: false
		});
	},
	
	toggleGeoFilter: function (item) {
		this.setState({
				filteringBy: item,
				showingDdn: false
			});
		this.props.toggleGeoFilter(item);
	},
	
	render: function () {
		var filterText = 'Geographic filter';
		if (this.state.filteringBy) {
			filterText = 'Filtering by ' + this.state.filteringBy;
		}
		
		var style = SidebarStyles.geoButton;
		if (this.state.highlighted) {
			style = SidebarStyles.geoButton.highlighted;
		}
		
		var pointer = null;
		if (this.state.showingDdn) {
			pointer = <p style={SidebarStyles.geoButton.pointer}>&#9664;</p>
		}
		
		return (
			<div>
				<AddDdn items={{Geo: this.props.shapes}} type={'Geo'} highlighted={this.state.showingDdn} onClose={this.closeAdd} onAdd={this.toggleGeoFilter}/>
				<div style={{display: 'inline-block', position: 'relative', float: 'right', width: '100%'}}>
					<button style={style} onClick={this.handleClick} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>{filterText}</button>
					{pointer}
				</div>
			</div>
		);
	}
});

module.exports = GeoFilterButton;