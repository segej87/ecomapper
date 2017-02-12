Colors = require('../../res/values').colors;
Numbers = require('../../res/values').numbers;

sidebarContainer = {
	position: 'absolute',
	zIndex: '1000001',
	marginTop: Numbers.sidebarTopMargin,
	marginLeft: 0,
	height: Numbers.sidebarHeight
};

sidebarOpen = {
	position: 'relative',
	float: 'left',
	marginTop: 0,
    backgroundColor: Colors.filterColor,
    width: 300,
	height: '100%',
	textAlign: 'right',
	padding: 20
};

toggle = {
	filter: {
		position: 'relative',
		float: 'left',
		height: 50,
		width: 30,
		color: 'white',
		textWeight: '900',
		backgroundColor: Colors.filterColor,
		cursor: 'pointer',
		border: 'none',
		borderRadius: '0px 7px 7px 0px',
		boxShadow: '0px 4px 4px 0px rgba(0, 0, 0, 0.4)'
	},
	message: {
		position: 'relative',
		float: 'left',
		height: 100,
		width: 30,
		color: 'white',
		textWeight: '900',
		backgroundColor: Colors.messageColor,
		cursor: 'pointer',
		border: 'none',
		borderRadius: '0px 7px 7px 0px',
		boxShadow: '0px 4px 4px 0px rgba(0, 0, 0, 0.4)'
	}
};

module.exports = {
	sidebarContainer,
	sidebarOpen,
	toggle
};