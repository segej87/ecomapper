const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');

var ShapesContent = React.createClass({
	getInitialState: function () {
		return ({
			
		});
	},
	
	onHover: function (e) {
		this.setState({
			hovered: e.target.id
		});
	},
	
	onLeave: function (e) {
		this.setState({
			hovered: null
		});
	},
	
	onClick: function (e) {
		this.props.onStartDraw(e.target.id.replace("Button",""));
	},
	
	renderChildren() {
		let opts = ['PolygonButton','PolylineButton'];
		
		return opts.map((o) => {
			let style = SidebarStyles.shapeButton;
			if (this.state.hovered == o) {
				style = SidebarStyles.shapeButton.hovered
			}
			return <img key={o} src={require('../res/img/buttons/' + o + '.png')} width="40px" height="40px" style={style} id={o} onMouseEnter={this.onHover} onMouseLeave={this.onLeave} onClick={this.onClick}/>;
		});
	},
	
	render: function () {
		return (
			<div style={{textAlign: 'center', marginTop: 25}}>
				{this.renderChildren()}
			</div>
		);
	}
});

export default (ShapesContent);