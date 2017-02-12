var React = require('react');
var navStyles = require('../styles/navStyles');
var NavbarItem = require('./NavbarItem');
var Values = require('../res/values');

var NavbarHomeSet = React.createClass({
	handleBack: function () {
		this.props.onBack();
	},
	
	handleClick: function (faded) {
		this.props.onClick(faded);
	},
	
	render: function () {
		return (
			<ul>
				<NavbarItem loggedIn={this.props.parentState.loggedIn} text="Back" onClick={this.handleBack} mapping={this.props.parentState.mapping} />
				<NavbarItem text={this.props.signInText} loggedIn={this.props.parentState.loggedIn} faded={this.props.parentState.faded} onClick={this.handleClick} mapping={this.props.parentState.mapping}/>
			</ul>
		);
	}
});

module.exports = NavbarHomeSet;