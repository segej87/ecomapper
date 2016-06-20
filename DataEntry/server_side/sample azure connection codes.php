Server: map-it.database.windows.net,1433 
nSQL Database: geojson
User Name: segej87
PHP Data Objects(PDO)

Sample Code:
try {
	$conn = new PDO ( \"sqlsrv:server = tcp:map-it.database.windows.net,1433; Database = geojson\", \"segej87\", \"{your_password_here}\");
    $conn->setAttribute( PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION );
	}
	catch ( PDOException $e ) {
		print( \"Error connecting to SQL Server.\" );
		die(print_r($e));
	}
		
SQL Server Extension Sample Code:

$connectionInfo = array(\"UID\" => \"segej87@map-it\", \"pwd\" => \"{your_password_here}\", \"Database\" => \"geojson\", \"LoginTimeout\" => 30, \"Encrypt\" => 1, \"TrustServerCertificate\" => 0);
$serverName = \"tcp:map-it.database.windows.net,1433\";
$conn = sqlsrv_connect($serverName, $connectionInfo);