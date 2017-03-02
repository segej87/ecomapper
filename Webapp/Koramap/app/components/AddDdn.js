var React = require('react');
var SidebarStyles = require('../styles/map/sidebarStyles');
var NavbarDropdownItem = require('./NavbarDropdownItem');
const koraLogo = require('../res/img/assets/koralogo.png');
const publicIcon = require('../res/img/icons/public-icon.png');
const privateIcon = require('../res/img/icons/private-icon.png');

var NavbarDropdown = React.createClass({
	inItems: [],
	items: [],
	logos: {
		UCBerkeley: 'http://brand.berkeley.edu/wp-content/uploads/2016/10/ucbseal_139_540.png',
		Private: privateIcon,
		Public: publicIcon
	},
	
	filterString: function (value) {
		var newItems = [];
		for (var i = 0; i < this.inItems.length; i++) {
			if (this.inItems[i].includes(value)) {
				newItems.push(this.inItems[i]);
			}
		}
		
		return newItems;
	},
	
	handleInput: function (e) {
		this.setState({
			searchString: e.target.value
		});
		
		if (e.target.value != '') {
			this.items = this.filterString(e.target.value);
		} else {
			this.items = this.inItems;
		}
	},
	
	handleClose: function (e) {
		document.getElementById('search').value = '';
		this.props.onClose(e);
	},
	
	addItem: function (e) {
		this.props.onAdd(e.target.id);
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.items[nextProps.type] != null) {
			this.inItems = nextProps.items[nextProps.type];
			this.items = this.filterString(document.getElementById('search').value);
		}
	},
	
	render: function () {
		var linkStyle;
		
		if (this.props.highlighted) {
			linkStyle = SidebarStyles.addDisplay
		} else {
			linkStyle = SidebarStyles.hidden
		}
		
		var showItems = [];
		if (this.props.type != null) {
			showItems = this.items.map((item, i) => {
				var image = koraLogo;
				if (this.logos[item.replace(' ','')]) {
					image = this.logos[item.replace(' ','')];
				}
				return (
					<li key={i} id={item} style={SidebarStyles.addDisplay.li} onClick={this.addItem}>
						<img src={image} style={{verticalAlign: 'middle'}} width="25" id={item} />
						<p style={{display: 'inline-block', margin: 'auto 10px', verticalAlign: 'middle'}} id={item}>{item}</p>
					</li>
				);
			});
		}
		
		return (
			<div style={linkStyle}>
				<button style={SidebarStyles.closeButton} onClick={this.handleClose}>&#x2e3;</button>
				<div style={{textAlign: 'center'}}>
					<p style={SidebarStyles.addDisplay.p}>Search:</p>
					<input type="text" id="search" style={SidebarStyles.addDisplay.input} onChange={this.handleInput}/>
				</div>
				<ul style={SidebarStyles.addDisplay.ul}>
					{showItems}
				</ul>
			</div>
		);
	}
});

module.exports = NavbarDropdown;