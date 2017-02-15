React = require('react');
SidebarStyles = require('../styles/map/sidebarStyles');

var FeatureInfo = React.createClass({
	getInitialState: function () {
		return ({
			photo: false
		});
	},
	
	handleDelete: function () {
		this.props.handleDelete();
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
		var photo;
		
		if (this.state.photo) {
			photo = <img style={{width: '100%'}} src={this.props.selectedPlace.featureProps.filepath} />
		}
		
		var tags = this.props.selectedPlace.featureProps.tags.map((tag, i) => {
			return (
				<button key={'tag_' + this.props.selectedPlace.fuid + i} style={SidebarStyles.tagButton}>{tag}</button>
			);
		});
		
		var access = this.props.selectedPlace.featureProps.access.map((access, i) => {
			return (
				<button key={'access_' + this.props.selectedPlace.fuid + i} style={SidebarStyles.accessButton}>{access}</button>
			);
		});
		
		var editArea;
		
		if (this.props.ownFeature) {
			editArea = (
				<div style={{marginTop: 10, backgroundColor: 'rgba(255, 255, 255, 0.5)', paddingTop: 2, paddingLeft: 5, paddingRight: 5, paddingBottom: 5}}>
					<p style={SidebarStyles.p}>Edit:</p>
					<button style={SidebarStyles.deleteButton} onClick={this.handleDelete}>Delete</button>
				</div>
			);
		}
		
		return (
			<div style={SidebarStyles.featureInfo}>
				<h1 style={SidebarStyles.h1}>{this.props.selectedPlace.featureProps.name}</h1>
				<a href='#' style={SidebarStyles.a}>{this.props.selectedPlace.featureProps.submitter}</a>
				<p style={SidebarStyles.p}>{this.props.selectedPlace.featureProps.datetime}</p>
					{photo}
				<p style={SidebarStyles.p}>{this.props.selectedPlace.featureProps.text}</p>
				<div style={{marginTop: 10, backgroundColor: 'rgba(230, 230, 230, 0.5)', paddingTop: 2, paddingLeft: 5, paddingRight: 5, paddingBottom: 5}}>
					<p style={SidebarStyles.p}>Tags:</p>
					{tags}
				</div>
				<div style={{marginTop: 10, backgroundColor: 'rgba(200, 200, 200, 0.5)', paddingTop: 2, paddingLeft: 5, paddingRight: 5, paddingBottom: 5}}>
					<p style={SidebarStyles.p}>Access levels:</p>
					{access}
				</div>
				{editArea}
			</div>
		);
	}
});

module.exports = FeatureInfo;