var React = require('react');
var Fader = require('./Fader');
var appStyles = require('../styles/appStyles');
var Values = require('../res/values');

var Login = React.createClass({
	getInitialState: function () {
		return {
			username: '',
			password: '',
			result: '',
			error: '',
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
		
		if (this.state.error != '') {
			this.setState({
					error: ''
				});
		}
	},
	
	attemptLogin: function (e) {
		if (this.props.offline) {
			this.handleCancel();
		}
		
		e.preventDefault();
		
		formData='username=' + this.state.username + '&password=' + this.state.password;
		
		var request = new XMLHttpRequest;
		
		var method = 'POST';
		var url = 'http://ecocollector.azurewebsites.net/get_login.php';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}

			if (request.status === 200) {
				try {
					var id = JSON.parse(request.responseText).UID.toLowerCase();
					if (id) {
						var firstname = JSON.parse(request.responseText).firstname;
						var lastname = JSON.parse(request.responseText).lastname;
						
						document.getElementById('uname').value = '';
						document.getElementById('pword').value = '';
						this.props.onSubmit(true, {userName: this.state.username, firstName: firstname, lastName: lastname, userId: id});
					}
				} catch (e) {
					var errorText = 'Can\'t log you in. Please try again.';
					if (request.responseText.includes('do not match any on record')) {
						errorText = 'That username and password combo doesn\'t match our records';
					} else if (request.responseText.includes('not found')) {
						errorText = 'That username wasn\'t found';
					}
					this.setState({
						error: errorText
					});
				}
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.send(formData);
	},
	
	handleEnter: function (event) {
		if (event.charCode == 13) {
			event.charCode == null;
			this.attemptLogin(event);
			return false;
		}
	},
	
	handleCancel: function () {
		document.getElementById('uname').value = '';
		document.getElementById('pword').value = '';
		
		this.setState(this.getInitialState());
		
		this.props.onSubmit(this.props.parentState.loggedIn, this.props.parentState.userInfo);
	},
	
	render: function () {
		var style;
	
		if (this.props.loggingIn) {
			style = appStyles.login
		} else {
			style = {display: 'none'};
		}
		
		var errorMsg;
		if (this.state.error != '') {
			errorMsg = <p style={style.error}>{this.state.error}</p>
		}
		
		return (
			<div>
				<Fader faded={this.props.loggingIn} />
				<div style={style}>
					<h1 style={style.h1}>{Values.strings.login}</h1>
					<div style={style.form}>
						<p style = {style.p}>{Values.strings.username}</p>
						<input style={style.input} type="text" onChange={this.handleInput.bind(this, 'u')} autoFocus id='uname'/>
						<p style={style.p}>{Values.strings.password}</p>
						<input style={style.input} type="password" onChange={this.handleInput.bind(this, 'p')} onKeyPress={this.handleEnter} id='pword'/>
						<button style={style.button} onClick={this.attemptLogin}>{Values.strings.login}</button>
					</div>
					<div>
						<a style={style.a} onClick={this.handleSignup}>{Values.strings.signup}</a>
						<a style={style.a} onClick={this.handleCancel}>{Values.strings.cancel}</a>
						{errorMsg}
					</div>
				</div>
			</div>
		);
	}
});

module.exports = Login;