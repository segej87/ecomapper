var React = require('react');
var navStyles = require('../styles/navStyles');
var NavbarItem = require('./NavbarItem');
var Values = require('../res/values');

var NavbarHomeSet = React.createClass({
	otherMapsDropdown: 
	[
		{link: 'http://inaturalist.org/', name: 'iNaturalist'},
		{link: 'http://wikimapia.org', name: 'wikimapia'}
	],
	
	handleClick: function (faded) {
		this.props.onClick(faded);
	},
	
	render: function () {
		return (
			<ul>
				<NavbarItem loggedIn={this.props.parentState.loggedIn} text="Learn more" />
				<NavbarItem loggedIn={this.props.parentState.loggedIn} text="Other maps" dropdown={this.otherMapsDropdown}/>
				<NavbarItem text={this.props.signInText} loggedIn={this.props.parentState.loggedIn} faded={this.props.parentState.faded} onClick={this.handleClick} />
			</ul>
		);
	}
});

module.exports = NavbarHomeSet;