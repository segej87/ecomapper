const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');
const AddDdn = require('./AddDdn').default;
const ServerUtils = require('../src/utils/Serverutils');

var SelectShapesPane = React.createClass({
	getInitialState: function () {
		return ({
			showingDdn: false,
			items: {}
		});
	},
	
	closeAdd: function () {
		this.setState({
			showingDdn: false,
			items: {}
		})
	},
	
	addShape: function (id) {
		let featToShow;
		for (var i = 0; i < this.state.shapeColl.features.length; i++) {
			if (this.state.shapeColl.features[i].id === id) {featToShow = this.state.shapeColl.features[i]}
		}
		
		if (featToShow) {
			let outShape = {type: 'FeatureCollection', features: [featToShow]};
			this.props.shapesLayer.showShape(outShape);
		}
	},
	
	onCollClick: function (e) {
		let clickedId = e.target.id;
		
		this.setState({showingDdn: true});
		
		
		let callback = function (result) {
			
			if (result.features) {
				let featNames = {}
				for (var i = 0; i < result.features.length; i++) {
					featNames[result.features[i].id] = result.features[i].properties.name;
				}
				
				this.setState({
					items: {shapes: featNames},
					shapeColl: result
				});
			} else {
				this.setState({
					items: {shapes: {}},
					shapeColl: result
				});
			}
			
		}.bind(this);
		ServerUtils.loadShapes(clickedId, callback);
	},
	
	render: function () {
		let shapes = this.props.shapesLayer.getShapes();
		let colls = Object.values(shapes).map((shape, i) => {
		return <li key={Object.keys(shapes)[i]} style={{marginBottom: 5}}><button id={Object.keys(shapes)[i]} style={{width: '100%', height: 30, marginLeft: 0, background: 'rgba(255,255,255,0.2)', cursor: 'pointer', border: 'none', color: 'white'}} onClick={this.onCollClick}>{shape}</button></li>;
		});
		
		if (this.state.showingDdn) {
			return (
				<div style={{marginLeft: 0}}>
				<ul style={{listStyle: 'none', marginLeft: 0, paddingLeft: 0}}>
					{colls}
				</ul>
				<AddDdn type='shapes' items={this.state.items} highlighted={true} onClose={this.closeAdd} onAdd={this.addShape}/>
			</div>
			);
		}
		
		return (
			<div style={{marginLeft: 0}}>
				<ul style={{listStyle: 'none', marginLeft: 0, paddingLeft: 0}}>
					{colls}
				</ul>
			</div>
		);
	}
});

export default (SelectShapesPane);