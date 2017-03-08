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
		zIndex: '1000003',
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
		zIndex: '1000004',
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
		zIndex: '1000002',
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
	fontSize: Numbers.h1FontSize,
	marginBottom: 5,
	color: 'white'
};

p = {
	textAlign: 'center',
	minWidth: 75,
	padding: 2,
	fontFamily: 'Lato, Open Sans, sans-serif',
	backgroundColor: 'rgba(220,220,220,0.95)',
	borderRadius: '2px 2px 0px 0px',
	display: 'inline-block',
	fontSize: 12,
	marginTop: 5,
	marginBottom: 0,
	color: 'black',
	boxShadow: '0px -1px 1px 0px rgba(0, 0, 0, 0.4)',
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

tagsButton = {
	backgroundColor: 'rgba(75, 200, 100, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: Numbers.buttonFontSize,
	height: Numbers.buttonHeight,
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginBottom: 5,
	marginRight: 5,
};

datatypeButton = {
	backgroundColor: 'rgba(200, 200, 100, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: Numbers.buttonFontSize,
	height: Numbers.buttonHeight,
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginBottom: 5,
	marginRight: 5,
	moving: {
		backgroundColor: 'rgba(200, 200, 100, 0.9)',
		color: 'white',
		fontFamily: 'Lato, Open Sans, sans-serif',
		fontSize: Numbers.buttonFontSize,
		height: Numbers.buttonHeight,
		border: 'none',
		cursor: 'pointer',
		borderRadius: 2,
		boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
		marginBottom: 5,
		marginRight: 5,
		position: 'absolute'
	}
};

speciesButton = {
	backgroundColor: 'rgba(200, 100, 75, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: Numbers.buttonFontSize,
	height: Numbers.buttonHeight,
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginBottom: 5,
	marginRight: 5
};

submittersButton = {
	backgroundColor: 'rgba(200, 100, 100, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: Numbers.buttonFontSize,
	height: Numbers.buttonHeight,
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
	fontSize: Numbers.buttonFontSize,
	height: Numbers.buttonHeight,
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
	fontSize: Numbers.buttonFontSize,
	height: Numbers.buttonHeight,
	border: 'none',
	cursor: 'pointer',
	borderRadius: 2,
	boxShadow: '0px 2px 3px 0px rgba(0, 0, 0, 0.4)',
	marginTop: 2,
	marginRight: 5
};

addButton = {
	position: 'relative',
	top: 8.5,
	float: 'right',
	border: 'none',
	width: 15,
	height: 15,
	fontSize: 10,
	backgroundColor: 'rgba(220,220,220,0.95)',
	borderRadius: '2px 2px 0px 0px',
	boxShadow: '0px -1px 1px 0px rgba(0, 0, 0, 0.4)',
	padding: 0,
	textAlign: 'center',
	cursor: 'pointer'
};

buttonHolder = {
	backgroundColor: 'rgb(245, 245, 245)',
	borderRadius: '0px 0px 2px 2px',
	padding: 10,
	textAlign: 'center',
	backgroundColor: 'rgb(245, 245, 245)',
	border: 'none',
	maxHeight: 75,
	overflowY: 'auto'
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

dropdown = {
	position: 'absolute',
	display: 'inline-block'
};

dropdownContent = {
	display: 'inline-block',
    position: 'absolute',
	left: -10,
    backgroundColor: 'rgba(255, 255, 255, 0.85)',
    minWidth: '125px',
    boxShadow: '0px 8px 16px 0px rgba(0, 0, 0, 0.4)',
    zIndex: '100000000000000000',
    borderRadius: '0 0px 0px 0px',
	a: {
		display: 'inline-block',
		width: 115,
		letterSpacing: 1,
		textTransform: 'none',
		color: '#00004c',
		textDecoration: 'none',
		padding: 5,
		fontFamily: 'Lato, sans-serif',
		fontSize: 10,
		highlighted: {
			display: 'block',
			width: 115,
			fontWeight: 'bolder',
			letterSpacing: 1,
			textTransform: 'none',
			color: '#00004c',
			textDecoration: 'none',
			padding: 5,
			fontFamily: 'Lato, sans-serif',
			background: 'rgba(255, 255, 255, 0.75)',
			cursor: 'pointer',
			fontSize: 10,
		}
	}
};

addDisplay = {
	display: 'inline-block',
	position: 'absolute',
	left: 325,
	top: Numbers.toggleHeight,
	backgroundColor: 'white',
	height: '75%',
	width: 250,
	borderRadius: 7,
	boxShadow: '4px 0px 4px 0px rgba(0, 0, 0, 0.4)',
	p: {
		display: 'inline-block',
		fontSize: 12,
		margin: '0px 0px 0px 0px',
		padding: 2,
		color: 'black',
	},
	input: {
		fontFamily: 'Open Sans, sans-serif',
		fontSize: 11,
		height: 16,
		border: 'none',
		textAlign: 'left',
		paddingLeft: 10,
	},
	ul: {
		position: 'relative',
		top : '9%',
		borderTop: '2px solid rgba(100,100,100,0.5)',
		padding: 0,
		paddingTop: 10,
		height: '70%',
		overflow: 'auto'
	},
	li: {
		paddingLeft: 5,
		paddingTop: 5,
		paddingBottom: 2,
		verticalAlign: 'middle',
		margin: '5px 10px',
		listStyle: 'none',
		cursor: 'pointer',
		boxShadow: '1px 1px 2px 1px rgba(0, 0, 0, 0.4)'
	},
	searchHolder: {
		textAlign: 'center',
		borderBottom: '1.5px solid rgb(255,75,100)',
		position: 'relative',
		width: 205,
		height: 20,
		top: '7%',
		left: '50%',
		margin: '0 -101px',
		boxShadow: '0px 0px 2px 0px rgba(0, 0, 0, 0.4)',
		hover: {
			textAlign: 'center',
			borderBottom: '1.5px solid rgb(255,75,100)',
			position: 'relative',
			width: 205,
			height: 20,
			top: '7%',
			left: '50%',
			margin: '0 -101px',
			boxShadow: '0px 0px 3px 0px rgba(0, 0, 0, 0.4)',
		}
	}
	
};

dateField = {
	backgroundColor: 'rgba(0,0,0,0)',
	border: 'none',
	borderBottom: '1.5px solid white',
	minWidth: 110,
	maxWidth: 125,
	minHeight: 20,
	maxHeight: 20,
	color: 'white',
	textAlign: 'left',
	cursor: 'pointer',
	label: {
		fontSize: 11,
		marginTop: 2,
		paddingLeft: 6,
		textAlign: 'left',
		color: 'rgb(220,220,220)'
	}
};

closeButton = {
	position: 'relative',
	float: 'right',
	borderRadius: 7,
	backgroundColor: 'white',
	color: 'black',
	border: 'none',
	cursor: 'pointer',
	fontSize: 16
};

hidden = {
	display: 'none'
};

pointer = {
	color: 'white',
	position: 'absolute',
	left: 300,
	top: -16.5,
	fontSize: 20
};

geoButton = {
	padding: '10px 5px',
	textAlign: 'left',
	color: 'white',
	width: '100%',
	border: 'none',
	backgroundColor: 'rgba(255,255,255,0.2)',
	cursor: 'pointer',
	marginTop: 20,
	highlighted: {
		padding: '10px 7px',
		textAlign: 'left',
		color: 'white',
		width: '100%',
		border: 'none',
		backgroundColor: 'rgba(255,255,255,0.4)',
		cursor: 'pointer',
		marginTop: 20,
		boxShadow: '0px 0px 3px 0px rgba(0, 0, 0, 0.4)',
	},
	pointer: {
		color: 'white',
		position: 'absolute',
		left: 300,
		top: 0,
		fontSize: 20
	}
};

geoInfo = {
	position: 'relative',
	textAlign: 'center',
	backgroundColor: 'white',
	float: 'right',
	right: 30,
	top: 50,
	width: 175,
	borderRadius: 7,
	padding: 10,
	boxShadow: '4px 0px 4px 0px rgba(0, 0, 0, 0.4)',
	a: {
		position: 'relative',
		width: 75,
		left: '50%',
		margin: '0 -37.5px',
		marginBottom: 5,
		display: 'block',
		color: 'blue',
		cursor: 'pointer',
		boxShadow: '4px 0px 4px 0px rgba(0, 0, 0, 0.4)',
	},
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
	tagsButton,
	accessButton,
	submittersButton,
	speciesButton,
	buttonHolder,
	datatypeButton,
	speciesButton,
	addButton,
	tabHolder,
	tab,
	dropdown,
	dropdownContent,
	hidden,
	addDisplay,
	dateField,
	closeButton,
	pointer,
	geoButton,
	geoInfo
};