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
if (isset($_POST['GUID']))
{
    $guid = $_POST['GUID'];
    
     // get the row corresponding to the user
    $tsql = "SELECT name FROM institutions WHERE IUID IN ( SELECT IUID FROM institconnections WHERE UUID = (?)) ORDER BY name ASC";
    $params = array($guid);
    $result = sqlsrv_query($conn, $tsql, $params, array("Scrollable"=>"buffered"));
        
    // check for empty result
    if (!empty($result))
    {
        if (sqlsrv_num_rows($result) > 0)
        {
            $instits = array();
            while ($row = sqlsrv_fetch_array($result)) {
                $name = $row["name"];
                array_push($instits, $name);
            }
            
            $instit_list = json_encode($instits, JSON_PRETTY_PRINT);
            
            $response["message"] = $instit_list;
            echo $response["message"];
        }
        else
        {
            // Username not found
            $response["message"] = "Error: institutions not found";	
            echo $response["message"];
        }
    }
    else
    {
        // Username not found
        $response["message"] = "Error: institutions not found";	
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