var navbar = {
	margin: 0,
	padding: 0,
	position: 'fixed',
    background: 'rgba(255, 255, 255, 0.75)',
    width: '100%',
	zIndex: '1'
};

var ul = {
	float: 'right'
};

var a = {
	color: '#00004c',
	fontWeight: '100',
	letterSpacing: 2,
	textTransform: 'uppercase',
	textDecoration: 'none',
	padding: '16.75px 20px',
	fontFamily: 'Oswald, sans-serif',
	cursor: 'pointer',
	highlighted: {
		color: '#00004c',
		fontWeight: '100',
		letterSpacing: 2,
		textTransform: 'uppercase',
		textDecoration: 'none',
		padding: '16.75px 20px',
		fontFamily: 'Oswald, sans-serif',
		background: 'rgba(255, 255, 255, 0.5)',
		cursor: 'pointer'
	}
};

var li = {
	display: 'inline-block'
};

var logo = {
	float: 'left',
	margin: '5px 20px',
	height: 40
};

var dropdown = {
	position: 'relative',
	display: 'inline-block'
};

var dropdownContent = {
	display: 'inline-block',
    position: 'absolute',
    top: 20,
	left: -166,
    backgroundColor: 'rgba(255, 255, 255, 0.75)',
    minWidth: '165px',
    boxShadow: '0px 8px 16px 0px rgba(0, 0, 0, 0.4)',
    zIndex: '10',
    borderRadius: '0 0 2px 2px',
	width: 166,
	a: {
		display: 'inline-block',
		fontWeight: '50',
		letterSpacing: 1,
		textTransform: 'none',
		color: '#00004c',
		textDecoration: 'none',
		padding: '16.75px 20px',
		fontFamily: 'Oswald, sans-serif',
		highlighted: {
			display: 'block',
			fontWeight: '50',
			letterSpacing: 1,
			textTransform: 'none',
			color: '#00004c',
			textDecoration: 'none',
			padding: '16.75px 20px',
			fontFamily: 'Oswald, sans-serif',
			background: 'rgba(255, 255, 255, 0.5)'
	}
	}
};

var hidden = {
	display: 'none'
};

module.exports = {
	navbar,
	ul,
	a,
	li,
	logo,
	dropdown,
	dropdownContent,
	hidden
};