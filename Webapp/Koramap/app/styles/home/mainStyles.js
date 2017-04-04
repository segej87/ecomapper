 main = {
	margin: 0,
	padding: 0,
	textAlign: 'center',
	background: 'url("https://ecomapper.blob.core.windows.net/bg-artwork-etc/LoginImage3.jpg") no-repeat center center',
	backgroundSize: 'cover',
	height: 500
};

container = {
	border: '1px solid #FFF',
	position: 'relative',
	top: 105,
	width: '40%',
	minWidth: 400,
	paddingBottom: 20,
	maxWidth: 940,
	margin: '0 auto'
};

h1 = {
	color: '#fff',
	margin: '0',
	fontSize: 130,
	fontFamily: 'Oswald, sans-serif',
	textTransform: 'uppercase'
};

p = {
	maxWidth: 400,
	backgroundColor: 'rgba(255, 255, 255, 0.5)',
	color: '#00004c',
	margin: '0 auto 20px auto',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: 25,
	fontWeight: '600'
};

button = {
	backgroundColor: 'rgba(255, 75, 100, 0.9)',
	color: 'white',
	fontFamily: 'Lato, Open Sans, sans-serif',
	fontSize: 25,
	height: 50,
	width: 150,
	border: 'none',
	cursor: 'pointer',
	borderRadius: 7,
	boxShadow: '0px 8px 16px 0px rgba(0, 0, 0, 0.4)'
};

module.exports = {
	main,
	container,
	h1,
	p,
	button
};