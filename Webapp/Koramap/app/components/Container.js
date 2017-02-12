React = require('react');
var Keys = require('../res/values').keys;
const mapStyles = require('../styles/map/mapStyles');

var GoogleApiWrapper = require('../src/index').GoogleApiWrapper
var Map = require('../src/index').Map

export class Container extends React.Component {
  render() {
	  const style = {
		  width: '90%',
		  height: '90%',
		  marginLeft: 20
	  }
	  
    if (!this.props.loaded) {
      return <div>Loading...</div>
    }
    return (
      <div>
		<Map google={this.props.google} />
	  </div>
    )
  }
};

export default GoogleApiWrapper({
  apiKey: Keys.gApi
})(Container)