const React = require('react');
const SidebarStyles = require('../styles/map/sidebarStyles');
const FilterButton = require('./FilterButton');
const AddDdn = require('./AddDdn');
const DateField = require('./DateField');

var FilterContent = React.createClass({
	getInitialState: function () {
		return ({
			highlightedType: null,
			highlightedItem: null,
			missingFilters: {
				datatype: [],
				submitters: [],
				access: [],
				tags: [],
				species: [],
				date: []
			},
			showingDdn: false,
			ddnItems: [],
			addingType: null
		});
	},
	
	handleAdd: function (e) {
		const label = e.target.id.replace('add_','')
		
		if (this.state.showingDdn && this.state.addingType == label) {
			this.setState({
				showingDdn: false,
				ddnItems: [],
				addingType: null
			});
			
			return;
		}
		
		var showItems = [];
		for (var i = 0; i < this.state.missingFilters[label].length; i++) {
			showItems.push(this.state.missingFilters[label][i]);
		}
		
		this.setState({
			showingDdn: true,
			ddnItems: showItems,
			addingType: label
		});
	},
	
	addItem: function (item) {
		this.props.onFilterChange(this.state.addingType, item, 'Add');
		var index = this.state.ddnItems.indexOf(item);
		
		var newItems =[];
		if (this.state.ddnItems.length <= 1) {
			newItems = [];
		} else {
			for (var i = 0; i < this.state.ddnItems.length; i++) {
				if (i != index) {
					newItems.push(this.state.ddnItems[i]);
				}
			}
		}
		
		this.setState({
			ddnItems: newItems
		});
	},
	
	closeAdd: function () {
		this.setState({
			showingDdn: false,
			ddnItems: [],
			addingType: null
		});
	},
	
	changeHighlightedItem: function (type, i) {
		if (type == this.state.highlightedType && i == this.state.highlightedItem || type == null || i == null) {
			this.setState({
				highlightedType: null,
				highlightedItem: null
			});
		} else {
			this.setState({
				highlightedType: type,
				highlightedItem: i
			});
		}
	},
	
	handleClick: function (type, val, result) {
		this.props.onFilterChange(type, val, result);
		this.setState({
			highlightedType: null,
			highlightedItem: null
		});
	},
	
	handleDragRemove: function (type, val ,result) {
		this.props.onFilterChange(type, val, result);
	},
	
	componentWillReceiveProps: function (nextProps) {
		var missingObject = {};
		for (var i = 0; i < Object.keys(nextProps.filters).length; i++) {
			const label = Object.keys(nextProps.filters)[i];
			
			var missingItems = [];
			for (var j = 0; j < nextProps.lists[label].length; j++) {
				if (!nextProps.filters[label].includes(nextProps.lists[label][j])) {
					missingItems.push(nextProps.lists[label][j]);
				}
			}
			
			missingObject[label] = missingItems;
		}
		
		this.setState({
			missingFilters: missingObject
		});
	},
	
	componentDidMount: function () {
		var missingObject = {};
		for (var i = 0; i < Object.keys(this.props.filters).length; i++) {
			const label = Object.keys(this.props.filters)[i];
			
			var missingItems = [];
			for (var j = 0; j < this.props.lists[label].length; j++) {
				if (!this.props.filters[label].includes(this.props.lists[label][j])) {
					missingItems.push(this.props.lists[label][j]);
				}
			}
			
			missingObject[label] = missingItems;
		}
		
		this.setState({
			missingFilters: missingObject
		});
	},
	
	render: function () {
		const itemArray = ['Remove','Remove all others'];
		const filtersArray = {submitters: 'Submitters',tags: 'Tags',access: 'Access levels',datatype: 'Datatypes',species: 'Species'};
		
		var filterDisplay = [];
		for (var i = 0; i < Object.keys(filtersArray).length; i++) {
			const f = Object.keys(filtersArray)[i];
			
			var pointer;
			if (f == this.state.addingType) {
				pointer = <p style={SidebarStyles.pointer}>&#9664;</p>
			} else {
				pointer = null;
			}
			
			filterDisplay.push(
				<div style={{marginTop: 20}} key={'filters_' + i}>
					<div style={{display: 'inline-block', position: 'relative', float: 'right', width: '100%', marginBottom: 0}}>
						<p style={SidebarStyles.p}>{Object.values(filtersArray)[i]}</p>
						<button style={SidebarStyles.addButton} onClick={this.handleAdd} id={'add_' + f}>+</button>
						{pointer}
					</div>
					<div style={SidebarStyles.buttonHolder} onMouseLeave={this.changeHighlightedItem}>
						{this.props.filters[f].map((item, i) => {
							return (
								<div key={f + '_' + i} style={{display: 'inline-block'}}>
									<FilterButton
									key={f + '_' + i}
									item={item}
									type={f}
									onClick={this.changeHighlightedItem}
									highlightedType={this.state.highlightedType}
									highlightedItem={this.state.highlightedItem}
									handleDragRemove={this.handleDragRemove} />
									<SidebarDropdown
									key={f + 'ddn_' + i}
									type={f}
									item={item}
									highlightedType={this.state.highlightedType}
									highlightedItem={this.state.highlightedItem}
									items={itemArray}
									handleClick={this.handleClick} />
								</div>
							);
						})}
					</div>
				</div>
			);
		}
		
		return (
			<div>
				<AddDdn items={this.state.missingFilters} type={this.state.addingType} highlighted={this.state.showingDdn} onClose={this.closeAdd} onAdd={this.addItem}/>
				<h1 style={SidebarStyles.h1}>Filters</h1>
				{filterDisplay}
				<div style={{marginTop: 20, textAlign: 'center'}}>
					<div style={{display: 'inline-block'}}>
						<DateField type="startDate" onChange={this.handleClick} currentVal={this.props.filters.date[0]}/>
						<p style={SidebarStyles.dateField.label}>Start date</p>
					</div>
					<div style={{display: 'inline-block', width: 50}}></div>
					<div style={{display: 'inline-block'}}>
						<DateField type="endDate" onChange={this.handleClick} currentVal={this.props.filters.date[1]}/>
						<p style={SidebarStyles.dateField.label}>End date</p>
					</div>
				</div>
			</div>
		);
	}
});

export default (FilterContent);