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
            $geojsonOut = json_encode($geojsonNew, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
            
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