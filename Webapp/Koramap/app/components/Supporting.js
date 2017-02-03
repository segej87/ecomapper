var React = require('react');
var supportingStyles = require('../styles/supportingStyles')

var Supporting = React.createClass({
	render: function () {
		return (
		<div style={supportingStyles.supporting}>
		<div style={supportingStyles.container}>
			<p style={supportingStyles.p}>Kora will bring true collaboration, networking, and sharing to the environmental
				information ecosystem. It will connect citizens, scientists, and decision-makers in a 
				joint project of observing, analyzing, and monitoring the world we all share.</p>
        </div>
		</div>
		);
	}
});

module.exports = Supporting;