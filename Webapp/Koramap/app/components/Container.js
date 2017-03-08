React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map
import Marker from '../src/components/Marker'
import Polygon from '../src/components/Polygon'
import InfoWindow from '../src/components/InfoWindow'

var google;
var filterGeos = [];
var lastLoadRecords;

var Container = React.createClass({
  getInitialState: function() {
    return {
      activeMarker: {},
      selectedPlace: {},
	  hasMarkers: false
    }
  },
	
	//TODO: wrap in promsise?
  processData: function (features) {
	  if (features == 'reset') {
		  delete this.markers;
			this.setState({
				hasMarkers: false
			});
		  return;
	  }
		
		console.log('Processing');
		
		let geoFilteredFeats = this.filterGeo(features);
	  
	  if (this.markers) {
		  var currentIds = [];
		  for (var i = 0; i < this.markers.length; i++) {
				currentIds.push(this.markers[i].key);
		  }
		  
		  var featureIds = [];
		  for (var i = 0; i < geoFilteredFeats.length; i++) {
			  featureIds.push(geoFilteredFeats[i].id);
		  }
		  
		  var newMarkers = [];
		  for (var i = 0; i < currentIds.length; i++) {
			  if (featureIds.includes(currentIds[i])) {
				  newMarkers.push(this.markers[i])
			  }
		  }
		  
		  for (var i = 0; i < featureIds.length; i++) {
			  if (!currentIds.includes(featureIds[i])) {
				  const feature = geoFilteredFeats[i];
				  newMarkers.push(
					<Marker key={feature.id} 
					  onClick={this.onMarkerClick} 
					  fuid={feature.id}
					  name={feature.properties.name} 
					  submitter={feature.properties.submitter} 
					  datetime={feature.properties.datetime} 
					  tags={feature.properties.tags} 
					  featureProps={feature.properties} 
					  position={{lat: feature.geometry.coordinates[1], lng: feature.geometry.coordinates[0]}}/>
				  );
			  }
		  }
		  
		  this.markers = newMarkers;
	  } else {
		  this.markers = geoFilteredFeats.map((feature, i) => {
			  return (
			  <Marker key={feature.id} 
			  onClick={this.onMarkerClick} 
			  fuid={feature.id}
			  name={feature.properties.name} 
			  submitter={feature.properties.submitter} 
			  datetime={feature.properties.datetime} 
			  tags={feature.properties.tags} 
			  featureProps={feature.properties} 
			  position={{lat: feature.geometry.coordinates[1], lng: feature.geometry.coordinates[0]}}/>
			  );
		  });
	  }
	  
	  this.setState({
		  hasMarkers: true
	  });
  },
	
	receiveGeo: function (polygons) {
		filterGeos = polygons;
		this.processData(this.props.records.features);
	},
	
	setSelectedGeo: function (names) {
		this.props.setSelectedGeo(names);
	},
	
	//TODO: wrap in promise?
	filterGeo: function (features) {
		if (filterGeos.length == 0) {
			return features;
		}
		
		var geoFilteredFeats = [];
		for (var i = 0; i < features.length; i++) {
			const position = new google.maps.LatLng(features[i].geometry.coordinates[1], features[i].geometry.coordinates[0]);
			for (var j = 0; j < filterGeos.length; j++) {
				var testPoly;
				if (filterGeos[j] instanceof google.maps.Polygon) {
					testPoly = filterGeos[j]
				} else {
					var paths=[];
					if (filterGeos[j].geometry.getType() == 'MultiPolygon') {
						const testPolys = filterGeos[j].geometry.getArray();
						for (var k = 0; k < testPolys.length; k++) {
							testPolys[k].forEachLatLng((LatLng) => {
								paths.push(LatLng);
							});	
						}
					} else if (filterGeos[j].geometry.getType() == 'Polygon') {
						filterGeos[j].geometry.forEachLatLng((LatLng) => {
							paths.push(LatLng);
						})
					}
					testPoly = new google.maps.Polygon({paths: paths});
				}
				
				if (google.maps.geometry.poly.containsLocation(position, testPoly)) {
					geoFilteredFeats.push(features[i]);
					break;
				}
			}
		}
		
		return (geoFilteredFeats);
	},
  
  onMarkerClick: function(props, marker, e) {
    this.setState({
      selectedPlace: props,
      activeMarker: marker
    });
	
	this.props.handleSelected(this.state.selectedPlace);
  },

  onInfoWindowClose: function() {
    this.setState({
			selectedPlace: {},
      activeMarker: null
    })
	
	this.props.handleSelected(this.state.selectedPlace);
  },

  onMapClicked: function(props) {
    if (Object.keys(this.state.selectedPlace).length > 0) {
      this.setState({
				selectedPlace: {},
        activeMarker: null
      })
	  
	  this.props.handleSelected(this.state.selectedPlace);
    }
  },
	
	componentDidUpdate(prevProps, prevState) {
		if (prevProps.google != this.props.google) {
			google = this.props.google;
		}
	},
  
  componentWillReceiveProps: function (nextProps) {
	  if (nextProps.userInfo.userId != this.props.userInfo.userId) {
		  this.props.resetRecords();
	  }
	  
		// TODO: Cut down on processing of features
	  if (nextProps.records.features && nextProps.records.updated != lastLoadRecords) {
			lastLoadRecords = nextProps.records.updated;
			this.processData(nextProps.records.features);
	  } else if (nextProps.records.features == null || Object.keys(nextProps.records.features).length == 0) {
		  this.processData('reset');
	  }
		
		if (nextProps.geoFiltering && !this.props.geoFiltering) {
			this.setState({
				hasMarkers: false
			});
		}
  },
  
  render() {
	if (!this.props.loaded) {
		return <div style={{textAlign: 'center', paddingTop: 75, fontSize: 24, color: 'white'}}>Loading Map...</div>
	}
	
	if (!this.state.hasMarkers || this.props.geoFiltering) {
		return (
			<div>
				<Map google={google}
				  zoom={5}
				  onClick={this.onMapClicked}
				  guid={this.props.userInfo.userId}
					geoFiltering={this.props.geoFiltering}
					onFilter={this.receiveGeo}
					setSelectedGeo={this.setSelectedGeo}>
			  </Map>
			  </div>
		);
	}
	
    return (
      <div>
				<Map google={google}
          zoom={5}
          onClick={this.onMapClicked}
					guid={this.props.userInfo.userId}
					geoFiltering={this.props.geoFiltering}
					onFilter={this.receiveGeo}>
					{this.markers}
				</Map>
	  </div>
    )
  }
});

export default GoogleApiWrapper({
  apiKey: Keys.gApi
})(Container)