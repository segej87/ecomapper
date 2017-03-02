var app = {
	position: 'absolute',
	boxSizing: 'border-box',
	margin: 0,
	padding: 0,
	top: 0,
	left: 0,
	right: 0,
	bottom: 0
}

var fadeOut = {
	position: 'absolute',
	boxSizing: 'border-box',
	top: 0,
	left: 0,
	right: 0,
	bottom: 0,
	margin: 0,
	padding: 0,
	backgroundColor: 'rgba(0, 0, 0, 0.75)',
	zIndex: '10000000'
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
	textSize: 30,
	zIndex: '10000001',
	boxShadow: '0px 8px 16px 0px rgba(0, 0, 0, 0.4)',
	textAlign: 'center',
	h1: {
		margin: '30px 40px 20px 40px',
		paddingBottom: 10,
		textAlign: 'center',
		color: '#00004c',
		borderBottom: '1px solid red'
	},
	form: {
		display: 'inline-block',
		textAlign: 'center',
		marginBottom: 5
	},
	input: {
		width: 200,
		height: 20,
		marginBottom: 10,
		textAlign: 'center',
		fontFamily: 'Oswald, sans-serif',
		fontSize: 16,
		borderRadius: 3,
		borderColor: 'rgba(50, 50, 50, 0.25)',
		borderWidth: 1
	},
	button: {
		backgroundColor: 'rgba(255, 75, 100, 0.9)',
		marginTop: 10,
		marginBottom: 10,
		color: 'white',
		fontFamily: 'Lato, Open Sans, sans-serif',
		fontSize: 20,
		height: 40,
		width: 120,
		border: 'none',
		cursor: 'pointer',
		borderRadius: 7,
		boxShadow: '0px 4px 8px 0px rgba(0, 0, 0, 0.2)'
	},
	p: {
		marginTop: 0,
		marginBottom: 5
	},
	a: {
		margin: 'auto',
		marginBottom: 5,
		display: 'block',
		color: 'blue',
		cursor: 'pointer'
	},
	error: {
		backgroundColor: 'rgba(255, 255, 255, 0.5)',
		color: 'red',
		margin: '0 auto 20px auto',
		fontFamily: 'Lato, Open Sans, sans-serif',
		fontSize: 10
	}
}

module.exports = {
	app,
	fadeOut,
	login
};