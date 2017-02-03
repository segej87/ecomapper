var React = require('react');
var appStyles = require('../styles/appStyles');
var Login = require('./Login');
var Navbar = require('./Navbar');
var Main = require('./Main');
var Supporting = require('./Supporting');

var HelloWorld = React.createClass({
	getInitialState: function () {
		return {
			loggedIn: false,
			userInfo: {
				userName: null,
				firstName: null,
				lastName: null
			},
			loggingIn: false
		};
	},
	
	handleLoggedIn: function (result, info) {
		this.setState(
			{
				loggedIn: result,
				userInfo: {
					userName: info.userName,
					firstName: info.firstName,
					lastName: info.lastName
				}
			}
		);
	},
	
	handleLoggingIn: function (loggingIn) {
		this.setState(
			{
				loggingIn: loggingIn
			}
		);
	},
	
	render: function() {
		return (
		<div style={appStyles.app}>
		<Login loggingIn={this.state.loggingIn} onSubmit={this.handleLoggedIn} onFinish={this.handleLoggingIn} />
		<Navbar loggedIn={this.state.loggedIn} userInfo={this.state.userInfo} faded={this.state.faded} onClick={this.handleLoggingIn}/>
		<Main loggedIn={this.state.loggedIn} userInfo= {this.state.userInfo}/>
		<Supporting />
		</div>
		);
	}
});

module.exports = HelloWorld;