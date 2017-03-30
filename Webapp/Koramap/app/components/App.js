React = require('react');
AppState = require('./AppState');
appStyles = require('../styles/appStyles');
Login = require('./Login');
Navbar = require('./Navbar');
Home = require('./Home').default;
MapContainer = require('./MapContainer').default;
Values = require('../res/values');

let appState = new AppState();

var HelloWorld = React.createClass({
	getInitialState: function () {
		return {
			loggedIn: false,
			userInfo: Values.standards.login,
			loggingIn: false,
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
		appState.setUserInfo(info);
		
		this.setState(
			{
				loggedIn: result,
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
			bodyJSX = <MapContainer appState={appState} offline={this.state.offline} loggedIn={this.state.loggedIn} mapping={this.state.mapping} onClick={this.handleMapping} />;
			navType = 'map'
		} else {
			bodyJSX = <Home loggedIn={this.state.loggedIn} userInfo={this.state.userInfo} mapping={this.state.mapping} onClick={this.handleMapping} />;
			navType = 'home'
		}
		
		return (
			<div style={appStyles.app}>
				<Login appState={appState} offline={this.state.offline} loggingIn={this.state.loggingIn} onSubmit={this.handleLoginResult} parentState = {this.state}/>
				<Navbar appState={appState} parentState={this.state} onClick={this.handleLoggingIn} onBack={this.handleMapping} navType={navType} />
				{bodyJSX}
			</div>
		);
	}
});

module.exports = HelloWorld;