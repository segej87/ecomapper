import React, {PropTypes as T} from 'react';
import ReactDOM from 'react-dom'
import { camelize } from './lib/String'
import {makeCancelable} from './lib/cancelablePromise'
import invariant from 'invariant'
let Serverutils = require('../src/utils/Serverutils');
let Geoutils = require('../src/utils/Geoutils');
let Rutils = require('../src/utils/Rutils');
Numbers = require('../res/values').numbers;

var MarkerClusterer = require('marker-clusterer-plus');

const mapStyles = {
  container: {
    position: 'absolute',
    width: '100vw',
    height: Numbers.mapHeight
  },
  map: {
    position: 'absolute',
    left: Numbers.toggleWidth,
    right: 0,
    bottom: 0,
    top: 0
  }
}

const evtNames = [
  'ready',
  'click',
  'dragend',
  'recenter',
  'bounds_changed',
  'center_changed',
  'dblclick',
  'dragstart',
  'heading_change',
  'idle',
  'maptypeid_changed',
  'mousemove',
  'mouseout',
  'mouseover',
  'projection_changed',
  'resize',
  'rightclick',
  'tilesloaded',
  'tilt_changed',
  'zoom_changed'
];

var geoFilters;
var selectedGeos = [];
var selectedGeoNames = [];
var currentGeoLen = 0;
var clickListener;
var hoverListener;
var leaveListener;

export {wrapper as GoogleApiWrapper} from './GoogleApiComponent'
export {Marker} from './components/Marker'
export {InfoWindow} from './components/InfoWindow'
export {HeatMap} from './components/HeatMap'

export class Map extends React.Component {
    constructor(props) {
        super(props)

        invariant(props.hasOwnProperty('google'),
                    'You must include a `google` prop.');

        this.listeners = {}
		
        this.state = {
          currentLocation: {
            lat: this.props.initialCenter.lat,
            lng: this.props.initialCenter.lng
          },
					hasGeos: false
        }
    }

    componentDidMount() {
			if (this.props.centerAroundCurrentLocation) {
        if (navigator && navigator.geolocation) {
          this.geoPromise = makeCancelable(
            new Promise((resolve, reject) => {
              navigator.geolocation.getCurrentPosition(resolve, reject);
            })
          );

        this.geoPromise.promise.then(pos => {
            const coords = pos.coords;
            this.setState({
              currentLocation: {
                lat: coords.latitude,
                lng: coords.longitude
              }
            })
          }).catch(e => e);
        }
      }
      this.loadMap();
    }

    componentDidUpdate(prevProps, prevState) {
			if (prevProps.google !== this.props.google) {
        this.loadMap();
      }
      if (this.props.visible !== prevProps.visible) {
        this.restyleMap();
      }
      if (this.props.zoom !== prevProps.zoom) {
        this.map.setZoom(this.props.zoom);
      }
      if (this.props.center !== prevProps.center) {
        this.setState({
          currentLocation: this.props.center
        })
      }
      if (prevState.currentLocation !== this.state.currentLocation) {
        this.recenterMap();
      }
    }

    componentWillUnmount() {
      const {google} = this.props;
      if (this.geoPromise) {
        this.geoPromise.cancel();
      }
      Object.keys(this.listeners).forEach(e => {
        google.maps.event.removeListener(this.listeners[e]);
      });
    }
		
		componentWillReceiveProps(nextProps) {
			if (nextProps.geoFiltering && !this.state.hasGeos) {
				this.startGeoFilter(nextProps);
			} else if (nextProps.geoFiltering == null && this.state.hasGeos && geoFilters && geoFilters.length > 0) {
				this.stopGeoFilter();
			}
			
			if (nextProps.drawingShape) {
				this.startDrawingShape(nextProps.drawingShape)
			} else if (this.props.drawingShape) {
				this.stopDrawingShape()
			}
		}
		
		startDrawingShape(type) {
			this.drawingManager = new google.maps.drawing.DrawingManager({
				drawingMode: google.maps.drawing.OverlayType[type.toUpperCase()],
				drawingControl: false,
				circleOptions: {
					fillColor: '#ffff00',
					fillOpacity: 1,
					strokeWeight: 5,
					clickable: false,
					editable: true,
					zIndex: 1
				},
				polygonOptions: {
					fillColor: '#009cde',
					fillOpacity: 0.5,
					strokeColor: '#009cde',
					editable: true
				},
				polylineOptions: {
					strokeWeight: 5
				}
			});
			
			let completion = function (e) {
				this.stopDrawingShape();
				let overlay = e.overlay;
				overlay.type = e.type;
				this.props.showNewShapeDialog(overlay);
			}.bind(this);
			
			let cancel = function (e) {
				console.log(e.code)
				if (e.code == '0x0001') {
					this.stopDrawingShape();
				}
			}.bind(this);
			
			google.maps.event.addListener(this.drawingManager, 'overlaycomplete', completion);
			
			this.drawingManager.setMap(this.map);
		}
		
		stopDrawingShape() {
			this.drawingManager.setMap(null);
			this.props.onStartDrawShape(null);
			// google.maps.event.removeListener(cancelShape);
		}
		
		startGeoFilter(nextProps) {
			switch (nextProps.geoFiltering) {
				case 'countries':
					this.startGeoActive(require('../res/json/countries.geo.json'));
					break;
				case 'usstates':
					this.startGeoActive(require('../res/json/us-states.geo.json'));
					break;
				default:
					let callback = function (result) {
						this.startGeoActive(result);
					}.bind(this);
					Serverutils.loadShapes(nextProps.geoFiltering, callback);
			}			
		}
		
		startGeoActive(geos) {
			this.setupActiveGeos();
			
			geoFilters = this.map.data.addGeoJson(geos);
			
			this.setState({
				hasGeos: true
			});
		}
		
		stopGeoFilter() {
			let selectedGeoIds = [];
			for (var i = 0; i < Object.keys(selectedGeos).length; i++) {
				selectedGeoIds.push(selectedGeos[Object.keys(selectedGeos)[i]].id)
			}
			
			for (var i = 0; i < geoFilters.length; i++) {
				if (!selectedGeoIds.includes(geoFilters[i].getId())) {
					this.map.data.remove(geoFilters[i]);
				}
			}
			
			this.setupInactiveGeos();
			
			geoFilters = null;
			
			this.setState({
				hasGeos: false
			});
			
			this.props.onFilter(selectedGeos);
		}
		
		setupActiveGeos() {
			this.map.data.setStyle(function(feature) {
				var color = 'gray';
				if (feature.getProperty('isColorful')) {
					color = 'green';
				}
				return /** @type {google.maps.Data.StyleOptions} */({
					fillColor: color,
					strokeColor: color,
					strokeWeight: 2,
					clickable: true
				});
			});

			clickListener = this.map.data.addListener('click', function(event) {
				event.feature.setProperty('isColorful', !event.feature.getProperty('isColorful'));
				
				var testArray = [];
				for (var i = 0; i < selectedGeos.length; i++) {
					testArray.push(selectedGeos[i].getId());
				}
				
				if (testArray.includes(event.feature.getId())) {
					selectedGeos.splice(testArray.indexOf(event.feature.getId()), 1);
					selectedGeoNames.splice(testArray.indexOf(event.feature.getId()), 1);
				} else {
					selectedGeos.push(event.feature);
					selectedGeoNames.push(event.feature.getProperty('name'))
				}
				
				document.getElementById('geoinfodescrip').innerHTML = selectedGeoNames.join(", ");
			});

			hoverListener = this.map.data.addListener('mouseover', function(event) {
				this.map.data.revertStyle();
				this.map.data.overrideStyle(event.feature, {strokeWeight: 8});
			});

			leaveListener = this.map.data.addListener('mouseout', function(event) {
				this.map.data.revertStyle();
			});
		}
		
		setupInactiveGeos() {
			// Color each shape gray. Change the color when the isColorful property
			// is set to true.
			this.map.data.setStyle(function(feature) {
				return /** @type {google.maps.Data.StyleOptions} */({
					fillColor: 'rgba(0,0,0,0)',
					strokeColor: 'red',
					strokeWeight: 2,
					clickable: false
				});
			});
			
			google.maps.event.removeListener(clickListener);
			google.maps.event.removeListener(hoverListener);
			google.maps.event.removeListener(leaveListener);
		}

    loadMap() {
      if (this.props && this.props.google) {
        const {google} = this.props;
        const maps = google.maps;

        const mapRef = this.refs.map;
        const node = ReactDOM.findDOMNode(mapRef);
        const curr = this.state.currentLocation;
        const center = new maps.LatLng(curr.lat, curr.lng);

        const mapTypeIds = this.props.google.maps.MapTypeId || {};
        const mapTypeFromProps = String(this.props.mapType).toUpperCase();

        const mapConfig = Object.assign({}, {
          mapTypeId: mapTypeIds[mapTypeFromProps],
          center: center,
          zoom: this.props.zoom,
          maxZoom: this.props.maxZoom,
          minZoom: this.props.maxZoom,
          clickableIcons: this.props.clickableIcons,
          disableDefaultUI: this.props.disableDefaultUI,
          zoomControl: this.props.zoomControl,
          mapTypeControl: this.props.mapTypeControl,
          scaleControl: this.props.scaleControl,
          streetViewControl: this.props.streetViewControl,
          panControl: this.props.panControl,
          rotateControl: this.props.rotateControl,
          scrollwheel: this.props.scrollwheel,
          draggable: this.props.draggable,
          keyboardShortcuts: this.props.keyboardShortcuts,
          disableDoubleClickZoom: this.props.disableDoubleClickZoom,
          noClear: this.props.noClear,
          styles: this.props.styles,
          gestureHandling: this.props.gestureHandling
        });

        Object.keys(mapConfig).forEach((key) => {
          // Allow to configure mapConfig with 'false'
          if (mapConfig[key] == null) {
            delete mapConfig[key];
          }
        });

        this.map = new maps.Map(node, mapConfig);
				
				this.props.setMap(this.map);

        evtNames.forEach(e => {
          this.listeners[e] = this.map.addListener(e, this.handleEvent(e));
        });
        maps.event.trigger(this.map, 'ready');
        this.forceUpdate();
      }
    }

    handleEvent(evtName) {
      let timeout;
      const handlerName = `on${camelize(evtName)}`

      return (e) => {
        if (timeout) {
          clearTimeout(timeout);
          timeout = null;
        }
        timeout = setTimeout(() => {
          if (this.props[handlerName]) {
            this.props[handlerName](this.props, this.map, e);
          }
        }, 0);
      }
    }

    recenterMap() {
        const map = this.map;

        const {google} = this.props;
        const maps = google.maps;

        if (!google) return;

        if (map) {
          let center = this.state.currentLocation;
          if (!(center instanceof google.maps.LatLng)) {
            center = new google.maps.LatLng(center.lat, center.lng);
          }
          // map.panTo(center)
          map.setCenter(center);
          maps.event.trigger(map, 'recenter')
        }
    }

    restyleMap() {
      if (this.map) {
        const {google} = this.props;
        google.maps.event.trigger(this.map, 'resize');
      }
    }

    renderChildren() {
      const {children} = this.props;

      if (!children) return;
			
			var markers = React.Children.map(children, c => {
        return React.cloneElement(c, {
          map: this.map,
          google: this.props.google,
          mapCenter: this.state.currentLocation
        });
      })
			
			// var markerClusterer = new MarkerClusterer(this.map, markers);
			
			return markers;
    }

    render() {
			const style = Object.assign({}, mapStyles.map, this.props.style, {
        display: this.props.visible ? 'inherit' : 'none',
				textAlign: 'center', 
				paddingTop: 75, 
				fontSize: 24, 
				color: 'white'
      });

      const containerStyles = Object.assign({},
        mapStyles.container, this.props.containerStyle)

      return (
        <div style={containerStyles} className={this.props.className}>
          <div style={style} ref='map'>
            Loading map...
          </div>
          {this.renderChildren()}
        </div>
      )
    }
};

Map.propTypes = {
  google: T.object,
  zoom: T.number,
  centerAroundCurrentLocation: T.bool,
  center: T.object,
  initialCenter: T.object,
  className: T.string,
  style: T.object,
  containerStyle: T.object,
  visible: T.bool,
  mapType: T.string,
  maxZoom: T.number,
  minZoom: T.number,
  clickableIcons: T.bool,
  disableDefaultUI: T.bool,
  zoomControl: T.bool,
  mapTypeControl: T.bool,
  scaleControl: T.bool,
  streetViewControl: T.bool,
  panControl: T.bool,
  rotateControl: T.bool,
  scrollwheel: T.bool,
  draggable: T.bool,
  keyboardShortcuts: T.bool,
  disableDoubleClickZoom: T.bool,
  noClear: T.bool,
  styles: T.array,
  gestureHandling: T.string
}

evtNames.forEach(e => Map.propTypes[camelize(e)] = T.func)

Map.defaultProps = {
  zoom: 5,
  initialCenter: {
    lat: 37.774929,
    lng: -122.419416
  },
  center: {},
  centerAroundCurrentLocation: true,
  style: {},
  containerStyle: {},
  visible: true
}

export default Map;
