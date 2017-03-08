const React = require('react')
const SidebarStyles = require('../styles/map/sidebarStyles');
let Draggable = require('react-draggable');

const GeoFilterInfoPanel = React.createClass({
	getInitialState: function () {
		return ({
			selectedItem: null,
			activeDrags: 0,
			deltaPosition: {
				x: 0,
				y: 0
			}
		});
	},
	
	handleDrag: function (e, ui) {
		const {x, y} = this.state.deltaPosition;
		this.setState({
			deltaPosition: {
				x: x + ui.deltaX,
				y: y + ui.deltaY
			}
		});
	},
	
	dragStart: function () {
		this.setState({activeDrags: ++this.state.activeDrags});
	},
	
	dragStop: function () {
		this.setState({activeDrags: --this.state.activeDrags});
	},
	
	handleConfirm: function () {
		this.props.toggleGeoFilter(this.props.geoFiltering);
	},
	
	render: function () {
		var header = '';
		var selected = '';
		if (this.props.selectedGeo) {
			header = 'Currently selected:'
			selected = this.props.selectedGeo.join(', ');
		}
		
		return (
		<Draggable
			zIndex={10000000000}
			onStart={this.dragStart}
			onDrag={this.handleDrag}
			onStop={this.dragStop}>
				<div style={SidebarStyles.geoInfo}>
					<p>{header}</p>
					<p>{selected}</p>
					<button style={SidebarStyles.deleteButton} onClick={this.handleConfirm}>Filter</button>
				</div>
			</Draggable>
		);
	}
});



module.exports = GeoFilterInfoPanel;