<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

require_once 'vendor\autoload.php';
use WindowsAzure\Common\ServicesBuilder;
use WindowsAzure\Common\ServiceException;

define("ACCOUNTNAME", "ecomapper");
define("ACCOUNTKEY", "c0h6WIRF2ObRNWwAkp9arNRLb1KUa0/fZwnKohRwgZfrbVca5WXPxIqJKPeSVyK1oPdAgbIghCpPJNayrId1tw==");

if (isset($_POST['GUID']) && isset($_POST['FILENAME']) && isset($_POST['FILE']))
{
    define("CONTAINERNAME", $_POST['GUID']);
    define("BLOBNAME", $_POST['FILENAME']);
    define("FILE", $_POST['FILE']);

    define("BLOCKSIZE", 4 * 1024 * 1024);    // Size of the block, modify if needed.
    define("PADLENGTH", 5);                  // Size of the string used for the block ID, modify if needed.

    try {
        echo "Beginning processing.\n";

        $connectionString = "DefaultEndpointsProtocol=http;AccountName=" . ACCOUNTNAME . ";AccountKey=" . ACCOUNTKEY;

        $blobRestProxy = ServicesBuilder::getInstance()->createBlobService($connectionString);
    //    createContainerIfNotExists($blobRestProxy);
        echo "Using the '" . CONTAINERNAME . "' container and the '" . BLOBNAME . "' blob.\n";
        echo "Using file '" . FILE . "'\n";
        if (!file_exists(FILE))
        {
            echo "The '" . FILE . "' file does not exist. Exiting program.\n";
            exit();        
        }

        $content = fopen(FILE, "r");

        //Upload blob
        $blobRestProxy->createBlockBlob(CONTAINERNAME, BLOBNAME, $content);

        echo "Done processing.\n";
    }
    catch(ServiceException $serviceException)
    {
        // Handle exception based on error codes and messages.
        // Error codes and messages are here: 
        // http://msdn.microsoft.com/en-us/library/windowsazure/dd179439.aspx
        echo "ServiceException encountered.\n";
        $code = $serviceException->getCode();
        $error_message = $serviceException->getMessage();
        echo "$code: $error_message";
    }
    catch (Exception $exception) 
    {
        echo "Exception encountered.\n";
        $code = $exception->getCode();
        $error_message = $exception->getMessage();
        echo "$code: $error_message";
    }
}
else
{
    echo "Required inputs missing.";
}
?>
