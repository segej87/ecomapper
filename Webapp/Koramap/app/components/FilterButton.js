React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');
SidebarDropdown = require('./SidebarDropdown');
// let Draggable = require('react-draggable');

var FilterButton = React.createClass({
	getInitialState: function () {
		return ({
			activeDrags: 0,
			deltaPosition: {
				x: 0,
				y: 0
			},
			highlighted: false
		});
	},
	
	handleFilterChange: function () {
		console.log('removing');
		this.props.handleDragRemove(this.props.type, this.props.item, 'Remove');
	},
	
	// handleDrag: function (e, ui) {
		// const {x, y} = this.state.deltaPosition;
		// this.setState({
			// deltaPosition: {
				// x: x + ui.deltaX,
				// y: y + ui.deltaY
			// }
		// });
	// },
	
	// handleStart: function () {
		// this.setState({activeDrags: ++this.state.activeDrags});
	// },
	
	// handleStop: function () {
		// let {x, y} = this.state.deltaPosition;
		
		// this.setState(this.getInitialState());
		
		// if (x != 0 && y != 0) {
			// this.handleFilterChange();
		// }
	// },
	
	handleTagClick: function (ev) {
		if (this.state.deltaPosition.x == 0 && this.state.deltaPosition.y == 0) {
			this.props.onClick(this.props.type, this.props.item);
		}
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.highlightedType != this.props.highlightedType || nextProps.highlightedItem != this.props.highlightedItem) {
			if (nextProps.highlightedType == this.props.type && nextProps.highlightedItem == this.props.item) {
				this.setState({highlighted: true});
			} else {
				this.setState({highlighted: false});
			}
		}
	},
	
	render: function () {
		var buttonStyle;
		if (this.state.activeDrags > 0) {
			buttonStyle = SidebarStyles[this.props.type + 'Button'].moving;
		} else {
			buttonStyle = SidebarStyles[this.props.type + 'Button'];
		}
		
		return (
			//TODO: Finish implementing draggables
			// <Draggable
			// zIndex={10000000000}
			// onStart={this.handleStart}
			// onDrag={this.handleDrag}
			// onStop={this.handleStop}>
				<button id={'tag_' + this.props.item} style={buttonStyle} onClick={this.handleTagClick}>{this.props.item}</button>
			// </Draggable>
		);
	}
});

module.exports = FilterButton;