var React = require('react');
var SidebarStyles = require('../styles/map/sidebarStyles');
var SidebarDropdownItem = require('./SidebarDropdownItem')

var SidebarDropdown = React.createClass({
	items: [],
	
	getInitialState: function () {
		return ({
			highlighted: false
		});
	},
	
	handleMouseEnter: function () {
		this.setState({highlighted: true});
	},
	
	handleClick: function (result) {
		this.props.handleClick(this.props.type, result);
		this.setState({highlighted: false});
	},
	
	componentWillMount: function () {
		this.items = [];
		for (var i = 0; i < this.props.items.length; i++) {
			this.items.push(<SidebarDropdownItem name={this.props.items[i]} onClick={this.handleClick} key={i} />);
		}
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.highlightedType == this.props.type && nextProps.highlightedItem == this.props.item) {
			this.setState({highlighted: true});
		} else {
			this.setState({highlighted: false});
		}
	},
	
	render: function () {
		var linkStyle;
		
		if (this.props.highlighted || this.state.highlighted) {
			linkStyle = SidebarStyles.dropdownContent
		} else {
			linkStyle = SidebarStyles.hidden
		}
		
		return (
			<div style={SidebarStyles.dropdown} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
				<div style={linkStyle}>
					{this.items}
				</div>
			</div>
		);
	}
});

module.exports = SidebarDropdown;