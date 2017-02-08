var React = require('react');
var navStyles = require('../styles/navStyles');
var NavbarDropdownItem = require('./NavbarDropdownItem')

var NavbarDropdown = React.createClass({
	items: [],
	
	getInitialState: function () {
		return {highlighted: false};
	},
	
	handleMouseEnter: function () {
		this.setState({highlighted: true});
	},
	
	handleMouseLeave: function () {
		this.setState({highlighted: false});
	},
	
	componentWillMount: function () {
		this.items = [];
		for (var i = 0; i < this.props.items.length; i++) {
			this.items.push(<NavbarDropdownItem link={this.props.items[i].link} name={this.props.items[i].name} onClick={this.handleMouseLeave} key={i} />);
		}
	},
	
	render: function () {
		var linkStyle;
		
		if (this.props.highlighted || this.state.highlighted) {
			linkStyle = navStyles.dropdownContent
		} else {
			linkStyle = navStyles.hidden
		}
		
		return (
			<div style={navStyles.dropdown} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
				<div style={linkStyle}>
					{this.items}
				</div>
			</div>
		);
	}
});

module.exports = NavbarDropdown;