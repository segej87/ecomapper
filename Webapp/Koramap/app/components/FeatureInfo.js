React = require('react');
TabArea = require('./TabArea');
SidebarStyles = require('../styles/map/sidebarStyles');
Rutils = require('../src/utils/Rutils.js');

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
	
	getPlot: function (props = this.props) {	
		let command = '/library/kora.scripts/R/plot'+plotTypes[this.state.plotIndex];
		let args = {allVals: props.appState.getMeasDist(), selVal: props.selectedMeasStand, species: props.selectedPlace.featureProps.species[0], units: props.selectedMeasUnit};
		let callback = function (imageDat) {
			this.setState({plot: imageDat});
		}.bind(this)
		
		Rutils.rPlot(command, args, callback);
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
	
	link: function (data, name){
            var a = document.createElement('a');
            a.download = name || self.location.pathname.slice(self.location.pathname.lastIndexOf('/')+1);
            a.href = data || self.location.href;
            return a;
        },
	
	startDownload: function (e) {
		// console.log('Starting download');
		// console.log(this.props.selectedPlace);
		// let ta = Uint8Array.from([this.props.selectedPlace]);
		// console.log(ta);
		// var blob = new Blob(ta, {type: 'application/octet-binary'});
		// console.log(blob);
		// var l = this.link('data:application/octet-stream;base64,' + btoa(JSON.stringify(this.props.selectedPlace)),'test.txt')
		console.log(this.props.selectedPlace);
		var placeText = JSON.stringify(this.props.selectedPlace.featureProps);
		console.log(placeText);
		
		var l;
		if (this.state.photo) {
			
			// TODO: make a blob from the feature's filepath
			let imageDat = btoa(unescape(encodeURIComponent(this.props.selectedPlace.featureProps.filepath)))
			l = this.link("data:image/jpeg+xml;base64," + imageDat,'test.jpg')
			// return
		} else if (this.state.plot) {
			l = this.link("data:image/svg+xml;base64," + this.state.plot,'test.svg')
		} else {
			return
		}
		
		var ev = document.createEvent("MouseEvents");
    ev.initMouseEvent("click", true, false, self, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    return l.dispatchEvent(ev);
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
			
			if (val == undefined || val == null || val == "NA") {
				val = this.props.selectedPlace.featureProps.value;
				unit = this.props.selectedPlace.featureProps.units;
			}
			meas = <p>{this.props.selectedPlace.featureProps.species + ': ' + val + ' ' + unit}</p>
			
			let srcString;
			if (this.state.plot) {
				srcString = "data:image/svg+xml;base64," + this.state.plot;
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
				<div style={{marginTop: 10, textAlign: 'center'}}>
					<button style={SidebarStyles.deleteButton} onClick={this.startDownload}>Download</button>
				</div>
			</div>
		);
	}
});

module.exports = FeatureInfo;