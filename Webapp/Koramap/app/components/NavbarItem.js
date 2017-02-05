var React = require('react');
var NavbarDropdown =require('./NavbarDropdown');
var navStyles = require('../styles/navStyles');

var NavbarItem = React.createClass({
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
		var faded = !this.props.faded;
		this.props.onClick(faded);
	},
		
	render: function () {
		var linkStyle;
		
		if (this.state.highlighted) {
			linkStyle = navStyles.a.highlighted
		} else {
			linkStyle = navStyles.a
		}
		
		var output;
		
		if (this.props.text == "Other maps") {
			output = (<li style={navStyles.li}>
			<a style={linkStyle} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
				{this.props.text}
			</a>
			<NavbarDropdown highlighted={this.state.highlighted}/>
			</li>);
		} else {
			output = (<li style={navStyles.li}>
			<a style={linkStyle} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave} onClick={this.handleClick}>
				{this.props.text}
			</a>
			</li>);
		}
		
		return output;
	}
});

module.exports = NavbarItem;