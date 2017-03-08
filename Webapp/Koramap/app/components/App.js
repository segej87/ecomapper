React = require('react');
appStyles = require('../styles/appStyles');
Login = require('./Login');
Navbar = require('./Navbar');
Home = require('./Home');
MapContainer = require('./MapContainer').default;

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
			mapping: false,
			offline: false
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
			bodyJSX = <MapContainer offline={this.state.offline} loggedIn={this.state.loggedIn} userInfo={this.state.userInfo} mapping={this.state.mapping} onClick={this.handleMapping}/>;
			navType = 'map'
		} else {
			bodyJSX = <Home loggedIn={this.state.loggedIn} userInfo={this.state.userInfo} mapping={this.state.mapping} onClick={this.handleMapping} />;
			navType = 'home'
		}
		
		return (
			<div style={appStyles.app}>
				<Login offline={this.state.offline} loggingIn={this.state.loggingIn} onSubmit={this.handleLoginResult} parentState = {this.state}/>
				<Navbar parentState={this.state} onClick={this.handleLoggingIn} onBack={this.handleMapping} navType={navType} />
				{bodyJSX}
			</div>
		);
	}
});

module.exports = HelloWorld;