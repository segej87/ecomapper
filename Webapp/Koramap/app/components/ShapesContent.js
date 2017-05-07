const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');
const DrawShapesPane = require('./DrawShapesPane').default;
const SelectShapesPane = require('./SelectShapesPane').default;

var ShapesContent = React.createClass({
	getInitialState: function () {
		return ({
			
		});
	},
	
	onStartDrawShape: function (type) {
		this.props.onStartDrawShape(type);
	},
	
	render: function () {
		let secondHeader = "Shape collections"
		if (this.props.drawingShape) {
			secondHeader = "Drawing " + this.props.drawingShape;
		}
		
		return (
			<div>
				<p style={{borderBottom: '1px solid white', fontSize: 14, fontFamily: 'Lato, Open Sans, sans-serif', paddingBottom: 3, paddingLeft: 5, color: 'white'}}>Draw new shape</p>
				<DrawShapesPane onStartDraw={this.onStartDrawShape}/>
				<p style={{borderBottom: '1px solid white', fontSize: 14, fontFamily: 'Lato, Open Sans, sans-serif', paddingBottom: 3, paddingLeft: 5, color: 'white', marginTop: 30}}>{secondHeader}</p>
				<SelectShapesPane shapesLayer={this.props.shapesLayer}/>
			</div>
		);
	}
});

export default (ShapesContent);