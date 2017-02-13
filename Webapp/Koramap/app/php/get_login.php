<?php

// array for JSON response
$response = array();

// headers for CORS
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, PUT, POST, DELETE, OPTIONS');
header('Access-Control-Max-Age: 1000');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
        
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
if (isset($_POST['username']) && isset($_POST['password']))
{
    $username = $_POST['username'];
    $password = $_POST['password'];
     // get the row corresponding to the user
    $tsql = "SELECT * FROM login WHERE username = (?)";
    $params = array($username);
    $result = sqlsrv_query($conn, $tsql, $params, array("Scrollable"=>"buffered"));
        
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
				$response["firstname"] = $result["firstname"];
				$response["lastname"] = $result["lastname"];	
                print_r(json_encode($response,JSON_PRETTY_PRINT));
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
