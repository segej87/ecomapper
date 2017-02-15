React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');

var FilterContent = React.createClass({
	handleAdd: function () {
		//TODO: Finish adding filters to array
		alert('tags');
	},
	
	render: function () {
		var tags = this.props.filters.tags.map((tag, i) => {
			return (
				<button key={'tag_' + i} style={SidebarStyles.tagButton}>{tag}</button>
			);
		});
		
		var accesses = this.props.filters.access.map((access, i) => {
			return (
				<button key={'access_' + i} style={SidebarStyles.accessButton}>{access}</button>
			);
		});
		
		return (
			<div>
				<h1 style={SidebarStyles.h1}>Filtering</h1>
				<div style={{marginTop: 20}}>
					<p style={SidebarStyles.p}>{'Tag filters:'}</p>
					<div style={SidebarStyles.buttonHolder}>
						{tags}
						<button style={SidebarStyles.addButton} onClick={this.handleAdd}>+</button>
					</div>
				</div>
				<div style={{marginTop: 20}}>
					<p style={SidebarStyles.p}>Access filters:</p>
					<div style={SidebarStyles.buttonHolder}>
						{accesses}
						<button style={SidebarStyles.addButton}>+</button>
					</div>
				</div>
			</div>
		);
	}
});

module.exports = FilterContent;