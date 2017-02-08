var React = require('react');
var NavbarDropdown = require('./NavbarDropdown');
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
		var dropdownLine;
		var linkLine;
		
		if (this.state.highlighted) {
			linkStyle = navStyles.a.highlighted
		} else {
			linkStyle = navStyles.a
		}
		
		if ('dropdown' in this.props && this.props.dropdown != null) {
			dropdownLine = <NavbarDropdown highlighted={this.state.highlighted} items={this.props.dropdown}/>;
		}
		
		if ('onClick' in this.props && typeof this.props.onClick == 'function') {
			linkLine = (
				<a style={linkStyle} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave} onClick={this.props.onClick}>
					{this.props.text}
				</a>
			);
		} else {
			linkLine = (
				<a style={linkStyle} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
					{this.props.text}
				</a>
			);
		}
		
		return (
			<li style={navStyles.li}>
				{linkLine}
				{dropdownLine}
			</li>
		);
	}
});

module.exports = NavbarItem;