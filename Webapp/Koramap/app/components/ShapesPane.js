React = require('react');
SidebarToggle = require('./SidebarToggle');
ShapesContent = require('./ShapesContent').default;
SidebarStyles = require('../styles/map/sidebarStyles');

var ShapesPane = React.createClass({
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
	
	onStartDrawShape: function (type) {
		this.props.onStartDrawShape(type);
	},
	
	render: function () {
		if (this.state.open) {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<div style={SidebarStyles.sidebarOpen.shapes}>
						<ShapesContent shapesLayer={this.props.shapesLayer} drawingShape={this.props.drawingShape} onStartDrawShape={this.onStartDrawShape} drawingShape={this.props.drawingShape}/>
					</div>
					<SidebarToggle type="shapes" onClick={this.openChange} open={this.state.open} />
				</div>
			);
		} else {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<SidebarToggle type="shapes" onClick = {this.openChange} open={this.state.open} />
				</div>
			);
		}
	}
});

module.exports = ShapesPane;