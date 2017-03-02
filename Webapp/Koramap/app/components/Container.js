React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map
import Marker from '../src/components/Marker'
import InfoWindow from '../src/components/InfoWindow'

var Container = React.createClass({
	
  getInitialState: function() {
    return {
      showingInfoWindow: false,
      activeMarker: {},
      selectedPlace: {},
	  hasMarkers: false
    }
  },
  
  processData: function (features) {
	  if (features == 'reset') {
		  delete this.markers;
		  return;
	  }
	  
	  if (this.markers) {
		  var currentIds = [];
		  for (var i = 0; i < this.markers.length; i++) {
			currentIds.push(this.markers[i].key);
		  }
		  
		  var featureIds = [];
		  for (var i = 0; i < features.length; i++) {
			  featureIds.push(features[i].id);
		  }
		  
		  var newMarkers = [];
		  for (var i = 0; i < currentIds.length; i++) {
			  if (featureIds.includes(currentIds[i])) {
				  newMarkers.push(this.markers[i])
			  }
		  }
		  
		  for (var i = 0; i < featureIds.length; i++) {
			  if (!currentIds.includes(featureIds[i])) {
				  const feature = features[i];
				  newMarkers.push(
					<Marker key={feature.id} 
					  onClick={this.onMarkerClick} 
					  fuid={feature.id}
					  name={feature.properties.name} 
					  submitter={feature.properties.submitter} 
					  datetime={feature.properties.datetime} 
					  tags={feature.properties.tags} 
					  featureProps={feature.properties} 
					  position={{lat: feature.geometry.coordinates[0], lng: feature.geometry.coordinates[1]}}/>
				  );
			  }
		  }
		  
		  this.markers = newMarkers;
	  } else {
		  this.markers = features.map((feature, i) => {
			  return (
			  <Marker key={feature.id} 
			  onClick={this.onMarkerClick} 
			  fuid={feature.id}
			  name={feature.properties.name} 
			  submitter={feature.properties.submitter} 
			  datetime={feature.properties.datetime} 
			  tags={feature.properties.tags} 
			  featureProps={feature.properties} 
			  position={{lat: feature.geometry.coordinates[0], lng: feature.geometry.coordinates[1]}}/>
			  );
		  });
	  }
	  
	  this.setState({
		  hasMarkers: true
	  });
  },
  
  onMarkerClick: function(props, marker, e) {
    this.setState({
      selectedPlace: props,
      activeMarker: marker,
      showingInfoWindow: true
    });
	
	this.props.handleSelected(this.state.selectedPlace);
  },

  onInfoWindowClose: function() {
    this.setState({
	  selectedPlace: {},
      showingInfoWindow: false,
      activeMarker: null
    })
	
	this.props.handleSelected(this.state.selectedPlace);
  },

  onMapClicked: function(props) {
    if (this.state.showingInfoWindow) {
      this.setState({
		selectedPlace: {},
        showingInfoWindow: false,
        activeMarker: null
      })
	  
	  this.props.handleSelected(this.state.selectedPlace);
    }
  },
  
  componentWillReceiveProps: function (nextProps) {
	  if (nextProps.userInfo.userId != this.props.userInfo.userId) {
		  this.props.resetRecords();
	  }
	  
	  if (nextProps.records.features) {
		this.processData(nextProps.records.features);
	  } else {
		  this.processData('reset');
	  }
  },
  
  render() {
	if (!this.props.loaded) {
		return <div>Loading...</div>
	}
	
	if (!this.state.hasMarkers) {
		return (
			<div>
				<Map google={this.props.google}
				  zoom={14}
				  onClick={this.onMapClicked}
				  guid={this.props.userInfo.userId}>
			  </Map>
			  </div>
		);
	}
	
    return (
      <div>
		<Map google={this.props.google}
          zoom={14}
          onClick={this.onMapClicked}
		  guid={this.props.userInfo.userId}>
		{this.markers}
      </Map>
	  </div>
    )
  }
});

export default GoogleApiWrapper({
  apiKey: Keys.gApi
})(Container)