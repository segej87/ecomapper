React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map
import Marker from '../src/components/Marker'
import InfoWindow from '../src/components/InfoWindow'

var Container = React.createClass({
	geoJson: {},
	
	markers: {},
	
  getInitialState: function() {
    return {
      showingInfoWindow: false,
      activeMarker: {},
      selectedPlace: {},
	  hasMarkers: false
    }
  },
  
  loadData: function (userId) {
	  if (userId == null) {
			console.log('Not logged in')
		} else {
			const formData='GUID=' + userId;
		
			var request = new XMLHttpRequest;
			
			var method = 'POST';
			var url = 'http://ecocollector.azurewebsites.net/get_geojson.php';
			
			request.onreadystatechange = (e) => {
				if (request.readyState !== 4) {
					console.log('Ready state: ' + request.readyState);
					return;
				} else {
					console.log('Ready state: ' + request.readyState)
				}

				if (request.status === 200) {
					this.geoJson = JSON.parse(JSON.parse(request.responseText).text[0]);
					
					this.processData();
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
  
  processData: function () {
	  this.markers = this.geoJson.features.map((feature, i) => {
		  return <Marker key={feature.id} onClick={this.onMarkerClick} name={feature.properties.name} position={{lat: feature.geometry.coordinates[1], lng: feature.geometry.coordinates[0]}}/>
	  });
	  
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
  },

  onInfoWindowClose: function() {
    this.setState({
      showingInfoWindow: false,
      activeMarker: null
    })
  },

  onMapClicked: function(props) {
    if (this.state.showingInfoWindow) {
      this.setState({
        showingInfoWindow: false,
        activeMarker: null
      })
    }
  },
  
  componentWillMount: function () {
	  this.loadData(this.props.userInfo.userId)
  },
  
  componentWillReceiveProps: function (nextProps) {
	  if (nextProps.userInfo.userId != this.props.userInfo.userId) {
		  this.loadData(nextProps.userInfo.userId)
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

				<InfoWindow
				  marker={this.state.activeMarker}
				  visible={this.state.showingInfoWindow}
				  onClose={this.onInfoWindowClose}>
					<div>
					  <h1>{this.state.selectedPlace.name}</h1>
					</div>
				</InfoWindow>
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

        <InfoWindow
          marker={this.state.activeMarker}
          visible={this.state.showingInfoWindow}
          onClose={this.onInfoWindowClose}>
            <div>
              <h1>{this.state.selectedPlace.name}</h1>
            </div>
        </InfoWindow>
      </Map>
	  </div>
    )
  }
});

export default GoogleApiWrapper({
  apiKey: Keys.gApi
})(Container)