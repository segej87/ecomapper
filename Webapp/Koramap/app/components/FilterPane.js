React = require('react');

var FilterPane = React.createClass({
	render: function () {
		var display;
		
		if (this.props.open) {
			display = <h1>Filter is open!</h1>;
		} else {
			display = <h1>Filter is closed!</h1>;
		}
		
		return display;
	}
});

module.exports = FilterPane;