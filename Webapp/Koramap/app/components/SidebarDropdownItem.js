var React = require('react');
var SidebarStyles = require('../styles/map/sidebarStyles');

var SidebarDropdownItem = React.createClass({
	getInitialState: function () {
		return {highlighted: false};
	},
		
	handleMouseEnter: function () {
		this.setState(
			{highlighted: true}
		);
	},
	
	handleMouseLeave: function () {
		this.setState(
			{highlighted: false}
		);
	},
	
	handleClick: function () {
		this.props.onClick(this.props.name);
	},
	
	render: function () {
		var linkStyle;
		
		if (this.state.highlighted) {
			linkStyle = SidebarStyles.dropdownContent.a.highlighted
		} else {
			linkStyle = SidebarStyles.dropdownContent.a
		}
		
		return <a style={linkStyle} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave} onClick={this.handleClick}>{this.props.name}</a>;
	}
});

module.exports = SidebarDropdownItem;