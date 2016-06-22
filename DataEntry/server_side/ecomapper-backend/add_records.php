<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

// array for JSON response
$response = array();
        
// Connect to mysql database
try
{
    $connectionInfo = array("UID" => "ecoCollector@map-it", "pwd" => "{173394aBzZqR!}", "Database" => "geojson", "LoginTimeout" => 30, "Encrypt" => 1, "TrustServerCertificate" => 0, "CharacterSet" => "UTF-8");
    $serverName = "tcp:map-it.database.windows.net,1433";
    $conn = sqlsrv_connect($serverName, $connectionInfo);
    if($conn == false)
    {
       echo "Error connecting to SQL Server";
    }
}
catch(Exception $e)
{
    echo $e.text();
}

/* temporary values for testing:
 * 
 */
$_POST['GUID'] = "7b5586d5-b297-473f-adbc-ec352ede4f26";
$_POST['geojson'] = '{
 "type": "FeatureCollection", 
 "features": [ 
  {
   "geometry": {
    "type": "Point", 
    "coordinates": [
     -58.4622398, 
     -34.6166197, 
     0
    ]
   }, 
   "type": "Feature", 
   "properties": {
    "name": "Second streamwater nitrate measurement", 
    "tags": "water;streamwater", 
    "datatype": "meas", 
    "value": 312.93, 
    "datetime": "2016-06-07 14:56:15", 
    "access": "institution", 
    "units": "ug/l", 
    "species": "nitrate"
   }
  },
  {
   "geometry": {
    "type": "Point", 
    "coordinates": [
     -99.2252831, 
     19.3197409, 
     0
    ]
   }, 
   "type": "Feature", 
   "properties": {
    "name": "dirty well", 
    "tags": "well water;water;thesis", 
    "datatype": "note", 
    "text": "there is a very dirty well here", 
    "datetime": "2016-06-08 09:55:03", 
    "access": "institution"
   }
  },
  {
   "geometry": {
    "type": "Point", 
    "coordinates": [
     -99.2252831, 
     19.3197409, 
     0
    ]
   }, 
   "type": "Feature", 
   "properties": {
    "name": "well-dirty.jpg", 
    "tags": "water;well water;dirty", 
    "datatype": "photo", 
    "filepath": "c:/users/jon sege/dropbox/kumpi mayu/mapdev/samplePhotos/", 
    "datetime": "2013-08-11 10:49:00", 
    "access": "institution"
   }
  }
 ]
}';

// check for post data
if (isset($_POST['GUID']) && isset($_POST['geojson']))
{
    $guid = $_POST['GUID'];
    $geojson = $_POST['geojson'];
    
    // get the current geojson corresponding to the user
    $tsql1 = "SELECT geojsonText FROM personal WHERE UID = (?)";
    $params1 = array($guid);
    $result = sqlsrv_query($conn, $tsql1, $params1, array("Scrollable"=>"buffered"));
    
    // check for empty result
    if (!empty($result))
    {
        if (sqlsrv_num_rows($result) == 1)
        {
            $result = sqlsrv_fetch_array($result);
            
            $geojsonOld = json_decode($result["geojsonText"], true);
            
            // read the current features from the sql database and count
            $old_feats = $geojsonOld['features'];
            $num_old_feats = count($old_feats);
            
            // read the new features from the POST and count
            $new_feats = json_decode($geojson, true)['features'];
            $num_new_feats = count($new_feats);
            
            // check if any of the new features are already in the sql database
            $samesArray = array();
            if ($num_new_feats > 0) {
                for ($j = 0; $j < $num_new_feats; ++$j) {
                    $newfeat = $new_feats[$j];
                    if ($num_old_feats > 0) {
                        for ($i = 0; $i < $num_old_feats; ++$i) {
                            $oldfeat = $old_feats[$i];
                            unset($oldfeat['id']);
                            if ($newfeat == $oldfeat) {
                                array_push($samesArray, $j);
                                break;
                            }
                        }
                    }
                }
            }
            
            // remove any identical features and recount
            for ($i = 0; $i < count($samesArray); ++$i) {
                unset($new_feats[$samesArray[$i]]);
            }
            $num_new_clean_feats = count($new_feats);
            
            if ($num_new_clean_feats == 0) {
                die("No new records detected");
            }
            
            // get old indexes
            $oldInds = [];
            for ($i = 0; $i < count($old_feats); ++$i) {
                array_push($oldInds, $old_feats[$i]['id']);
            }
            
            // assign indexes to the new features, making sure they are unique
            for ($i = 0; $i < count($new_feats); ++$i) {
                $newId = rand(1000000, 9999999);
                while (in_array($newID, $oldInds)) {
                    $newId = rand(1000000, 9999999);
                    echo $newId;
                }
                $new_feats[$i]['id'] = $newId;
                array_push($oldInds, $newId);
            }
            
            // combine old and new features
            $addFeats = array_merge($old_feats, $new_feats);
            
            // check processes
            $checkArray = array();
            
            // check that all old features were added back
            for ($i = 0; $i < count($old_feats); ++$i) {
                if (!in_array($old_feats[$i], $addFeats)) {
                    array_push($checkArray, "Old feature " . $i . " not added");
                }
            }
            
            for ($i = 0; $i < count($new_feats); ++$i) {
                if (!in_array($new_feats[$i], $addFeats)) {
                    array_push($checkArray, "New feature " . $i . " not added");
                }
            }
            
            for ($i = 0; $i < count($addFeats); ++$i) {
                if (!array_key_exists('id', $addFeats[$i])) {
                    array_push($checkArray, "Key not added to feature " . $i);
                }
            }
            
            if (count($checkArray) > 0) {
                die(implode(", ", $checkArray));
            }
            
            // replace old features with new
            $geojsonNew = $geojsonOld;
            unset($geojsonNew['features']);
            $geojsonNew['features'] = $addFeats;
            
            // check that new geojson string's features array is correct
            if ($geojsonNew['features'] != $addFeats) {
                die("The attempt to add new features failed");
            }
            
            // json encode the new info to send
            $geojsonOut = json_encode($geojsonNew);
            
             // write the new geojson file to the server
            $tsql2 = "UPDATE personal SET geojsonText = (?) WHERE UID = (?)";
            $params2 = array($geojsonOut, $guid);
            $stmt = sqlsrv_query($conn, $tsql2, $params2);
            if ($stmt === false ) {
                die( print_r (sqlsrv_errors(), true));
            } else {
                echo "Success! Report: Old features: " . $num_old_feats . 
                        ". New features: " . $num_new_feats . 
                        ". New clean features: " . $num_new_clean_feats;
            }
        }
        else
        {
            // text not found
            $response["message"] = "Error: geojson not found";	
            echo $response["message"];
        }
    }
    else
    {
        // text not found
        $response["message"] = "Error: geojson not found";	
        echo $response["message"];
    }
}
else
{
    // required field is missing
    $response["message"] = "Error: Required field is missing";
    echo $response["message"];
}

try
{
    sqlsrv_close($conn);
}
catch (Exception $ex)
{
    echo $ex.text();
}