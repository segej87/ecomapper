React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');
const Geoutils = require('../src/utils/Geoutils');

let MarkerClusterer = require('marker-clusterer-plus');

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map
import Marker from '../src/components/Marker'
import Polygon from '../src/components/Polygon'
import InfoWindow from '../src/components/InfoWindow'

var google;
var filterGeos = [];
var lastLoadRecords;

const clustering = false;

var Container = React.createClass({
  getInitialState: function() {
    return {
      activeMarker: {},
      selectedPlace: {},
			hasMarkers: false,
			singleMeas: false
    }
  },
	
	//TODO: wrap in promsise?
  processData: function (features) {
	  if (features == 'reset') {
		  delete this.markers;
			delete this.markerClusterer;
			this.setState({
				hasMarkers: false
			});
		  return;
	  }
		
		console.log('Processing');
		
		let singleMeas = this.props.filters.datatype.length == 1 && this.props.filters.datatype[0] == 'Meas' && this.props.filters.species.length == 1;
		
		let filteredGeos = Geoutils.filterGeo(filterGeos, features, google);
		let geoFilteredFeats = filteredGeos.geoFilteredFeats;
		this.props.setWorkingSet(filteredGeos.workingSet);
		let maxVal;
		let minVal;
		if (singleMeas) {
			maxVal = this.props.selectedMeasDist[0];
			minVal = this.props.selectedMeasDist[0];
			for (var j = 0; j < this.props.selectedMeasDist.length; j++) {
				if (this.props.selectedMeasDist[j] > maxVal) {
					maxVal = this.props.selectedMeasDist[j]
				}
				if (this.props.selectedMeasDist[j] < minVal) {
					minVal = this.props.selectedMeasDist[j]
				}
			}
		}
		
		if (this.map & clustering) {
			this.markerClusterer = new MarkerClusterer(this.map);
		}
	  
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
					let frac;
					if (singleMeas) {
						frac = (this.props.standVals[this.props.standIds.indexOf(feature.id)]-minVal)/(maxVal-minVal);
					} else {
						frac = 1;
					}
				  newMarkers.push(
					<Marker key={feature.id} 
						markerClusterer={this.markerClusterer}
						draggable={false}
					  onClick={this.onMarkerClick} 
					  fuid={feature.id}
					  name={feature.properties.name} 
					  submitter={feature.properties.submitter} 
					  datetime={feature.properties.datetime} 
					  tags={feature.properties.tags} 
					  featureProps={feature.properties} 
					  position={{lat: feature.geometry.coordinates[1], lng: feature.geometry.coordinates[0]}}
						singleMeas={this.state.singleMeas}
						frac={frac}/>
				  );
			  }
		  }
		  
		  this.markers = newMarkers;
	  } else {
		  this.markers = geoFilteredFeats.map((feature, i) => {
			let frac;
				if (singleMeas) {
					frac = (this.props.standVals[this.props.standIds.indexOf(feature.id)]-minVal)/(maxVal-minVal);
				} else {
					frac = 1;
				}
			  return (
			  <Marker key={feature.id} 
				markerClusterer={this.markerClusterer}
				draggable={false}
			  onClick={this.onMarkerClick} 
			  fuid={feature.id}
			  name={feature.properties.name} 
			  submitter={feature.properties.submitter} 
			  datetime={feature.properties.datetime} 
			  tags={feature.properties.tags} 
			  featureProps={feature.properties} 
			  position={{lat: feature.geometry.coordinates[1], lng: feature.geometry.coordinates[0]}}
				singleMeas={this.state.singleMeas}
				frac={frac}/>
			  );
		  });
	  }
	  
	  this.setState({
		  hasMarkers: true
	  });
  },
	
	setMap: function (map) {
		this.map = map;
	},
	
	//TODO: move up to MapContainer
	receiveGeo: function (polygons) {
		filterGeos = polygons;
		this.processData(this.props.records.features);
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
		
		if (lastLoadRecords != prevProps.records.updated) {
			if (prevState.singleMeas != this.state.singleMeas) {
				this.processData('reset');
			}
			if (this.props.records.features) {
				this.processData(this.props.records.features);
			}
		}
	},
  
  componentWillReceiveProps: function (nextProps) {
	  if (nextProps.userInfo.userId != this.props.userInfo.userId) {
		  this.props.resetRecords();
	  }
	  
	  if (nextProps.records.features && nextProps.records.updated != lastLoadRecords) {
			lastLoadRecords = nextProps.records.updated;
			
			if (nextProps.filters.datatype.length == 1 && nextProps.filters.datatype[0] == 'Meas' && nextProps.filters.species.length == 1 && !this.state.singleMeas) {
				this.setState({singleMeas: true});
			}
			
			if (this.state.singleMeas && (!(nextProps.filters.datatype.length == 1 && nextProps.filters.datatype[0] == 'Meas') || nextProps.filters.species.length != 1)) {
				this.setState({singleMeas: false});
			}
			
			// this.processData(nextProps.records.features);
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
						setMap={this.setMap}>
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
						onFilter={this.receiveGeo}
						setMap={this.setMap}>
						{this.markers}
					</Map>
			</div>
			)
		}
	});

export default GoogleApiWrapper({
  apiKey: Keys.gApi
})(Container)