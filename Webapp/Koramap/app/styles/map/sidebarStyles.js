Colors = require('../../res/values').colors;
Numbers = require('../../res/values').numbers;

sidebarContainer = {
	position: 'absolute',
	marginTop: Numbers.sidebarTopMargin,
	marginLeft: 0,
	maxHeight: Numbers.sidebarHeight,
	color: 'black',
	textAlign: 'left',
	boxSizing: 'border-box'
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
		fontFamily: 'Oswald, sans-serif',
		padding: '0px 10px 0px 10px',
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
		fontFamily: 'Oswald, sans-serif',
		padding: '0px 10px 0px 10px',
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

h1 = {
	fontSize: '3vh',
	marginBottom: 5,
	color: 'black'
};

p = {
	fontSize: 11,
	marginTop: 5,
	marginBottom: 5,
	color: 'black'
};

a = {
	fontSize: '2vh',
	textDecoration: 'none',
	cursor: 'pointer',
	color: 'blue'
};

date = {
	fontSize: 11,
	marginTop: 5,
	marginBottom: 5,
	color: 'rgb(75, 75, 75)'
};

featureInfo = {
	color: 'black',
	overflow: 'auto'
};

tagButton = {
	backgroundColor: 'rgba(75, 200, 100, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: '2vh',
	height: '3vh',
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginBottom: 5,
	marginRight: 5
};

accessButton = {
	backgroundColor: 'rgba(75, 100, 200, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: '2vh',
	height: '3vh',
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginBottom: 5,
	marginRight: 5
};

deleteButton = {
	backgroundColor: 'rgba(255, 75, 100, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: '2vh',
	height: '3vh',
	width: '9vh',
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginTop: 2,
	marginRight: 5
};

addButton = {
	position: 'relative',
	float: 'right',
	border: 'none',
	width: 20,
	height: 20,
	backgroundColor: 'rgba(220,220,220,0.75)',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	padding: 0,
	textAlign: 'center',
	cursor: 'pointer'
};

buttonHolder = {
	backgroundColor: 'rgb(245, 245, 245)',
	borderRadius: 2,
	padding: 10,
	marginTop: -0.5,
	textAlign: 'center',
	backgroundColor: 'rgba(245, 245, 245, 0.75)',
	border: 'none'
};

tabHolder = {
	marginTop: 10,
	paddingTop: 2,
	paddingLeft: 5,
	paddingRight: 5,
	textAlign: 'center',
	marginBottom: 0
};

titleArea = {
	borderBottom: '1px solid black'
};

noteArea = {
	paddingLeft: 5,
	paddingRight: 5,
	paddingTop: 5,
	paddingBottom: 5,
	backgroundColor: 'white',
	border: 'none',
	borderRadius: 2,
	minHeight: 50
};

note = {
	fontSize: 12,
	color: 'black',
	margin: 0
};

tab = {
	fontSize: '2vh',
	borderTop: '1px solid rgba(150,150,150,0.75)',
	borderLeft: '1px solid rgba(150,150,150,0.75)',
	borderRight: '1px solid rgba(150,150,150,0.75)',
	borderBottom: 'none',
	marginLeft: 2.5,
	marginRight: 2.5,
	marginBottom: 0,
	paddingTop: 2,
	paddingLeft: 5,
	paddingRight: 5,
	minWidth: 100,
	textDecoration: 'none',
	backgroundColor: 'rgba(245, 245, 245, 0.25)',
	cursor: 'pointer',
	selected: {
		fontSize: '2vh',
		borderTop: '1px solid rgba(150,150,150,0.75)',
		borderLeft: '1px solid rgba(150,150,150,0.75)',
		borderRight: '1px solid rgba(150,150,150,0.75)',
		borderBottom: 'none',
		marginLeft: 2.5,
		marginRight: 2.5,
		marginBottom: 0,
		paddingTop: 2,
		paddingLeft: 5,
		paddingRight: 5,
		minWidth: 100,
		textDecoration: 'none',
		backgroundColor: 'rgba(245, 245, 245, 0.75)',
		cursor: 'pointer',
	}
};

module.exports = {
	sidebarContainer,
	sidebarOpen,
	toggle,
	featureInfo,
	h1,
	p,
	a,
	date,
	titleArea,
	noteArea,
	note,
	deleteButton,
	tagButton,
	accessButton,
	buttonHolder,
	addButton,
	tabHolder,
	tab
};