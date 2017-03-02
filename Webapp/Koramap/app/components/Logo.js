var React = require('react');
const imgSrc = require('../res/img/assets/koralogo.png');

var Logo = React.createClass({
	render: function () {
		return <img src={imgSrc} alt="Kora logo" style={this.props.style}/>;
	}
});

module.exports = Logo;