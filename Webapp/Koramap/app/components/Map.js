React = require('react');
Sidebar = require('./Sidebar');

var Map = React.createClass({
	getInitialState: function () {
		return {
			openMenu: null
		};
	},
	
	render: function () {
		return (
			<div>
				<Sidebar openMenu={this.state.openMenu}/>
			</div>
		);
	}
});

module.exports = Map;