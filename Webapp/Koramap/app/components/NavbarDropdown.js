var React = require('react');
var navStyles = require('../styles/navStyles');
var NavbarDropdownItem = require('./NavbarDropdownItem')

var NavbarDropdown = React.createClass({
	getInitialState: function () {
		return {highlighted: false};
	},
	
	handleMouseEnter: function () {
		this.setState({highlighted: true});
	},
	
	handleMouseLeave: function () {
		this.setState({highlighted: false});
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
				<NavbarDropdownItem link="http://my-observatory.com/" name="myObservatory" onClick={this.handleMouseLeave}/>
				<NavbarDropdownItem link="http://inaturalist.org/" name="iNaturalist" onClick={this.handleMouseLeave}/>
				<NavbarDropdownItem link="http://wikimapia.org/" name="wikimapia" onClick={this.handleMouseLeave}/>
			</div>
			</div>
		);
	}
});

module.exports = NavbarDropdown;