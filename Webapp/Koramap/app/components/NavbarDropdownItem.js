var React = require('react');
var navStyles = require('../styles/navStyles');

var NavbarDropdownItem = React.createClass({
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
	
	render: function () {
		var linkStyle;
		
		if (this.state.highlighted) {
			linkStyle = navStyles.dropdownContent.a.highlighted
		} else {
			linkStyle = navStyles.dropdownContent.a
		}
		
		return <a href={this.props.link} target="_blank" style={linkStyle} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave} onClick={this.props.onClick}>{this.props.name}</a>;
	}
});

module.exports = NavbarDropdownItem;