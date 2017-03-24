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
	
	handleClick: function (e) {
		this.props.onStartDrawShape(null);
		if (e.target.id == 'saveButton') {
			this.props.saveShape({name: document.getElementById('namefield').value}, document.getElementById('collfield').value)
		} else if (e.target.id == 'cancelButton') {
			this.props.removePendingShape();
		}
	},
	
	render: function () {
		let collOpts = Object.keys(this.props.collections).map((c, i) => {
			return (<option value={c} key={c}>{this.props.collections[c]}</option>);
		});
		
		let show;
		if (this.props.pendingOverlay) {
			show = (
			<div style={{width: '90%', textAlign: 'center'}}>
				<p style={{fontSize: 12}}>Give this shape a name</p>
				<input style={{width: '100%'}} id='namefield'/>
				<p style={{fontSize: 12}}>Choose a collection for this shape:</p>
				<select id='collfield' style={{width: '100%'}}>
					{collOpts}
				</select>
				<button style={SidebarStyles.deleteButton} onClick={this.handleClick} id='saveButton'>Save</button>
				<button style={SidebarStyles.deleteButton} onClick={this.handleClick} id='cancelButton'>Cancel</button>
			</div>
			);
		} else {
			show = (
			<div>
				<p id='drawshapeheader' style={{fontWeight: 'bold'}}>{"Drawing " + this.props.drawingShape}</p>
				<p id='drawingshapeinfo'></p>
				<button style={SidebarStyles.deleteButton} onClick={this.handleClick}>Cancel</button>
			</div>
			);
		}
		
		return (
		<Draggable
			zIndex={10000000000}
			onStart={this.dragStart}
			onDrag={this.handleDrag}
			onStop={this.dragStop}>
				<div style={SidebarStyles.geoInfo}>
					{show}
				</div>
			</Draggable>
		);
	}
});


module.exports = GeoFilterInfoPanel;