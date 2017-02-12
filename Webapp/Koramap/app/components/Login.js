var React = require('react');
var Fader = require('./Fader');
var appStyles = require('../styles/appStyles');
var Values = require('../res/values');

var Login = React.createClass({
	getInitialState: function () {
		return {
			username: '',
			password: '',
			result: ''
		};
	},
	
	handleInput: function (type, e) {
		if (type == 'u') {
			this.setState({
				username: e.target.value
			});
		} else if (type == 'p') {
			this.setState({
				password: e.target.value
			});
		}
	},
	
	attemptLogin: function (e) {
		e.preventDefault();
		
		formData='username=' + this.state.username + '&password=' + this.state.password;
		
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/get_login.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				console.log('Ready state: ' + request.readyState);
				return;
			} else {
				console.log('Ready state: ' + request.readyState)
			}

			if (request.status === 200) {
				var id = JSON.parse(request.responseText).UID.toLowerCase();
				var firstname = JSON.parse(request.responseText).firstname;
				var lastname = JSON.parse(request.responseText).lastname;
				
				document.getElementById('uname').value = '';
				document.getElementById('pword').value = '';
				this.props.onSubmit(true, {userName: this.state.username, firstName: firstname, lastName: lastname, userId: id});
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	},
	
	handleCancel: function () {
		document.getElementById('uname').value = '';
		document.getElementById('pword').value = '';
		
		this.props.onSubmit(this.props.parentState.loggedIn, this.props.parentState.userInfo);
	},
	
	render: function () {
		var style;
	
		if (this.props.loggingIn) {
			style = appStyles.login
		} else {
			style = {display: 'none'};
		}
		
		return (
			<div>
				<Fader faded={this.props.loggingIn} />
				<div style={style}>
					<h1 style={style.h1}>{Values.strings.login}</h1>
					<div style={style.form}>
						<p style = {style.p}>{Values.strings.username}</p>
						<input style={style.input} type="text" onChange={this.handleInput.bind(this, 'u')} id='uname'/>
						<p style={style.p}>{Values.strings.password}</p>
						<input style={style.input} type="password" onChange={this.handleInput.bind(this, 'p')} id='pword'/>
						<button style={style.button} onClick={this.attemptLogin}>{Values.strings.login}</button>
					</div>
					<div>
						<a style={style.a} onClick={this.handleSignup}>{Values.strings.signup}</a>
						<a style={style.a} onClick={this.handleCancel}>{Values.strings.cancel}</a>
					</div>
				</div>
			</div>
		);
	}
});

module.exports = Login;