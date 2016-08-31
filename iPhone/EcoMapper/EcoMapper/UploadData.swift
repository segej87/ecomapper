//
//  UploadData.swift
//  EcoMapper
//
//  Created by Jon on 7/1/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation
import Photos

public class UploadData {
    
    // MARK: Properties
    
    var tableView: RecordTableViewController?
    
    init?(tableView: RecordTableViewController?) {
        self.tableView = tableView
        
        if tableView == nil {
            return nil
        }
    }
    
    // MARK: Upload geojson records
    
    func uploadRecords(biggerDict: NSDictionary) {
        do {
            // Try to encode the passed data as JSON
            let biggestDict = try NSJSONSerialization.dataWithJSONObject(biggerDict, options: NSJSONWritingOptions())
            
            // Encode the JSON data as a string for use in POST
            let dataString = NSString(data: biggestDict, encoding: NSUTF8StringEncoding)
            
            // Establish a request to the server-side PHP script, and define the method as POST
            let request = NSMutableURLRequest(URL: NSURL(string: UserVars.recordAddScript)!)
            request.HTTPMethod = "POST"
            
            // Create the POST string with necessary variables, and put in HTTP body
            let postString = "GUID=\(UserVars.uuid!)&geojson=\(dataString!)"
            request.HTTPBody = postString.dataUsingEncoding(NSUTF8StringEncoding)
            
            // Create a session with the PHP script, and attempt to upload records
            let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                
                // Make sure there are no errors creating the session and that some data is being passed
                guard error == nil && data != nil else {
                    print("error=\(error!)")
                    return
                }
                
                // Check if HTTP response code is 200 ("OK"). If not, print an error
                if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {
                    print("Status code should be 200, but it's \(httpStatus.statusCode)")
                    print("response = \(response!)")
                }
                
                // Get the PHP script's response to the session
                let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
                
                print(responseString)
                
                // Once background task finishes, perform post-upload operations
                dispatch_async(dispatch_get_main_queue()) {
                    
                    // Perform post-upload tasks
                    self.postRecordUpload(responseString!)
                }
            }
            task.resume()
        } catch let error as NSError {
            print(error)
            
            // Reactivate the sync button
            tableView!.syncButton.enabled = true
        }
    }
    
    // MARK: Record helper methods
    
    func postRecordUpload (responseString: NSString) {
        // For reference, print the response string to the log
        print(responseString)
        
        // Check if response string contains "Success!" If so, the records were uploaded successfully, and should be deleted form the phone
        if responseString.stringByReplacingOccurrencesOfString("Success!", withString: "") != responseString {
            
            // Create an array of UITableView index paths corresponding to records in phone storage
            var iPs = [NSIndexPath]()
            for i in 0..<tableView!.records.count{
                let iP = NSIndexPath(forRow: i, inSection: 0)
                iPs.append(iP)
            }
            
            // Delete all records
            tableView!.records.removeAll()
            
            // Delete UITableView rows from the index paths above
            tableView!.tableView.deleteRowsAtIndexPaths(iPs, withRowAnimation: .Fade)
            
            // Save the new (empty) records list
            tableView!.saveRecords()
        }
        
        // Reactivate the sync button
        tableView!.syncButton.enabled = true
    }
    
    // MARK: Upload media
    
    func uploadMedia() {
        
        // Get the container for the user, and create on server if necessary
        let container = getContainer()
        
        // Create a dictionary of media names and paths for upload loop
        var mediaList = [String:NSURL]()
        for i in tableView!.medias.indices {
            mediaList[tableView!.medias[i].mediaName!] = tableView!.medias[i].mediaPath
        }
        
        // Loop through media dictionary and attempt to upload
        for m in mediaList.keys{
            let mName = m
            let mPath = mediaList[m]
            
            print(mPath)
            
            // PHAsset only works on iOS 8.0 or above
            if #available(iOS 8.0, *) {
                
                // Fetch the image from phone storage using the image URL
                let asset = PHAsset.fetchAssetsWithALAssetURLs([mPath!], options: nil)
                if let result = asset.firstObject where result is PHAsset {
                    
                    // Create an image manager
                    let imageManager = PHImageManager.defaultManager()
                    
                    // Set options for retrieving image data
                    let options = PHImageRequestOptions()
                    options.deliveryMode = PHImageRequestOptionsDeliveryMode.Opportunistic
                    
                    // Retrieve image data, and, if retrieved, attempt to upload and delete
                    imageManager.requestImageDataForAsset(result as! PHAsset, options: options) { (imageData, dataUTI, orientation, info) -> Void in
                        if let imageData = imageData {
                            
                            // Attempt to upload the blob to the server
                            self.uploadBlob(mName, imageData: imageData, container: container)
                        }
                    }
                    
                } else if let result = loadPhoto(mPath!) {
                    print("No asset found, trying local folder")
                    
                    let imageData = UIImageJPEGRepresentation(result, 1.0)
                    
                    self.uploadBlob(mName, imageData: imageData!, container: container)
                } else {
                    print("Couldn't upload photo")
                }

//                guard let result = asset.firstObject where result is PHAsset else {
//                    
//                    return
//                }
                

            } else {
                // TODO: Fallback on earlier versions
            }
        }
    }
    
    // MARK: Media helper methods
    
    func getContainer() -> AZSCloudBlobContainer {
        // Connect to Azure blob storage container for media
        let connectionString = "DefaultEndpointsProtocol=https;AccountName=ecomapper;AccountKey=c0h6WIRF2ObRNWwAkp9arNRLb1KUa0/fZwnKohRwgZfrbVca5WXPxIqJKPeSVyK1oPdAgbIghCpPJNayrId1tw=="
        let containerName = UserVars.uuid!
        
        let storageAccount : AZSCloudStorageAccount;
        try! storageAccount = AZSCloudStorageAccount(fromConnectionString: connectionString)
        let blobClient = storageAccount.getBlobClient()
        let container = blobClient.containerReferenceFromName(containerName)
        
        let condition = NSCondition()
        var containerCreated = false
        
        container.createContainerIfNotExistsWithAccessType(AZSContainerPublicAccessType.Blob, requestOptions: nil, operationContext: nil) { (error:NSError?, created) -> Void in
            condition.lock()
            containerCreated = true
            condition.signal()
            condition.unlock()
        }
        
        condition.lock()
        while (!containerCreated) {
            condition.wait()
        }
        condition.unlock()
        
        return container
    }
    
    func uploadBlob(mName: String, imageData: NSData, container: AZSCloudBlobContainer) {
        // Attempt to upload the image data to the correct blob container
        let blob = container.blockBlobReferenceFromName(mName)
        blob.uploadFromData(imageData, completionHandler: { (error: NSError?) -> Void in
            if error == nil {
                // Upload was successful
                print("Blob uploaded")
                
                //  Delete the entry from the media list and save list
                self.tableView!.medias.removeAtIndex(self.tableView!.indexOfMedia(mName))
                self.tableView!.saveMedia()
                
                // Check to make sure the deletion occurred succesfully
                if self.tableView!.indexOfMedia(mName) == -1 {
                    print("Media successfully removed")
                } else {
                    print("Media couldn't be removed")
                }
            } else {
                
                // Print any error that came up
                print("Error: \(error)")
            }
        })
    }
    
    func loadPhoto(photoURL: NSURL) -> UIImage? {
        if let recURL = photoURL.path {
            let np = NSKeyedUnarchiver.unarchiveObjectWithFile(recURL) as? NewPhoto
            print(np?.photo)
            return np?.photo
        }
        return nil
    }
    
}