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
	
	tester: function () {
		var params = JSON.stringify({
			username: this.state.username,
			password: this.state.password
		});
		
		var formData = new FormData();
		
		for (var k in params) {
			formData.append(k, params[k]);
		};
		
		fetch('http://ecocollector.azurewebsites.net/get_login.php', {
			method: 'POST',
			headers: {
				'Content-Type': 'x-www-form-urlencoded; charset=UTF-8'
			},
			body: formData
		})
		.then ((response) => response.json())
		.then((json) => {
				this.setState({
					result: json
				});
			}
		)
		.catch((error) => {
			console.log(error)
		});
	},
	
	tester2: function (e) {
		e.preventDefault();
		console.log('starting tester2');
		
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
				
				this.props.onSubmit(true, {userName: this.state.username, firstName: firstname, lastname: lastname, userId: id});
			} else {
				console.log('Status: ' + request.status);
				console.log('Status text: ' + request.statusText);
			}
		};

		request.open(method, url, true);
		if (method == 'GET') {
			request.send();
		} else {
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
		}
	},
	
	attemptLogin: function (e) {
		this.tester2(e);
	},
	
	render: function () {
		var style;
	
		if (this.props.loggingIn) {
			style = appStyles.login
		} else {
			style = {display: 'none'};
		}
		
		return (
			<div style={appStyles.app}>
				<Fader faded={this.props.loggingIn} />
				<div style={style}>
					<h1 style={style.h1}>{Values.strings.login}</h1>
					<div style={style.form}>
						{Values.strings.username}<br/>
						<input style={style.input} type="text" onChange={this.handleInput.bind(this, 'u')}/><br/><br/>
						{Values.strings.password}<br/>
						<input style={style.input} type="password" onChange={this.handleInput.bind(this, 'p')}/><br/><br/><br/>
						<button style={style.button} onClick={this.attemptLogin}>{Values.strings.login}</button>
					</div>
				</div>
			</div>
		);
	}
});

module.exports = Login;