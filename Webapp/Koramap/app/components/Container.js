React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');
const Geoutils = require('../src/utils/Geoutils');

let MarkerClusterer = require('marker-clusterer-plus');

let RastersLayer = require('./RastersLayer').default;

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map
import Marker from '../src/components/Marker'
import Polygon from '../src/components/Polygon'
import InfoWindow from '../src/components/InfoWindow'

var google;
var lastLoadRecords;
var rastersLayer;

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
	  if (features == 'reset' || features == null) {
		  delete this.markers;
			delete this.markerClusterer;
			this.setState({
				hasMarkers: false
			});
		  return;
	  }
		
		console.log('Processing');
		
		let singleMeas = this.props.filters.datatype.length == 1 && this.props.filters.datatype[0] == 'Meas' && this.props.filters.species.length == 1;
		
		// add test raster
		// if (google && this.props.shapesLayer.getSelectedGeos().length > 0) {rastersLayer.idw(this.props.shapesLayer.getSelectedGeos());}
		
		let filteredGeos = Geoutils.filterGeo(this.props.shapesLayer.getSelectedGeos(), features, google);
		let geoFilteredFeats = filteredGeos.geoFilteredFeats;
		this.props.appState.setWorkingSet(filteredGeos.workingSet);
		let maxVal;
		let minVal;
		if (singleMeas) {
			let dist = this.props.appState.getMeasDist();
			maxVal = dist[0];
			minVal = dist[0];
			for (var j = 0; j < dist.length; j++) {
				if (dist[j] > maxVal) {
					maxVal = dist[j]
				}
				if (dist[j] < minVal) {
					minVal = dist[j]
				}
			}
		}
		
		if (this.props.appState.getMap() && clustering) {
			this.markerClusterer = new MarkerClusterer(this.props.appState.getMap());
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
						frac = (this.props.appState.getStandVals()[this.props.appState.getStandIds().indexOf(feature.id)]-minVal)/(maxVal-minVal);
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
					frac = (this.props.appState.getStandVals()[this.props.appState.getStandIds().indexOf(feature.id)]-minVal)/(maxVal-minVal);
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
	
	onStartDrawShape: function (type) {
		this.props.onStartDrawShape(type);
	},
	
	showNewShapeDialog: function (overlay) {
		this.props.showNewShapeDialog(overlay);
	},
	
  onMarkerClick: function(props, marker, e) {
    this.setState({
      selectedPlace: props,
      activeMarker: marker
    });
	
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
	
	componentDidMount() {
		rastersLayer = new RastersLayer(this.props.appState);
		rastersLayer.setGoogle(this.props.google);
	},
	
	componentDidUpdate(prevProps, prevState) {
		if (prevProps.google != this.props.google) {
			google = this.props.google;
			rastersLayer.setGoogle(google);
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
			
	  } else if (nextProps.records.features == null || Object.keys(nextProps.records.features).length == 0) {
		  this.processData('reset');
	  }
		
		if (nextProps.geoFiltering && !this.props.geoFiltering) {
			this.setState({
				hasMarkers: false
			});
			
			this.props.shapesLayer.startGeoFilter(nextProps.geoFiltering);
		} else if (this.props.geoFiltering && nextProps.geoFiltering == null) {
			this.props.shapesLayer.stopGeoFilter();
			this.processData(this.props.records.features);
		}
		
		if (nextProps.drawingShape) {
			this.props.shapesLayer.startDrawingShape(nextProps.drawingShape, this.showNewShapeDialog, this.onStartDrawShape)
		} else if (this.props.drawingShape) {
			this.props.shapesLayer.stopDrawingShape(this.onStartDrawShape)
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
						appState={this.props.appState}
						shapesLayer={this.props.shapesLayer}>
					</Map>
				</div>
			);
		}
		
			return (
				<div>
					<Map google={google}
						zoom={5}
						onClick={this.onMapClicked}
						appState={this.props.appState}
						shapesLayer={this.props.shapesLayer}>
						{this.markers}
					</Map>
			</div>
			)
		}
	});

export default GoogleApiWrapper({
  apiKey: Keys.gApi,
	libraries: ['places','drawing']
})(Container)