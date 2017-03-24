React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');

var SidebarToggle = React.createClass({
	handleClick: function () {
		this.props.onClick(!this.props.open);
	},
	
	render: function () {
		var style;
		
		switch (this.props.type) {
			case "filter":
				style = SidebarStyles.toggle.filter;
				break;
			case "message":
				style = SidebarStyles.toggle.message;
				break;
			case "shapes":
				style = SidebarStyles.toggle.shapes;
				break;
			default:
				style = SidebarStyles.toggle.filter;
				break;
		}
		
		if (this.props.open) {
			return <button style={style} onClick={this.handleClick}>{"<"}</button>
		} else {
			return <button style={style} onClick={this.handleClick}>{">"}</button>	
		}
	}
});

module.exports = SidebarToggle;