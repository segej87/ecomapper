/*global JSON */

"use strict";

const server = 'http://192.168.220.128'
const root = '/ocpu';

function rJson(command, args, callback, options) {		
	var opts = options || {},
		url,
		method = args ? "POST" : "GET";
		
	const formData = JSON.stringify(args);
	
	url = server + root + command;
	
	var request = new XMLHttpRequest;
			
	request.onreadystatechange = (e) => {
		if (request.readyState !== 4) {
			return;
		}
		
		if (request.status === 200) {
			callback(request.response);
		} else {
			console.log(request.status);
			console.log(request.statusText);
		}
	};
	
	request.open(method, url, true);
	request.setRequestHeader("Content-type", "application/json");
	request.send(formData);
};

function rPlot(command, args, callback, options) {
	let seshCallback = function (sesh) {
		rData(sesh, '/files/out.svg', callback)
	};
	rSesh(command, args, seshCallback, options);
};

function idw(command, args, callback, options) {
	let seshCallback = function (sesh) {
		rImage(sesh, '/files/out.png', callback)
	};
	rSesh(command, args, seshCallback, options);
};

function rSesh(command, args, callback, options) {
	var opts = options || {},
			url,
			method = args ? "POST" : "GET";
		
	url = server + root + command;
	
	const formData=JSON.stringify(args);
	
	var request = new XMLHttpRequest;
	
	request.onreadystatechange = (e) => {
		if (request.readyState !== 4) {
			return;
		}
		
		if (request.status === 201) {
			let sesh = request.response.split('/tmp/')[1].split('/R/')[0];
			callback(sesh)
		} else {
			console.log('Session response: ' + request.statusText);
		}
	};
	
	request.open(method, url, true);
	request.setRequestHeader("Content-type", "application/json");
	request.send(formData);
};

function rData(sesh, out, callback) {
	const url = server + root + '/tmp/' + sesh + out;
 		let method = 'GET';
		
		var request = new XMLHttpRequest;
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}
			
			if (request.status === 200) {
				let imageDat = btoa(unescape(encodeURIComponent(request.response)));
				callback(imageDat)
			} else {
				callback('Data retrieval response: ' + request.status);
			}
		};
		
		request.open(method, url, true);
		request.send();
};

function rImage(sesh, out, callback) {
	const url = server + root + '/tmp/' + sesh + out;
 		let method = 'GET';
		
		var request = new XMLHttpRequest;
		request.responseType = 'blob';
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}
			
			if (request.status === 200) {
				let imageDat = request.response;
				callback(imageDat)
			} else {
				var reader = new window.FileReader();
				reader.readAsText(request.response);
				reader.onloadend = function () {
					console.log('Image retrieval response ' + reader.result);
				}
			}
		};
		
		request.open(method, url, true);
		request.send();
};

exports.rJson = rJson;
exports.rPlot = rPlot;
exports.idw = idw;
exports.idw = idw;
exports.rSesh = rSesh;
exports.rData = rData;
exports.rImage = rImage;