React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map
import Marker from '../src/components/Marker'
import Polygon from '../src/components/Polygon'
import InfoWindow from '../src/components/InfoWindow'

var google;
var filterGeos =[];

var Container = React.createClass({
  getInitialState: function() {
    return {
      showingInfoWindow: false,
      activeMarker: {},
      selectedPlace: {},
	  hasMarkers: false
    }
  },
  
	
	//TODO: wrap in promsise?
  processData: function (features) {
	  if (features == 'reset') {
		  delete this.markers;
		  return;
	  }
		
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
					  position={{lat: feature.geometry.coordinates[0], lng: feature.geometry.coordinates[1]}}/>
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
			  position={{lat: feature.geometry.coordinates[0], lng: feature.geometry.coordinates[1]}}/>
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
	
	//TODO: wrap in promise?
	filterGeo: function (features) {
		if (filterGeos.length == 0) {
			return features;
		}
		
		// var geoFilteredFeats = [];
		// for (var i = 0; i < this.markers.length; i++) {
			// const position = new google.maps.LatLng(this.markers[i].props.position.lat, this.markers[i].props.position.lng);
			// for (var j = 0; j < filterGeos.length; j++) {
				// var paths=[];
				// if (filterGeos[j].geometry.getType() == 'MultiPolygon') {
					// const testPolys = filterGeos[j].geometry.getArray();
					// for (var k = 0; k < testPolys.length; k++) {
						// testPolys[k].forEachLatLng((LatLng) => {
							// paths.push(LatLng);
						// });	
					// }
				// } else if (filterGeos[j].geometry.getType() == 'Polygon') {
					// filterGeos[j].geometry.forEachLatLng((LatLng) => {
						// paths.push(LatLng);
					// })
				// }
				
				// const testPoly = new google.maps.Polygon({paths: paths});
				// if (google.maps.geometry.poly.containsLocation(position, testPoly)) {
					// geoFilteredFeats.push({
							// id: this.markers[i].props.fuid,
							// properties: this.markers[i].props.featureProps,
							// geometry: {
									// coordinates: [this.markers[i].props.position.lat, this.markers[i].props.position.lng]
								// },
							// type: 'Feature'
						// });
					// break;
				// }
				// if (google.maps.geometry.poly.containsLocation(this.markers[i].position, testPoly)) {
					// console.log(this.markers[i].name);
				// }
			// }
		// }
		
		var geoFilteredFeats = [];
		for (var i = 0; i < features.length; i++) {
			const position = new google.maps.LatLng(features[i].geometry.coordinates[0], features[i].geometry.coordinates[1]);
			for (var j = 0; j < filterGeos.length; j++) {
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
				
				const testPoly = new google.maps.Polygon({paths: paths});
				if (google.maps.geometry.poly.containsLocation(position, testPoly)) {
					geoFilteredFeats.push(features[i]);
					break;
				}
				// if (google.maps.geometry.poly.containsLocation(this.markers[i].position, testPoly)) {
					// console.log(this.markers[i].name);
				// }
			}
		}
		
		// this.processData(geoFilteredFeats);
		return (geoFilteredFeats);
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
	
	componentDidUpdate(prevProps, prevState) {
		if (prevProps.google != this.props.google) {
			google = this.props.google;
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
		return <div style={{textAlign: 'center', paddingTop: 75, fontSize: 24, color: 'white'}}>Loading Map...</div>
	}
	
	if (!this.state.hasMarkers || this.props.countryFiltering) {
		return (
			<div>
				<Map google={google}
				  zoom={5}
				  onClick={this.onMapClicked}
				  guid={this.props.userInfo.userId}
					countryFiltering={this.props.countryFiltering}
					onFilter={this.receiveGeo}>
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
					countryFiltering={this.props.countryFiltering}
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