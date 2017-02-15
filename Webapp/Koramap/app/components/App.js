React = require('react');
appStyles = require('../styles/appStyles');
Login = require('./Login');
Navbar = require('./Navbar');
Home = require('./Home');
MapContainer = require('./MapContainer');

var HelloWorld = React.createClass({
	getInitialState: function () {
		return {
			loggedIn: false,
			userInfo: {
				userName: 'public',
				firstName: 'Guest',
				lastName: 'Person',
				userId: '10101010-1010-1010-1010-101010101010'
			},
			loggingIn: false,
			faded: false,
			mapping: false
		};
	},
	
	handleLoggingIn: function (result) {
		this.setState(
			{
				loggingIn: result
			}
		);
	},
	
	handleLoginResult: function (result, info) {
		this.setState(
			{
				loggedIn: result,
				userInfo: {
					userName: info.userName,
					firstName: info.firstName,
					lastName: info.lastName,
					userId: info.userId
				},
				loggingIn: false
			}
		);
		
		if (result) {
			console.log('Logged in');
		} else {
			console.log('Login canceled');
		}
	},
	
	handleMapping: function (result) {
		this.setState(
			{
				mapping: result
			}
		);
	},
	
	render: function() {
		var bodyJSX;
		var navType;
		
		if (this.state.mapping) {
			bodyJSX = <MapContainer loggedIn={this.state.loggedIn} userInfo={this.state.userInfo} mapping={this.state.mapping} onClick={this.handleMapping}/>;
			navType = 'map'
		} else {
			bodyJSX = <Home loggedIn={this.state.loggedIn} userInfo={this.state.userInfo} mapping={this.state.mapping} onClick={this.handleMapping} />;
			navType = 'home'
		}
		
		console.log('Rendering app with navType: ' + navType);
		
		return (
			<div style={appStyles.app}>
				<Login loggingIn={this.state.loggingIn} onSubmit={this.handleLoginResult} parentState = {this.state}/>
				<Navbar parentState={this.state} onClick={this.handleLoggingIn} onBack={this.handleMapping} navType={navType} />
				{bodyJSX}
			</div>
		);
	}
});

module.exports = HelloWorld;