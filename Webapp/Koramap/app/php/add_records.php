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
       echo "Error connecting to server";
    }
}
catch(Exception $e)
{
    echo $e.text();
}

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
			//TODO: Replace with SQL logic
            $samesArray = array();
            if ($num_new_feats > 0) {
                for ($j = 0; $j < $num_new_feats; $j++) {
                    $newfeat = $new_feats[$j];
                    if ($num_old_feats > 0) {
                        for ($i = 0; $i < $num_old_feats; $i++) {
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
            for ($i = 0; $i < count($samesArray); $i++) {
                unset($new_feats[$samesArray[$i]]);
            }
            $clean_feats = array_values($new_feats); // reindex the array
            $num_new_clean_feats = count($clean_feats);
            
            if ($num_new_clean_feats == 0) {
                die("Total in: " . $num_new_feats . ". No new records detected");
            }
            
            // loop to add new rows to records table
            for ($i = 0; $i < $num_new_clean_feats; $i++) {
                $f = $clean_feats[$i];
                $cols_array = array(
                    'type' => 'Feature',
                    'geometry_type' => 'Point',
                    'geometry_lon' => $f['geometry']['coordinates'][0],
                    'geometry_lat' => $f['geometry']['coordinates'][1],
                    'submitter_uuid' => $guid
                    );
                
                if (count($f['geometry']['coordinates']) > 2) {
                    $cols_array['geometry_elev'] = $f['geometry']['coordinates'][2];
                }
                
				//TODO: REPLACE str_replace with other logic to prevent security issues.
                $property_array = $f['properties'];
                $props_out = array();
                for ($j = 0; $j < count(array_keys($property_array)); $j++) {
                    $property_name = 'property_' . array_keys($property_array)[$j];
                    $property_val = $property_array[array_keys($property_array)[$j]];
                    $props_out[$property_name] = str_replace("'","''",$property_val);
                }
                
                $add_cols = array_merge($cols_array, $props_out);
                
                // Remove tags, affiliations, species, and units from the property array, they will be added as
                // linktos in separate tables
                unset($add_cols['property_tags']);
                unset($add_cols['property_access']);
                unset($add_cols['property_species']);
                unset($add_cols['property_units']);
                
                // TODO: prevent SQL injection
                $add_col_keys = '(' . implode(', ',array_keys($add_cols)) . ')';
				$add_col_vals = '(' . str_repeat('?, ', count($add_cols) - 1) . '?)';

                // Insert the new features as rows in the records table
                $tsql_insert = "INSERT INTO records " . $add_col_keys . " OUTPUT Inserted.FUID VALUES " . $add_col_vals;
				$params_insert = array_values($add_cols);
				
                $result_insert = sqlsrv_query($conn, $tsql_insert, $params_insert);
                $result_fetch = sqlsrv_fetch_array($result_insert);
                
                if ($result_insert === false ) {
                    $idTest = "not added";
                    die( print_r (sqlsrv_errors(), true));
                } else {
                    $newId = strtolower($result_fetch["FUID"]);
                    $idTest = $newId . " added";
                    $clean_feats[$i]['id'] = $newId;
                    //echo strtolower($result_fetch["FUID"]);
                }
                
                // Get the feature's tags
                $tags_array = $f['properties']['tags'];
                
                // Loop through tags to add to list (if necessary), and connect to the new feature
                for ($t = 0; $t < count($tags_array); $t++) {
                    $tag = $tags_array[$t];
                    
                    // Check if the tag is already in the tagslist by querying
                    // the tags table, then store the result
                    $tsql_tagcheck = "SELECT TUID FROM tags WHERE text = (?)";
                    $params_tagcheck = array($tag);
                    $result_tagcheck = sqlsrv_query($conn, $tsql_tagcheck, $params_tagcheck);
                    $result_tagfetch = sqlsrv_fetch_array($result_tagcheck);
                    
                    // If the tag is not in the list, insert it
                    if ($result_tagfetch == '') {
                        // Write the new tag to the list, then get the new tag ID
                        $tsql_tagadd = "INSERT INTO tags (text) OUTPUT Inserted.TUID VALUES ((?))";
                        $params_tagadd = array($tag);
                        $result_tagadd = sqlsrv_query($conn, $tsql_tagadd, $params_tagadd);
                        $result_tagid = strtolower(sqlsrv_fetch_array($result_tagadd)[0]);
                    } else {
                        $result_tagid = strtolower($result_tagfetch[0]);
                    }
                    
                    // TODO: Add connection between tag and feature in tagconnections table
                    $tag_connection_array = array($newId, $result_tagid, $guid);
                    $tag_connection_string = "('" . implode("', '",$tag_connection_array) . "')";
                    $tsql_tagconnect = "INSERT INTO tagconnections (FUID, TUID, submitter_uuid) VALUES " . $tag_connection_string;
                    $result_tagconnect = sqlsrv_query($conn, $tsql_tagconnect);
                }
                
                // Loop through access levels to connect to the new feature
                // Get and the feature's access levels
                $access_array = $f['properties']['access'];
                
                // Loop through access to add to list (if necessary), and connect to the new feature
                for ($a = 0; $a < count($access_array); $a++) {
                    $access = $access_array[$a];
                    
                    // Get the UID of the institution from the instititions table
                    $tsql_accesscheck = "SELECT IUID FROM institutions WHERE name = (?)";
                    $params_accesscheck = array($access);
                    $result_accesscheck = sqlsrv_query($conn, $tsql_accesscheck, $params_accesscheck);
                    $result_accessfetch = sqlsrv_fetch_array($result_accesscheck);
                    
                    $result_accessid = strtolower($result_accessfetch[0]);
                    
                    // Add connection between access and feature in accessconnections table
                    $access_connection_array = array($newId, $result_accessid, $guid);
                    $access_connection_string = "('" . implode("', '",$access_connection_array) . "')";
                    $tsql_accessconnect = "INSERT INTO accessconnections (FUID, IUID, submitter_uuid) VALUES " . $access_connection_string;
                    $result_accessconnect = sqlsrv_query($conn, $tsql_accessconnect);
                }
                
                // Connect species (if applicable) to the new feature
                // Check whether the feature has a secies property
                if (array_key_exists('species',$f['properties'])) {
                    // Get the feature's species
                    $species_string = $f['properties']['species'];
                    
                    // Check if the species is already in the specieslist by querying
                    // the species table, then store the result
                    $tsql_speccheck = "SELECT SUID FROM species WHERE name = (?)";
                    $params_speccheck = array($species_string);
                    $result_speccheck = sqlsrv_query($conn, $tsql_speccheck, $params_speccheck);
                    $result_specfetch = sqlsrv_fetch_array($result_speccheck);
                    
                    // If the species is not in the list, insert it
                    if ($result_specfetch == '') {
                        // Write the new species to the list, then get the new species ID
                        $tsql_specadd = "INSERT INTO species (name) OUTPUT Inserted.SUID VALUES ((?))";
                        $params_specadd = array($species_string);
                        $result_specadd = sqlsrv_query($conn, $tsql_specadd, $params_specadd);
                        $result_specid = strtolower(sqlsrv_fetch_array($result_specadd)[0]);
                    } else {
                        $result_specid = strtolower($result_specfetch[0]);
                    }
                    
                    // Add connection between species and feature in speciesconnections table
                    $spec_connection_array = array($newId, $result_specid, $guid);
                    $spec_connection_string = "('" . implode("', '",$spec_connection_array) . "')";
                    $tsql_specconnect = "INSERT INTO speciesconnections (FUID, SUID, submitter_uuid) VALUES " . $spec_connection_string;
                    $result_specconnect = sqlsrv_query($conn, $tsql_specconnect);
                }
                
                // Connect units (if applicable) to the new feature
                // Check whether the feature has a units property
                if (array_key_exists('units',$f['properties'])) {
                    // Get the feature's units
                    $units_string = $f['properties']['units'];

                    // Check if the unit is already in the unitlist by querying
                    // the units table, then store the result
                    $tsql_unitcheck = "SELECT NUID FROM units WHERE text = (?)";
                    $params_unitcheck = array($units_string);
                    $result_unitcheck = sqlsrv_query($conn, $tsql_unitcheck, $params_unitcheck);
                    $result_unitfetch = sqlsrv_fetch_array($result_unitcheck);

                    // If the unit is not in the list, insert it
                    if ($result_unitfetch == '') {
                        // Write the new unit to the list, then get the new unit ID
                        $tsql_unitadd = "INSERT INTO units (text) OUTPUT Inserted.NUID VALUES ((?))";
                        $params_unitadd = array($units_string);
                        $result_unitadd = sqlsrv_query($conn, $tsql_unitadd, $params_unitadd);
                        $result_unitid = strtolower(sqlsrv_fetch_array($result_unitadd)[0]);
                    } else {
                        $result_unitid = strtolower($result_unitfetch[0]);
                    }

                    // Add connection between units and feature in unitconnections table
                    $unit_connection_array = array($newId, $result_unitid, $guid);
                    $unit_connection_string = "('" . implode("', '",$unit_connection_array) . "')";
                    $tsql_unitconnect = "INSERT INTO unitconnections (FUID, NUID, submitter_uuid) VALUES " . $unit_connection_string;
                    $result_unitconnect = sqlsrv_query($conn, $tsql_unitconnect);
                }
            }
            
            // combine old and new features
            $addFeats = array_merge($old_feats, $clean_feats);
            
            // check processes
            $checkArray = array();
            
            // check that all old features were added back
            for ($i = 0; $i < count($old_feats); $i++) {
                if (!in_array($old_feats[$i], $addFeats)) {
                    array_push($checkArray, "Old feature " . $i . " not added");
                }
            }
            
            for ($i = 0; $i < $num_new_clean_feats; $i++) {
                if (!in_array($clean_feats[$i], $addFeats)) {
                    array_push($checkArray, "New feature " . $i . " not added");
                }
            }
            
            for ($i = 0; $i < count($addFeats); $i++) {
                if (!array_key_exists('id', $addFeats[$i])) {
                    array_push($checkArray, "Number of new clean features: " . $num_new_clean_feats);
                    array_push($checkArray, "New ID " . $idTest . " in loop of length " . $indTest);
                    array_push($checkArray, "Key not added to feature " . ($i + 1));
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
            $geojsonOut = json_encode($geojsonNew, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
            
             //TODO: Uncomment after finishing record insert loop
             // write the new geojson file to the server
            $tsql2 = "UPDATE personal SET geojsonText = (?) WHERE UID = (?)";
            $params2 = array($geojsonOut, $guid);
            $stmt = sqlsrv_query($conn, $tsql2, $params2);
            if ($stmt === false ) {
                die( print_r (sqlsrv_errors(), true));
            } else {
                echo "Success! Features before sync: " . $num_old_feats . 
                        ". Total new features: " . $num_new_feats . 
                        ". New features added: " . $num_new_clean_feats;
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
