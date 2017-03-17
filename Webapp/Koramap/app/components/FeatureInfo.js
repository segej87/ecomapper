React = require('react');
TabArea = require('./TabArea');
SidebarStyles = require('../styles/map/sidebarStyles');

let plotTypes = ['box','hist'];

var FeatureInfo = React.createClass({
	getInitialState: function () {
		return ({
			photo: false,
			activeButtons: 'tags',
			plot: null,
			plotIndex: 0
		});
	},
	
	handleDelete: function () {
		this.props.handleDelete();
	},
	
	changeActiveButtons: function () {
		if (this.state.activeButtons == 'tags') {
			this.setState({
				activeButtons: 'access'
			});
		} else {
			this.setState({
				activeButtons: 'tags'
			});
		}
	},
	
	getRImage: function (sesh) {
		const url = 'http://192.168.220.128/ocpu/tmp/' + sesh + '/files/out.svg';
 		method = 'GET';
		
		var request = new XMLHttpRequest;
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}
			
			if (request.status === 200) {
				let imageDat = btoa(unescape(encodeURIComponent(request.response)));
				this.setState({plot: imageDat});
			} else {
				console.log(request.status);
			}
		};
		
		request.open(method, url, true);
		request.send();
	},
	
	getPlot: function (props = this.props) {
		this.setState({plot: false});
		const allVals = props.selectedMeasDist;
		const selVal = props.selectedMeasStand;
		const species = props.selectedPlace.featureProps.species[0];
		const units = props.selectedMeasUnit;
		
		const url = "http://192.168.220.128/ocpu/library/kora.scripts/R/plot"+plotTypes[this.state.plotIndex];
		const method = "POST";
		
		const formData=JSON.stringify({allVals: allVals, selVal: selVal, species: species, units: units});
		
		var request = new XMLHttpRequest;
		
		request.onreadystatechange = (e) => {
			if (request.readyState !== 4) {
				return;
			}
			if (request.status === 200) {
			}
			if (request.status === 201) {
				const sesh = request.response.split('/tmp/')[1].split('/R/')[0];
				this.getRImage(sesh);
			} else {
				console.log(request.status);
				console.log(request.statusText);
			}
		};
		
		request.open(method, url, true);
		request.setRequestHeader("Content-type", "application/json");
		request.send(formData);
	},
	
	changePlotIndex: function (e) {
		let id = e.target.id;
		var newInd = this.state.plotIndex;
		if (id == 'backButton' && newInd > 0) {
			newInd--;
			this.setState({plotIndex: newInd});
		} else if (id == 'nextButton' && newInd < (plotTypes.length-1)) {
			newInd++;
			this.setState({plotIndex: newInd});
		}
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.selectedPlace.featureProps && nextProps.selectedPlace.featureProps.datatype == 'photo') {
			if (nextProps.selectedPlace.featureProps.filepath != this.props.selectedPlace.featureProps.filepath) {
				this.getInitialState();
			}
			this.setState({
				photo: true
			});
		} else if (nextProps.selectedPlace.featureProps && nextProps.selectedPlace.featureProps.datatype == 'meas' && nextProps.selectedPlace.fuid != this.props.selectedPlace.fuid) {
			this.getPlot(nextProps);
		} else if (nextProps.selectedPlace.featureProps && nextProps.selectedPlace.featureProps.datatype != 'meas') {
			this.setState(this.getInitialState());
		}
	},
	
	componentDidUpdate(prevProps, prevState) {
		if (prevState.plotIndex != this.state.plotIndex) {
			this.getPlot()
		}
	},
	
	componentDidMount: function () {
		if (this.props.selectedPlace.featureProps && this.props.selectedPlace.featureProps.datatype == 'photo') {
			this.setState({
				photo: true
			});
		}
		
		if (this.props.selectedPlace.featureProps && this.props.selectedPlace.featureProps.datatype == 'meas') {
			this.getPlot();
		}
	},
	
	render: function () {
		var meas;
		var plot;
		
		if (this.props.selectedPlace.featureProps.datatype == 'meas') {
			var val = this.props.selectedMeasStand;
			var unit = this.props.selectedMeasUnit;
			
			if (val == "NA") {
				val = this.props.selectedPlace.featureProps.value;
				unit = this.props.selectedPlace.featureProps.units;
			}
			meas = <p>{this.props.selectedPlace.featureProps.species + ': ' + val + ' ' + unit}</p>
			
			let srcString;
			if (this.state.plot) {
				srcString = "data:image/svg+xml;base64,"+this.state.plot;
			}
			plot = (
			<div>
				<div style={{width: 290, height: 225, textAlign: 'center', marginTop: 30, marginBottom: 25, borderRadius: '3px 3px 3px 3px'}}>
					<img src={srcString} width="290" height="225" style={{margin: '0 auto', borderColor: '1px solid black'}}/>
				</div>
				<div style={{textAlign: 'center'}}>
					<button style={SidebarStyles.tagsButton} onClick={this.changePlotIndex} id='backButton'>Back</button>
					<button style={SidebarStyles.tagsButton} onClick={this.changePlotIndex} id='nextButton'>Next</button>
				</div>
				</div>
			);
		}
		
		var photo;
		
		if (this.state.photo) {
			photo = <img style={{width: '100%'}} src={this.props.selectedPlace.featureProps.filepath} />
		}
		
		var editArea;
		
		if (this.props.ownFeature) {
			editArea = (
				<div style={{marginTop: 10, paddingTop: 2, paddingLeft: 5, paddingRight: 5, paddingBottom: 2, textAlign: 'center'}}>
					<button style={SidebarStyles.deleteButton} onClick={this.handleDelete}>Delete</button>
				</div>
			);
		}
		
		return (
			<div style={SidebarStyles.featureInfo}>
				<div style={SidebarStyles.titleArea}>
					<h1 style={SidebarStyles.h1}>{this.props.selectedPlace.featureProps.name}</h1>
				</div>
				<div style={{display: 'inline-block', position: 'relative', float: 'right', width: '100%', marginBottom: 10}}>
					<a style={SidebarStyles.a}>{this.props.selectedPlace.featureProps.submitter}</a>
					<div style={{textAlign: 'right', position: 'relative', float: 'right', display: 'inline-block'}}>
						<a style={SidebarStyles.date}>{this.props.selectedPlace.featureProps.datetime}</a>
					</div>
				</div>
				{plot}
				{meas}
				{photo}
				<p style={SidebarStyles.p}>Note:</p>
				<div style={SidebarStyles.noteArea}>
					<p style={SidebarStyles.note}>{this.props.selectedPlace.featureProps.text}</p>
				</div>
				<TabArea activeButtons={this.state.activeButtons} onClick={this.changeActiveButtons} selectedPlace={this.props.selectedPlace}/>
				{editArea}
			</div>
		);
	}
});

module.exports = FeatureInfo;