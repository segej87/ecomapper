Colors = require('../../res/values').colors;
Numbers = require('../../res/values').numbers;

sidebarContainer = {
	position: 'absolute',
	marginTop: Numbers.sidebarTopMargin,
	marginLeft: 0,
	maxHeight: Numbers.sidebarHeight
};

sidebarOpen = {
	filter: {
		position: 'relative',
		float: 'left',
		zIndex: '1000002',
		marginTop: 0,
		backgroundColor: Colors.filterColor,
		width: 300,
		height: Numbers.sidebarHeight,
		textAlign: 'center',
		fontFamily: 'Oswald, sans-serif',
		padding: 0,
		boxShadow: '4px 0px 4px 0px rgba(0, 0, 0, 0.4)'
	},
	message: {
		position: 'relative',
		float: 'left',
		zIndex: '1000002',
		marginTop: 0,
		backgroundColor: Colors.messageColor,
		width: 300,
		height: Numbers.sidebarHeight,
		textAlign: 'center',
		fontFamily: 'Oswald, sans-serif',
		padding: 0,
		boxShadow: '4px 0px 4px 0px rgba(0, 0, 0, 0.4)'
	}
};

toggle = {
	filter: {
		position: 'relative',
		float: 'left',
		height: Numbers.toggleHeight,
		width: 30,
		zIndex: '1000001',
		color: 'white',
		fontWeight: '900',
		backgroundColor: Colors.filterColor,
		cursor: 'pointer',
		border: 'none',
		borderRadius: '0px 7px 7px 0px',
		boxShadow: '4px 2px 4px 0px rgba(0, 0, 0, 0.4)'
	},
	message: {
		position: 'relative',
		float: 'left',
		top: Numbers.toggleHeight,
		height: Numbers.toggleHeight,
		width: 30,
		zIndex: '1000001',
		color: 'white',
		fontWeight: '900',
		backgroundColor: Colors.messageColor,
		cursor: 'pointer',
		border: 'none',
		borderRadius: '0px 7px 7px 0px',
		boxShadow: '4px 2px 4px 0px rgba(0, 0, 0, 0.4)'
	}
};

module.exports = {
	sidebarContainer,
	sidebarOpen,
	toggle
};