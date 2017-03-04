var React = require('react');
var DatePicker = require('react-datepicker');
var moment = require('moment');
 
// require('react-datepicker/dist/react-datepicker.css');
 
// CSS Modules, react-datepicker-cssmodules.css 
require('react-datepicker/dist/react-datepicker-cssmodules.css');

var CustomDateInput = React.createClass({
	displayName: "CustomDateInput",

	propTypes: {
	    onClick: React.PropTypes.func,
	    value: React.PropTypes.string
	},

	render () {
		var val = 'None';
		if (this.props.value) {
			val = this.props.value
		}
		
	  return (
	    <button
	      style={SidebarStyles.dateField}
	      onClick={this.props.onClick}>
	      {val}
		</button>
	  )
	}
});

 
var DateField = React.createClass({
  displayName: 'Date',
 
  getInitialState: function() {
	  var initDate;
	  if (this.props.currentVal && this.props.currentVal != 'none') {
		  initDate = moment(this.props.currentVal.toISOString());
	  }
	  
    return {
      startDate: initDate
    };
  },
 
  handleChange: function(date) {
	this.setState({
      startDate: date
    });
	
	if (date == null) {
		this.props.onChange(this.props.type,'none','Replace');
	}
	this.props.onChange(this.props.type, date, 'Replace');
  },
 
  render: function() {
    return <DatePicker
		customInput={<CustomDateInput />}
		isClearable={true}
        selected={this.state.startDate}
        onChange={this.handleChange} />;
  }
});

module.exports = DateField;
