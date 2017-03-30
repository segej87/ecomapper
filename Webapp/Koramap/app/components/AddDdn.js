const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');
const NavbarDropdownItem = require('./NavbarDropdownItem');
const SearchArea = require('./SearchArea');
const koraLogo = require('../res/img/assets/koralogo.png');
import { Scrollbars } from 'react-custom-scrollbars'

var NavbarDropdown = React.createClass({
	inItems: [],
	items: [],
	
	//TODO: get non-standard icons from the server
	logos: {
		'UC Berkeley': 'http://brand.berkeley.edu/wp-content/uploads/2016/10/ucbseal_139_540.png',
		'Private': require('../res/img/icons/private-icon2.png'),
		'Public': require('../res/img/icons/public-icon2.png'),
		'Meas': require('../res/img/icons/meas-icon2.png'),
		'Photo': require('../res/img/icons/photo-icon2.png'),
		'Note': require('../res/img/icons/note-icon2.png')
	},
	
	getInitialState: function () {
		return ({
			searchString: ''
		});
	},
	
	filterString: function (value) {
		var newItems = {};
		for (var i = 0; i < Object.values(this.inItems).length; i++) {
			if (Object.values(this.inItems)[i].includes(value)) {
				newItems[Object.keys(this.inItems)[i]] = Object.values(this.inItems)[i];
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
		//TODO: standardize this
		if (this.props.type == 'Geo') {
			this.props.onAdd(e.target.id);
		} else {
			this.props.onAdd(this.items[e.target.id]);
		}
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
			showItems = Object.keys(this.items).map((item, i) => {
				var image = koraLogo;
				if (this.props.type == 'submitters') {
					image = 'https://ecomapper.blob.core.windows.net/profiles/' + this.items[item] + '.jpg';
				} else if (this.logos[this.items[item]]) {
					image = this.logos[this.items[item]];
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
					<li key={item} id={item} style={SidebarStyles.addDisplay.li} onClick={this.addItem}>
						<img src={image} style={{verticalAlign: 'middle', borderRadius: borderRad}} width={imgSize} id={item} />
						<p style={{display: 'inline-block', margin: 'auto 10px', verticalAlign: 'middle'}} id={item}>{this.items[item]}</p>
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