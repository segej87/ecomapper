<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

// Delete after dev
$_POST['GUID'] = '178a7a0d-ca41-4abf-b256-bcc465cb4d67';

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
    
     // get the institutions that the user is connected to
    $tsql = "SELECT name FROM institutions WHERE IUID IN ( "
            . "SELECT IUID FROM institconnections WHERE UUID = (?)) "
            . "ORDER BY name ASC";
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
            
            //$instit_list = json_encode($instits, JSON_PRETTY_PRINT);
            
            $response["institutions"] = $instits;
        }
        else
        {
            // Username not found
            $response["institutions"] = "Error: institutions not found";	
        }
    }
    else
    {
        // Username not found
        $response["institutions"] = "Error: institutions not found";	
    }
    
    // get the tags that the user is connected to
    $tsql_tags = "SELECT text FROM tags WHERE TUID IN ( "
            . "SELECT DISTINCT TUID FROM tagconnections WHERE FUID IN ( "
            . "SELECT FUID FROM accessconnections WHERE IUID IN ( "
            . "SELECT IUID FROM institconnections WHERE UUID = (?)) "
            . "OR (IUID = '01010101-0101-0101-0101-010101010101' AND submitter_uuid = (?)) "
            . "OR IUID = '10101010-1010-1010-1010-101010101010'))"
            . "ORDER BY text ASC";
    $params_tags = array($guid, $guid);
    $result_tags = sqlsrv_query($conn, $tsql_tags, $params_tags, array("Scrollable"=>"buffered"));
    
    // check for empty result
    if (!empty($result_tags))
    {
        if (sqlsrv_num_rows($result_tags) > 0)
        {
            $tags = array();
            while ($row = sqlsrv_fetch_array($result_tags)) {
                $text = $row["text"];
                array_push($tags, $text);
            }
            
            //$instit_list = json_encode($tags, JSON_PRETTY_PRINT);
            
            $response["tags"] = $tags;
        }
        else
        {
            // Username not found
            $response["tags"] = "Error: tags not found";	
        }
    }
    else
    {
        // Username not found
        $response["tags"] = "Error: tags not found";	
    }
    
    // get the species that the user is connected to
    $tsql = "SELECT name FROM species "
            . "ORDER BY name ASC";
    $params = array($guid);
    $result = sqlsrv_query($conn, $tsql, $params, array("Scrollable"=>"buffered"));
        
    // check for empty result
    if (!empty($result))
    {
        if (sqlsrv_num_rows($result) > 0)
        {
            $specs = array();
            while ($row = sqlsrv_fetch_array($result)) {
                $name = $row["name"];
                array_push($specs, $name);
            }
            
            $response["species"] = $specs;
        }
        else
        {
            // Username not found
            $response["institutions"] = "Error: institutions not found";	
        }
    }
    else
    {
        // Username not found
        $response["institutions"] = "Error: institutions not found";	
    }
    
    print_r(json_encode($response,JSON_PRETTY_PRINT));
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