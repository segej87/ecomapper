<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 
 // headers for CORS
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, PUT, POST, DELETE, OPTIONS');
header('Access-Control-Max-Age: 1000');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

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
       echo "Error: connecting to SQL Server";
    }
}
catch(Exception $e)
{
    echo $e.text();
}

// check for post data
if (isset($_POST['GUID']))
{
    $guid = $_POST['GUID'];
    
    // get the user's geojson data
	$tsql = "SELECT geojsonText FROM personal WHERE UID = (?)";
    $params = array($guid);
    $result = sqlsrv_query($conn, $tsql, $params, array("Scrollable"=>"buffered"));
        
    // check for empty result
    if (!empty($result))
    {
        if (sqlsrv_num_rows($result) > 0)
        {
            $text = array();
            while ($row = sqlsrv_fetch_array($result)) {
                $rowText = $row["geojsonText"];
                array_push($text, $rowText);
            }
            
            $response["text"] = $text;
        }
        else
        {
            // Username not found
            $response["text"] = "Warning: geojson not found";	
        }
    }
    
    print_r(json_encode($response));
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