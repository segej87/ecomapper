export const loadData = function (userId, filters) {
	const formData='GUID=' + this.props.userInfo.userId + '&filters=' + JSON.stringify(this.state.filters);
		
			var request = new XMLHttpRequest;
			
			var method = 'POST';
			var url = 'http://ecocollector.azurewebsites.net/get_geojson.php';
			
			request.onreadystatechange = (e) => {
				if (request.readyState !== 4) {
					return;
				}

				if (request.status === 200) {
					const geoJsonIn = JSON.parse(request.responseText);
					if (Object.keys(this.state.records).length == 0) {
						console.log('Setting initial data');
						this.setState({
							records: geoJsonIn
						});
					} else {
						var currentRecords = this.state.records;
						var currentFeats = currentRecords.features;
						
						var currentIds = [];
						for (var i = 0; i < currentFeats.length; i++) {
							currentIds.push(currentFeats[i].id);
						}
						
						for (var i = 0; i < geoJsonIn.features.length; i++) {
							if (!currentIds.includes(geoJsonIn.features[i].id)) {
								console.log('Adding feature');
								currentFeats.push(geoJsonIn.features[i]);
							}
						}
						
						newRecords = {type: currentRecords.type, features: currentRecords.features};
						
						this.setState({
							records: newRecords
						});
					}
				} else {
					console.log('Status: ' + request.status);
					console.log('Status text: ' + request.statusText);
				}
			};

			request.open(method, url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(formData);
}