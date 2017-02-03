var React = require('react');
var Fader = require('./Fader');
var appStyles = require('../styles/appStyles');

var Login = React.createClass({
	submit: function (e) {
		this.props.onSubmit(true, {userName: 'segej87', firstName: 'Jon', lastName: 'Sege'});
		this.props.onFinish(false);
	},
	
	render: function () {
		var style = {display: 'none'};
	
		if (this.props.loggingIn) {
			style = appStyles.login
		}
		
		return (
			<div style={appStyles.app}>
			<Fader faded={this.props.loggingIn} />
			<div style={style}>
				<h1 style={style.h1}>Sign in</h1>
			<form style={style.form} onsubmit={this.submit}>
				Username<br/>
				<input style={style.input} type="text" name="username" /><br/><br/>
				Password<br/>
				<input style={style.input} type="password" name="password" /><br/><br/><br/>
				<button style={style.button} type="submit">Sign in</button>
			</form>
			</div>
			</div>
		);
	}
});

module.exports = Login;