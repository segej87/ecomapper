<?php
 
/*
 * Following code will get login row
 * matching supplied username
 */
 
// array for JSON response
$response = array();
 
// import database connection variables
require_once __DIR__ . '/db_config.php';
 
// Connecting to mysql database
$con = mysqli_connect(DB_SERVER, DB_USER, DB_PASSWORD) or die(mysqli_error());
 
// Selecing database
$db = mysqli_select_db($con, DB_DATABASE) or die(mysqli_error()) or die(mysqli_error());
 
// check for post data
if (isset($_POST["username"]) && isset($_POST["password"])) {
    $username = $_POST["username"];
	$password = $_POST["password"];
 
    // get a product from products table
    $result = mysqli_query($con,"SELECT * FROM Login WHERE Username =\"" . $username . "\"");
 
    if (!empty($result)) {
        // check for empty result
        if (mysqli_num_rows($result) > 0) {
 
            $result = mysqli_fetch_array($result);
 
            $login = array();
            $login["UID"] = $result["UID"];
            $login["Username"] = $result["Username"];
            $login["Password"] = $result["Password"];
			
			if ($login["Password"] == $password) {
				// user node
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

mysqli_close($con);

?>