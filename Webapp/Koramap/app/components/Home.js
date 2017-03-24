var React = require('react');
var mainStyles = require('../styles/home/mainStyles');
var Supporting = require('./Supporting');

var Home = React.createClass ({
	handleClick: function () {
		this.props.onClick(!this.props.mapping);
	},
	
	render: function() {
		return (
		<div>
			<div style={mainStyles.main}>
				<div style={mainStyles.container}>
					<h1 style={mainStyles.h1}>Kora</h1>
					<p style={mainStyles.p}>social mapping - coming soon</p>
					<button style={mainStyles.button} onClick={this.handleClick}>Go to map</button>
				</div>
			</div>
			<Supporting />
		</div>
		);
	}
});

export default Home;