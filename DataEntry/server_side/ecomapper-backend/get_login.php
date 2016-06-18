<?php

// array for JSON response
$response = array();
        
// Connect to mysql database
try
{
    $connectionInfo = array("UID" => "ecoCollector@map-it", "pwd" => "{173394aBzZqR!}", "Database" => "geojson", "LoginTimeout" => 30, "Encrypt" => 1, "TrustServerCertificate" => 0);
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
if (isset($_GET['username']) && isset($_GET['password']))
{
    $username = $_GET['username'];
    $password = $_GET['password'];
     // get the row corresponding to the user
    $tsql = "SELECT * FROM login WHERE username ='" . $username . "'";
    $result = sqlsrv_query($conn, $tsql, array(), array("Scrollable"=>"buffered"));
        
    // check for empty result
    if (!empty($result))
    {
        if (sqlsrv_num_rows($result) > 0)
        {
            $result = sqlsrv_fetch_array($result);
            $login = array();
            $login["UID"] = $result["UID"];
            $login["username"] = $result["username"];
            $login["password"] = $result["password"];
            
            if ($login["password"] == $password)
            {
                $response["UID"] = $result["UID"];		
                echo $response["UID"];
            }
            else
            {
                // Password doesn't match
                $response["message"] = "Error: The provided username & password combination do not match any on record";
                echo $response["message"];
            }
        }
        else
        {
            // Username not found
            $response["message"] = "Error: " . $username  . " not found";	
            echo $response["message"];
        }
    }
    else
    {
        // Username not found
        $response["message"] = "Error: " . $username . " not found";
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
