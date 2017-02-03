var React = require('react');

var Logo = React.createClass({
	render: function () {
		return <img src="https://ecomapper.blob.core.windows.net/bg-artwork-etc/kora_logo.png" alt="Kora logo" style={this.props.style}/>;
	}
});

module.exports = Logo;