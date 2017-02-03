var React = require('react');
var appStyles = require('../styles/appStyles');

var Fader = React.createClass({
	render: function () {
		var fadeStyle;
		
		if (this.props.faded) {
			fadeStyle = appStyles.fadeOut
		} else {
			fadeStyle = {display: 'none'}
		}
		
		return <div style={fadeStyle}></div>
	}
});

module.exports = Fader;