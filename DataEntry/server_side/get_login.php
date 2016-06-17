<?php
 
/*
 * Following code will get login row
 * matching supplied username
 */
 
// array for JSON response
$response = array();

function OpenConnection() {
    try
	{
	// Connecting to mysql database
	$serverName = "tcp:map-it.database.windows.net,1433";
	$connectionOptions = array("Database"=>"geojson", "Uid"=>"segej87", "PWD"=>"J0nathan5!");
	$conn = sqlsrv_connect($serverName, $connectionOptions);
	if($conn == false)
		die(FormatErrors(sqlsrv_errors()));
	}
	catch(Exception $e)
	{
		echo("Error!");
	}
}
 
// Connecting to mysql database
$con = OpenConnection();
 
// check for post data
if (isset($_POST["username"]) && isset($_POST["password"])) {
    $username = $_POST["username"];
	$password = $_POST["password"];
 
    // get a product from products table
	$tsql = "SELECT * FROM Login WHERE Username =\"" . $username . "\"";
    $result = sqlsrv_query($con, $tsql);
 
    if (!empty($result)) {
        // check for empty result
        if (sqlsrv_num_rows($result) > 0) {
 
            $result = sqlsrv_fetch_array($result);
 
            $login = array();
            $login["UID"] = $result["UID"];
            $login["username"] = $result["username"];
            $login["password"] = $result["password"];
			
			if ($login["password"] == $password) {
				$response["UID"] = $result["UID"];
				
				echo $response["UID"];
			} else {
				// Password doesn't match
				$response["message"] = "Error: The provided username & password combination do not match any on record";
				
				echo $response["message"];
			}
        } else {
            // Username not found
            $response["message"] = "Error: " . $username  . " not found";
			
			echo $response["message"];
        }
    } else {
        // Username not found
        $response["message"] = "Error: " . $username . " not found";
		
		echo $response["message"];
    }
} else {
    // required field is missing
    $response["message"] = "Error: Required field is missing";
	
	echo $response["message"];
}

sqlsrv_close($con);

?>