var React = require('react');
var mainStyles = require('../styles/mainStyles');

var Main = React.createClass ({
	render: function() {
		return (
		<div style={mainStyles.main}>
            <div style={mainStyles.container}>
                <h1 style={mainStyles.h1}>Kora</h1>
                <p style={mainStyles.p}>social mapping - coming soon</p>
				<button style={mainStyles.button}>Go to map</button>
            </div>
        </div>
		);
	}
});

module.exports = Main;