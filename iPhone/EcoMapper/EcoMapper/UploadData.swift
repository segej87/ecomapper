//
//  UploadData.swift
//  EcoMapper
//
//  Created by Jon on 7/1/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation
import Photos

open class UploadData {
    
    // MARK: Properties
    
    var tableView: RecordTableViewController?
    
    init?(tableView: RecordTableViewController?) {
        self.tableView = tableView
        
        if tableView == nil {
            return nil
        }
    }
    
    // MARK: Upload geojson records
    
    func uploadRecords(_ biggerDict: NSDictionary) {
        do {
            // Try to encode the passed data as JSON
            let biggestDict = try JSONSerialization.data(withJSONObject: biggerDict, options: JSONSerialization.WritingOptions())
            
            // Encode the JSON data as a string for use in POST
            let dataString = NSString(data: biggestDict, encoding: String.Encoding.utf8.rawValue)
            
            print("New records to send: \(dataString)")
            
            // Establish a request to the server-side PHP script, and define the method as POST
            let request = NSMutableURLRequest(url: URL(string: UserVars.recordAddScript)!)
            request.httpMethod = "POST"
            
            // Create the POST string with necessary variables, and put in HTTP body
            let postString = "GUID=\(UserVars.uuid!)&geojson=\(dataString!)"
            request.httpBody = postString.data(using: String.Encoding.utf8)
            
            // Create a session with the PHP script, and attempt to upload records
            let task = URLSession.shared.dataTask(with: request as URLRequest, completionHandler: { data, response, error in
                
                // Make sure there are no errors creating the session and that some data is being passed
                guard error == nil && data != nil else {
                    print("error=\(error!)")
                    return
                }
                
                // Check if HTTP response code is 200 ("OK"). If not, print an error
                if let httpStatus = response as? HTTPURLResponse , httpStatus.statusCode != 200 {
                    print("Status code should be 200, but it's \(httpStatus.statusCode)")
                    print("response = \(response!)")
                }
                
                // Get the PHP script's response to the session
                let responseString = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
                
                print(responseString)
                
                // Once background task finishes, perform post-upload operations
                DispatchQueue.main.async {
                    
                    // Perform post-upload tasks
                    self.postRecordUpload(responseString!)
                }
            }) 
            task.resume()
        } catch let error as NSError {
            print(error)
            
            // Reactivate the sync button
            tableView!.syncButton.isEnabled = true
        }
    }
    
    // MARK: Record helper methods
    
    func postRecordUpload (_ responseString: NSString) {
        // For reference, print the response string to the log
        print(responseString)
        
        // Check if response string contains "Success!" If so, the records were uploaded successfully, and should be deleted form the phone
        if responseString.replacingOccurrences(of: "Success!", with: "") != responseString as String {
            
            // Create an array of UITableView index paths corresponding to records in phone storage
            var iPs = [IndexPath]()
            for i in 0..<tableView!.records.count{
                let iP = IndexPath(row: i, section: 0)
                iPs.append(iP)
            }
            
            // Delete all records
            tableView!.records.removeAll()
            
            // Delete UITableView rows from the index paths above
            tableView!.tableView.deleteRows(at: iPs, with: .fade)
            
            // Save the new (empty) records list
            tableView!.saveRecords()
        }
        
        // Reactivate the sync button
        tableView!.syncButton.isEnabled = true
    }
    
    // MARK: Upload media
    
    func uploadMedia() {
        
        // Get the container for the user, and create on server if necessary
        let container = getContainer()
        
        // Create a dictionary of media names and paths for upload loop
        var mediaList = [String:URL]()
        for i in tableView!.medias.indices {
            mediaList[tableView!.medias[i].mediaName!] = tableView!.medias[i].mediaPath as URL?
        }
        
        // Loop through media dictionary and attempt to upload
        for m in mediaList.keys{
            let mName = m
            let mPath = mediaList[m]
            
            print(mPath)
            
            // PHAsset only works on iOS 8.0 or above
            if #available(iOS 8.0, *) {
                
                // Fetch the image from phone storage using the image URL
                let asset = PHAsset.fetchAssets(withALAssetURLs: [mPath!], options: nil)
                if let result = asset.firstObject {
                    
                    // Create an image manager
                    let imageManager = PHImageManager.default()
                    
                    // Set options for retrieving image data
                    let options = PHImageRequestOptions()
                    options.deliveryMode = PHImageRequestOptionsDeliveryMode.opportunistic
                    
                    // Retrieve image data, and, if retrieved, attempt to upload and delete
                    imageManager.requestImageData(for: result , options: options) { (imageData, dataUTI, orientation, info) -> Void in
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
        let container = blobClient?.containerReference(fromName: containerName)
        
        let condition = NSCondition()
        var containerCreated = false
        
        container?.createContainerIfNotExists(with: AZSContainerPublicAccessType.blob, requestOptions: nil, operationContext: nil) { (error:Error?, created) -> Void in
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
        
        return container!
    }
    
    func uploadBlob(_ mName: String, imageData: Data, container: AZSCloudBlobContainer) {
        // Attempt to upload the image data to the correct blob container
        let blob = container.blockBlobReference(fromName: mName)
        blob.upload(from: imageData, completionHandler: { (error: Error?) -> Void in
            if error == nil {
                // Upload was successful
                print("Blob uploaded")
                
                //  Delete the entry from the media list and save list
                self.tableView!.medias.remove(at: self.tableView!.indexOfMedia(mName))
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
    
    func loadPhoto(_ photoURL: URL) -> UIImage? {
        let recURL = photoURL.path
        if !recURL.isEmpty {
            let np = NSKeyedUnarchiver.unarchiveObject(withFile: recURL) as? NewPhoto
            print(np?.photo)
            return np?.photo
        }
        return nil
    }
    
}
