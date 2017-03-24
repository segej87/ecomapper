const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');
const NavbarDropdownItem = require('./NavbarDropdownItem');
const SearchArea = require('./SearchArea');
const koraLogo = require('../res/img/assets/koralogo.png');
const publicIcon = require('../res/img/icons/public-icon2.png');
const privateIcon = require('../res/img/icons/private-icon2.png');
const measIcon = require('../res/img/icons/meas-icon2.png');
const photoIcon = require('../res/img/icons/photo-icon2.png');
const noteIcon = require('../res/img/icons/note-icon2.png');
import { Scrollbars } from 'react-custom-scrollbars'

var NavbarDropdown = React.createClass({
	inItems: [],
	items: [],
	
	//TODO: get non-standard icons from the server
	logos: {
		UCBerkeley: 'http://brand.berkeley.edu/wp-content/uploads/2016/10/ucbseal_139_540.png',
		Private: privateIcon,
		Public: publicIcon,
		Meas: measIcon,
		Photo: photoIcon,
		Note: noteIcon,
		segej87: 'https://ecomapper.blob.core.windows.net/profiles/segej87.jpg',
		rsege: 'https://ecomapper.blob.core.windows.net/profiles/rsege.jpg'
	},
	
	getInitialState: function () {
		return ({
			searchString: ''
		});
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
		this.setState(this.getInitialState());
		this.props.onClose(e);
	},
	
	addItem: function (e) {
		this.props.onAdd(e.target.id);
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.items[nextProps.type] != null) {
			this.inItems = nextProps.items[nextProps.type];
			this.items = this.filterString(this.state.searchString);
		}
		
		if (this.props.highlighted && !nextProps.highlighted || nextProps.type != this.props.type) {
			this.state = this.getInitialState();
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
				
				var imgSize;
				var borderRad;
				if (this.props.type == 'submitters') {
					imgSize = '50';
					borderRad = 25;
				} else {
					imgSize = '25';
					borderRad = 12.5;
				}
				return (
					<li key={i} id={item} style={SidebarStyles.addDisplay.li} onClick={this.addItem}>
						<img src={image} style={{verticalAlign: 'middle', borderRadius: borderRad}} width={imgSize} id={item} />
						<p style={{display: 'inline-block', margin: 'auto 10px', verticalAlign: 'middle'}} id={item}>{item}</p>
					</li>
				);
			});
		}
		
		return (
			<div style={linkStyle}>
				<button style={SidebarStyles.closeButton} onClick={this.handleClose}>&#x2e3;</button>
				<SearchArea val={this.state.searchString} handleInput={this.handleInput}/>
				<Scrollbars style={SidebarStyles.addDisplay.scrollBars}>
					<ul style={SidebarStyles.addDisplay.ul}>
						{showItems}
					</ul>
				</Scrollbars>
			</div>
		);
	}
});

export default NavbarDropdown;