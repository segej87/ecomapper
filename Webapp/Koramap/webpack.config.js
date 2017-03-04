var HTMLWebpackPlugin = require('html-webpack-plugin');
var HTMLWebpackPluginConfig = new HTMLWebpackPlugin({
	template: __dirname + '/app/index.html',
	filename: 'index.html',
	inject: 'body'
});

module.exports = {
	entry: __dirname + '/app/index.js',
	module: {
		loaders: [
		{
		  test: /\.css$/,
		  loader: 'style-loader'
		},
		{
		  test: /\.css$/,
		  loader: 'css-loader',
		  query: {
			modules: true,
			localIdentName: '[name]__[local]___[hash:base64:5]'
		  }
		},
		{
			test: /\.js$/,
			exclude: /node_modules/,
			loader: 'babel-loader'
		},
		{ test: /\.(jpe?g|png|gif|svg)$/i, loader: 'file-loader' }
		]
	},
	output: {
		filename: 'transformed.js',
		path: __dirname + '/build'
	},
	plugins: [HTMLWebpackPluginConfig]
	};