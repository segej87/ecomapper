import React, { PropTypes as T } from 'react'

import { camelize } from '../lib/String'
const evtNames = ['click', 'mouseover', 'recenter', 'dragend'];

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

export class Polygon extends React.Component {

  componentDidMount() {
    this.polygonPromise = wrappedPromise();
    this.renderPolygon();
  }

  componentDidUpdate(prevProps) {
    if ((this.props.map !== prevProps.map) ||
      (this.props.paths !== prevProps.paths)) {
        if (this.polygon) {
            this.polygon.setMap(null);
        }
        this.renderPolygon();
    }
  }

  componentWillUnmount() {
    if (this.polygon) {
      this.polygon.setMap(null);
    }
  }

  renderPolygon() {
    let {
      map, google, paths, mapCenter, icon, label, draggable
    } = this.props;
    if (!google) {
      return null
    }

    let pos = paths || mapCenter;
    if (!(pos[0] instanceof google.maps.LatLng)) {
			var posArray = [];
			for (var i = 0; i < pos.length; i++) {
				posArray.push(new google.maps.LatLng(pos[i].lat, pos[i].lng));
			}
      paths = posArray; 
    }

    const pref = {
      map: map,
      paths: testPaths,
			strokeColor: '#FF0000',
			strokeOpacity: 0.8,
			strokeWeight: 2,
			fillColor: '#FF0000',
			fillOpacity: 0.35
    };
    this.polygon = new google.maps.Polygon(pref);

    evtNames.forEach(e => {
      this.polygon.addListener(e, this.handleEvent(e));
    });

    this.polygonPromise.resolve(this.polygon);
  }

  getPolygon() {
    return this.polygonPromise;
  }

  handleEvent(evt) {
    return (e) => {
      const evtName = `on${camelize(evt)}`
      if (this.props[evtName]) {
        this.props[evtName](this.props, this.polygon, e);
      }
    }
  }

  render() {
    return null;
  }
}

Polygon.propTypes = {
  paths: T.array,
  map: T.object
}

evtNames.forEach(e => Polygon.propTypes[e] = T.func)

Polygon.defaultProps = {
  name: 'Polygon',
  datetime: Date()
}

export default Polygon
