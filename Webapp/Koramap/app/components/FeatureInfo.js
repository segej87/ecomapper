React = require('react');
TabArea = require('./TabArea');
SidebarStyles = require('../styles/map/sidebarStyles');

var FeatureInfo = React.createClass({
	getInitialState: function () {
		return ({
			photo: false,
			activeButtons: 'tags'
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
	
	componentWillReceiveProps: function (nextProps) {
		if (nextProps.selectedPlace.featureProps && nextProps.selectedPlace.featureProps.datatype == 'photo') {
			if (nextProps.selectedPlace.featureProps.filepath != this.props.selectedPlace.featureProps.filepath) {
				this.getInitialState();
			}
			this.setState({
				photo: true
			});
		} else {
			this.getInitialState();
		}
	},
	
	componentDidMount: function () {
		if (this.props.selectedPlace.featureProps && this.props.selectedPlace.featureProps.datatype == 'photo') {
			this.setState({
				photo: true
			});
		}
	},
	
	render: function () {
		var meas;
		
		if (this.props.selectedPlace.featureProps.datatype == 'meas') {
			meas = <p>{this.props.selectedPlace.featureProps.species + ': ' + this.props.selectedPlace.featureProps.value + ' ' + this.props.selectedPlace.featureProps.units}</p>
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