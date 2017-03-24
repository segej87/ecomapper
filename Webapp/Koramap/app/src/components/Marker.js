import React, { PropTypes as T } from 'react'

import { camelize } from '../lib/String'
const evtNames = ['click', 'mouseover', 'recenter', 'dragend'];

const noteIcon = require('../../res/img/markers/note-marker.png');
const measIcon = require('../../res/img/markers/meas-marker.png');
const photoIcon = require('../../res/img/markers/photo-marker.png');
var markerIcon;

var addedMarker = false;

const wrappedPromise = function() {
    var wrappedPromise = {},
        promise = new Promise(function (resolve, reject) {
            wrappedPromise.resolve = resolve;
            wrappedPromise.reject = reject;
        });
    wrappedPromise.then = promise.then.bind(promise);
    wrappedPromise.catch = promise.catch.bind(promise);
    wrappedPromise.promise = promise;

    return wrappedPromise;
}

export class Marker extends React.Component {
  componentDidMount() {
    this.markerPromise = wrappedPromise();
		
    this.renderMarker();
  }

  componentDidUpdate(prevProps, prevState) {
    if ((this.props.map !== prevProps.map) ||
      (this.props.position !== prevProps.position)) {
        if (this.marker) {
            this.marker.setMap(null);
        }
        this.renderMarker();
    }
  }

  componentWillUnmount() {
		if (this.marker) {
      this.marker.setMap(null);
			if (this.props.markerClusterer) {
				this.props.markerClusterer.removeMarker(this.marker);
				addedMarker = false;
			}
    }
  }

  renderMarker() {
		if (this.props.singleMeas) {
			markerIcon = (
				<svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
					<circle cx="100" cy="100" r="100"/>
				</svg>
			);
		} else {
			switch(this.props.featureProps.datatype) {
				case 'note':
					markerIcon = noteIcon;
					break;
				case 'meas':
					markerIcon = measIcon;
					break;
				case 'photo':
					markerIcon = photoIcon;
					break;
				default:
					break;
			}
		}
		
		let {
      map, google, position, mapCenter, icon, label, draggable
    } = this.props;
    if (!google) {
      return null
    }
		
		if (markerIcon == null) {
				markerIcon = icon;
		}

    let pos = position || mapCenter;
    if (!(pos instanceof google.maps.LatLng)) {
      position = new google.maps.LatLng(pos.lat, pos.lng);
    }
		
		var markerSize;
		var markerAnchor;
		markerSize = new google.maps.Size(20, 34);
		markerAnchor = new google.maps.Point(10, 34);
		
		if (!markerIcon) {
			markerIcon = icon;
		} else if (this.props.singleMeas) {
			console.log(this.props.frac);
			let hue = Math.floor(120-(this.props.frac*120));
			let scaledFill = 'hsl('+hue+',80%,50%)';
			let scaledStroke = 'hsl('+hue+',80%,25%)';
			
			markerIcon = {
				path: google.maps.SymbolPath.CIRCLE,
				fillOpacity: 1,
				fillColor: scaledFill,
				strokeWeight: 1,
				strokeColor: scaledStroke,
				scale: 4.5
			}
		} else {
			markerIcon = {
				url: markerIcon, // url
				scaledSize: markerSize, // scaled size
				origin: new google.maps.Point(0,0), // origin
				anchor: markerAnchor // anchor
			};
		}

    const pref = {
      map: map,
      position: position,
      icon: markerIcon,
      label: label,
      draggable: draggable,
			animation: google.maps.Animation.DROP
    };
    this.marker = new google.maps.Marker(pref);
		this.marker.set
		
		if (this.props.markerClusterer && !addedMarker) {
			this.props.markerClusterer.addMarker(this.marker);
		}

    evtNames.forEach(e => {
      this.marker.addListener(e, this.handleEvent(e));
    });

    this.markerPromise.resolve(this.marker);
  }

  getMarker() {
    return this.markerPromise;
  }

  handleEvent(evt) {
		return (e) => {
      const evtName = `on${camelize(evt)}`
      if (this.props[evtName]) {
        this.props[evtName](this.props, this.marker, e);
      }
    }
  }

  render() {
    return null;
  }
}

Marker.propTypes = {
  position: T.object,
  map: T.object
}

evtNames.forEach(e => Marker.propTypes[e] = T.func)

Marker.defaultProps = {
  name: 'Marker',
  datetime: Date()
}

export default Marker
