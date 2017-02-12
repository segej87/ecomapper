var React = require('react');
var NavbarHomeSet = require('./NavbarHomeSet');
var NavbarMapSet = require('./NavbarMapSet');
var Logo = require('./Logo');
var navStyles = require('../styles/navStyles');
var Values = require('../res/values');

var Navbar = React.createClass({	
	handleBack: function () {
		this.props.onBack(!this.props.parentState.mapping);
	},
	
	handleClick: function (faded) {
		this.props.onClick(faded);
	},
	
	render: function() {
		var signInText;
		var set;
		var navStyle;
		
		if (this.props.parentState.loggedIn && this.props.parentState.userInfo.firstName != null) {
			signInText = "Hi " + this.props.parentState.userInfo.firstName
		} else {
			signInText = Values.strings.login
		}
		
		if (this.props.navType == 'home') {
			set = <NavbarHomeSet parentState={this.props.parentState} signInText={signInText} onClick={this.handleClick} />;
			navStyle = navStyles.navbar;
		} else {
			set = <NavbarMapSet parentState={this.props.parentState} signInText={signInText} onClick={this.handleClick} onBack={this.handleBack} />;
			navStyle = navStyles.navbar.blue;
		}
		
		return (
			<div style={navStyle}>
				<Logo style={navStyles.logo} />
				<ul style={navStyles.ul}>
					{set}
				</ul>
			</div>
		);
	}
});

module.exports = Navbar;