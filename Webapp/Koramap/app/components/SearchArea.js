const React = require('react');

const SearchArea = React.createClass({
	getInitialState: function () {
		return ({
			highlighted: false,
		});
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.val != this.props.val) {
			document.getElementById('search').value = nextProps.val;
		}
	},
	
	handleMouseEnter: function () {
		this.setState({
			highlighted: true
		});
	},
	
	handleMouseLeave: function () {		
		this.setState({
			highlighted: false
		});
	},
	
	render: function () {
		var style = SidebarStyles.addDisplay.searchHolder;
		if (this.state.highlighted || document.getElementById('search') === document.activeElement) {
			style = SidebarStyles.addDisplay.searchHolder.highlighted;
		}
		
		return (
			<div style={style}>
				<p style={SidebarStyles.addDisplay.p}>Search:</p>
				<input type="text" id="search" style={SidebarStyles.addDisplay.input} onChange={this.props.handleInput} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave} onClick={this.handleClick}/>
			</div>
		);
	}
});

module.exports = SearchArea;