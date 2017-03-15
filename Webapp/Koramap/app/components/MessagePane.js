React = require('react');
SidebarToggle = require('./SidebarToggle');
FeatureInfo = require('./FeatureInfo');
SidebarStyles = require('../styles/map/sidebarStyles');

var MessagePane = React.createClass({
	getInitialState: function () {
		return {
			open: false,
			toggleOpen: false
		};
	},
	
	openChange: function (result) {
		this.setState({
			open: result,
			toggleOpen: result
		});
	},
	
	handleDelete: function () {
		if (this.props.selectedPlace.featureProps) {
			console.log('Deleting ' + JSON.stringify(this.props.selectedPlace.fuid));
			const formData='FUID=' + this.props.selectedPlace.fuid + '&type=' + -1;
		
			var request = new XMLHttpRequest;
			
			var method = 'POST';
			var url = 'http://ecocollector.azurewebsites.net/change_data.php';
			
			request.onreadystatechange = (e) => {
				if (request.readyState !== 4) {
					return;
				}

				if (request.status === 200) {
					this.props.handleDelete(this.props.selectedPlace.fuid);
				} else {
					console.log('Status: ' + request.status);
					console.log('Status text: ' + request.statusText);
				}
			};

			request.open(method, url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
		}
	},
	
	componentWillReceiveProps: function (nextProps) {
		if (Object.keys(nextProps.selectedPlace).length > 0 && nextProps.selectedPlace.fuid != this.props.selectedPlace.fuid) {
			if (!this.state.open) {
				this.setState({
					open: true
				});
			}
		} else if (Object.keys(nextProps.selectedPlace).length == 0 && !this.state.toggleOpen) {
			if (this.state.open) {
				this.setState({
					open: false
				});
			}
		}
	},
	
	render: function () {
		var body;
		
		if (this.props.selectedPlace.featureProps) {
			const ownFeature = this.props.userInfo.userName == this.props.selectedPlace.featureProps.submitter;
			body = (
		<FeatureInfo selectedPlace={this.props.selectedPlace} handleDelete={this.handleDelete} ownFeature={ownFeature} selectedMeasDist = {this.props.selectedMeasDist} selectedMeasStand={this.props.selectedMeasStand} selectedMeasUnit={this.props.selectedMeasUnit}/>
			);
		}
		
		if (this.state.open) {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<div style={SidebarStyles.sidebarOpen.message}>
						{body}
					</div>
					<SidebarToggle type="message" onClick = {this.openChange} open={this.state.open} />
				</div>
			);
		} else {
			return (
				<div style={SidebarStyles.sidebarContainer}>
					<SidebarToggle type="message" onClick = {this.openChange} open={this.state.open} />
				</div>
			);
		}
	}
});

module.exports = MessagePane;