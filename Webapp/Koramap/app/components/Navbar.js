var React = require('react');
var navStyles = require('../styles/navStyles');
var Logo = require('./Logo');
var NavbarItem = require('./NavbarItem');
var Values = require('../res/values');

var Navbar = React.createClass({
	barItems: ["Learn more", "Other maps"],
	
	handleClick: function (faded) {
		this.props.onClick(faded);
	},
	
	render: function() {
		var signInText;
		
		if (this.props.loggedIn && this.props.userInfo.firstName != null) {
			signInText = "Hi " + this.props.userInfo.firstName
		} else {
			signInText = Values.strings.login
		}
		
		return (
		<div style={navStyles.navbar}>
			<Logo style={navStyles.logo} />
			<ul style={navStyles.ul}>
				<NavbarItem text="Learn more" loggedIn={this.props.loggedIn} />
				<NavbarItem text="Other maps" loggedIn={this.props.loggedIn} />
				<NavbarItem text={signInText} loggedIn={this.props.loggedIn} faded={this.props.faded} onClick={this.handleClick} />
			</ul>
		</div>
		);
	}
});

module.exports = Navbar;