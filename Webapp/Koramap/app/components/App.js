let React = require('react');
let AppState = require('./AppState');
import {app as appStyle} from '../styles/appStyles';
let Login = require('./Login').default;
let Navbar = require('./Navbar').default;
let Home = require('./Home').default;
let MapContainer = require('./MapContainer').default;

let appState = new AppState();

var HelloWorld = React.createClass({
	getInitialState: function () {
		return {
			loggedIn: false,
			loggingIn: false,
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
			bodyJSX = <MapContainer appState={appState} loggedIn={this.state.loggedIn} mapping={this.state.mapping} onClick={this.handleMapping} />;
			navType = 'map'
		} else {
			bodyJSX = <Home mapping={this.state.mapping} onClick={this.handleMapping} />;
			navType = 'home'
		}
		
		return (
			<div style={appStyle}>
				<Login appState={appState} loggedIn={this.state.loggedIn} loggingIn={this.state.loggingIn} onSubmit={this.handleLoginResult} />
				<Navbar appState={appState} parentState={this.state} onClick={this.handleLoggingIn} onBack={this.handleMapping} navType={navType} />
				{bodyJSX}
			</div>
		);
	}
});

export default HelloWorld;