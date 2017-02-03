var app = {
	position: 'absolute',
	top: 0,
	left: 0,
	right: 0,
	bottom: 0,
	boxSizing: 'border-box',
	margin: 0,
	padding: 0
}

var fadeOut = {
	position: 'absolute',
	top: 0,
	left: 0,
	right: 0,
	bottom: 0,
	boxSizing: 'border-box',
	margin: 0,
	padding: 0,
	backgroundColor: 'rgba(0, 0, 0, 0.75)',
	zIndex: '1000'
}

var login = {
	position: 'fixed',
	width: 300,
	height: 350,
	top: '50%',
	left: '50%',
	marginTop: '-195px',
	marginLeft: '-150px',
	fontFamily: 'Oswald, sans-serif',
	backgroundColor: 'white',
	borderRadius: 300/60,
	border: 'none',
	textSize: 50,
	zIndex: '1001',
	backgroundColor: 'rgb(250, 250, 250)',
	boxShadow: '0px 8px 16px 0px rgba(0, 0, 0, 0.4)',
	h1: {
		marginTop: 40,
		textAlign: 'center',
		color: '#00004c'
	},
	form: {
		textAlign: 'center',
		marginBottom: 5
	},
	input: {
		width: 200,
		height: 20,
		textAlign: 'center',
		fontFamily: 'Oswald, sans-serif',
		fontSize: 16,
		borderRadius: 3,
		borderColor: 'rgba(100, 100, 100, 0.25)',
		borderWidth: 2
	},
	button: {
		backgroundColor: 'rgba(255, 75, 100, 0.9)',
		color: 'white',
		fontFamily: 'Lato, Open Sans, sans-serif',
		fontSize: 20,
		height: 40,
		width: 120,
		border: 'none',
		cursor: 'pointer',
		borderRadius: 7,
		boxShadow: '0px 4px 8px 0px rgba(0, 0, 0, 0.2)'
	}
}

module.exports = {
	app,
	fadeOut,
	login
};