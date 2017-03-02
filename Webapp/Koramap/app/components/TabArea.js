React = require('react');

TabArea = React.createClass({
	switchType: function () {
		this.props.onClick();
	},
	
	render: function () {
		// const buttons = this.props.selectedPlace.featureProps[this.props.activeButtons].map((item, i) => {
			
		// });
		
		var tags = this.props.selectedPlace.featureProps.tags.map((tag, i) => {
			return (
				<button key={'tag_' + this.props.selectedPlace.fuid + i} style={SidebarStyles.tagsButton}>{tag}</button>
			);
		});
		
		var access = this.props.selectedPlace.featureProps.access.map((access, i) => {
			return (
				<button key={'access_' + this.props.selectedPlace.fuid + i} style={SidebarStyles.accessButton}>{access}</button>
			);
		});
		
		var tabArea;
		var buttons;
		
		switch (this.props.activeButtons) {
			case 'tags':
				buttons = tags;
				tabArea = (
					<div style={SidebarStyles.tabHolder}>
						<button style={SidebarStyles.tab.selected}>Tags</button>
						<button style={SidebarStyles.tab} onClick={this.switchType}>Access</button>
					</div>
				);
				break;
			case 'access':
				buttons = access;
				tabArea = (
					<div style={SidebarStyles.tabHolder}>
						<button style={SidebarStyles.tab} onClick={this.switchType}>Tags</button>
						<button style={SidebarStyles.tab.selected}>Access</button>
					</div>
				);
				break;
			default:
				buttons = tags
		}
		
		return (
			<div>
				{tabArea}
				<div style={SidebarStyles.buttonHolder}>
					{buttons}
				</div>
			</div>
		);
	}
});

module.exports = TabArea;