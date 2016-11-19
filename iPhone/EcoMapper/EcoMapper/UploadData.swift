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
    // The calling table view
    var tableView: NotebookViewController?
    
    init(tableView: NotebookViewController?) {
        self.tableView = tableView
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
            let postString = "GUID=\(UserVars.UUID!)&geojson=\(dataString!)"
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
                
                // TODO: make sure this executes so "Local" items are changed to "Server"
                self.getListsUsingUUID(UserVars.UUID!)
                
                // Once background task finishes, perform post-upload operations
                DispatchQueue.main.async {
                    
                    // Perform post-upload tasks
                    self.postRecordUpload(responseString!)
                }
            }) 
            task.resume()
        } catch let error as NSError {
            print(error)
            
            // Reactivate the buttons and table
            enableButtons()
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
    }
    
    // MARK: Upload media
    
    func uploadMedia() {
        
        // Get the container for the user, and create on server if necessary
        let container = getContainer()
        
        // Create a dictionary of media names and paths for upload loop
        var mediaList = [String:URL]()
        for i in tableView!.medias.indices {
            if tableView!.medias[i].marked == true {
                mediaList[tableView!.medias[i].mediaName!] = tableView!.medias[i].mediaPath as URL?
            }
        }
        
        // Loop through media dictionary and attempt to upload
        for m in mediaList.keys{
            
            // Check if the device is connected to WiFi
            guard let reach = NetworkTests.reachability
                else {
                    NetworkTests.setupReachability(nil)
                    return
            }
            
            if UserDefaults.standard.bool(forKey: "PhotoWiFi") && !reach.isReachableViaWiFi() {
                print("Can't reach WiFi")
                break
            }
            
            if !reach.isReachable() {
                print("not connected")
                break
            }
            
            let mName = m
            let mPath = mediaList[m]
            
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
                            self.uploadBlob(mName, imageData: imageData, container: container, mPath: mPath!)
                        }
                    }
                    
                } else if let result = loadPhoto(mPath!) {
                    print("No asset found, trying local folder")
                    
                    let imageData = UIImageJPEGRepresentation(result, 1.0)
                    
                    self.uploadBlob(mName, imageData: imageData!, container: container, mPath: mPath!)
                } else {
                    print("Couldn't upload photo")
                }
                

            } else {
                // TODO: Fallback on earlier versions
            }
        }
        
        tableView?.mediaProgress.stopAnimating()
    }
    
    // MARK: Media helper methods
    
    func getContainer() -> AZSCloudBlobContainer {
        // Connect to Azure blob storage container for media
        let connectionString = "DefaultEndpointsProtocol=https;AccountName=ecomapper;AccountKey=c0h6WIRF2ObRNWwAkp9arNRLb1KUa0/fZwnKohRwgZfrbVca5WXPxIqJKPeSVyK1oPdAgbIghCpPJNayrId1tw=="
        let containerName = UserVars.UUID!
        
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
    
    func uploadBlob(_ mName: String, imageData: Data, container: AZSCloudBlobContainer, mPath: URL) {
        // Attempt to upload the image data to the correct blob container
        let blob = container.blockBlobReference(fromName: mName)
        blob.upload(from: imageData, completionHandler: { (error: Error?) -> Void in
            if error == nil {
                // Upload was successful
                print("Blob uploaded")
                
                //  Delete the entry from the media list and save list
                if self.tableView!.indexOfMedia(mName) != -1 {
                    self.tableView!.medias.remove(at: self.tableView!.indexOfMedia(mName))
                    self.tableView!.saveMedia()
                    self.tableView!.mediaMonitorManager()
                }
                
                // Check to make sure the deletion occurred succesfully
                if self.tableView!.indexOfMedia(mName) == -1 {
                    print("Media successfully removed")
                } else {
                    print("Media couldn't be removed")
                }
                
                // Try to delete the photo file
                do {
                    try FileManager.default.removeItem(at: mPath)
                    NSLog("Deleted photo \(mPath)")
                } catch let error as NSError {
                    NSLog("Could not delete photo \(mPath): \(error.localizedDescription)")
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
    
    // MARK: Get lists from server
    func getListsUsingUUID(_ uuid: String) {
        // Establish a request to the server-side PHP script, and define the method as POST
        let request = NSMutableURLRequest(url: URL(string: UserVars.listScript)!)
        request.httpMethod = "POST"
        
        // Create the POST string with necessary variables, and put in HTTP body
        let postString = "GUID=\(uuid)"
        request.httpBody = postString.data(using: String.Encoding.utf8)
        
        // Create a session with the PHP script, and attempt to login
        let task = URLSession.shared.dataTask(with: request as URLRequest, completionHandler: { data, response, error in
            
            // Make sure there are no errors creating the session and that some data is being passed
            guard error == nil && data != nil else {
                print("error=\(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? HTTPURLResponse , httpStatus.statusCode != 200 {
                print("Unexpected http status code: \(httpStatus.statusCode)")
                print("response = \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
            
            // Perform rest of login procedure after background server session finishes
            DispatchQueue.main.async {
                
                // For reference, print the response string to the log
                print("Response: \(responseString!)")
                
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let listSuccess = responseString! != ""
                
                // If the login attempt was successful, set the structure variable uuid for use by other classes and segue to the record table view controller. If the attempt was unsuccessful, present an alert with the login error.
                if listSuccess {
                    do {
                        // Encode the response string as data, then parse JSON
                        let responseData = responseString!.data(using: String.Encoding.utf8.rawValue)
                        let responseArray = try JSONSerialization.jsonObject(with: responseData!, options: JSONSerialization.ReadingOptions()) as! [String:AnyObject]
                        
                        // Initialize an array of all keys to read from the server response
                        let keys = ["institutions","tags","species","units"]
                        
                        // Read the arrays corresponding to the keys, and write to user variables
                        for k in keys {
                            let kArray = responseArray[k] as! [String]
                            
                            if kArray.count == 1 && kArray[0].contains("Error:") {
                                
                            } else {
                                for i in kArray {
                                    switch k {
                                    case "institutions":
                                        if !UserVars.AccessLevels.contains(i) {
                                            UserVars.AccessLevels.append(i)
                                        }
                                    case "tags":
                                        if !UserVars.Tags.keys.contains(i) || (UserVars.Tags.keys.contains(i) && UserVars.Tags[i]![0] as! String == "Local") {
                                            UserVars.Tags[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    case "species":
                                        if !UserVars.Species.keys.contains(i) || (UserVars.Species.keys.contains(i) && UserVars.Species[i]![0] as! String == "Local") {
                                            UserVars.Species[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    case "units":
                                        if !UserVars.Units.keys.contains(i) || (UserVars.Units.keys.contains(i) && UserVars.Units[i]![0] as! String == "Local") {
                                            UserVars.Units[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    default:
                                        print("Unexpected key")
                                    }
                                }
                            }
                        }
                        
                        UserVars.saveUserVars()
                        
                        // Reactivate the buttons and table
                        self.enableButtons()
                    } catch let error as NSError {
                        print(error.localizedDescription)
                    }
                } else {
                    // Show the error to the user as an alert controller
                    var errorString: String?
                    if responseString!.replacingOccurrences(of: "Error", with: "") != responseString! as String {
                        errorString = responseString!.replacingOccurrences(of: "Error: ",with: "")
                    } else {
                        errorString = "Can't connect to the server - please check your internet connection"
                    }
                    
                    // Present an alert to the user
                    if #available(iOS 9.0, *) {
                        let alertVC = UIAlertController(title: "Login Error", message: "\(errorString!)", preferredStyle: .alert)
                        let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                        alertVC.addAction(okAction)
                        self.tableView!.present(alertVC, animated: true, completion: nil)
                    } else {
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString!)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
        })
        task.resume()
    }
    
    //MARK: General helper methods
    
    func enableButtons () {
        for b in [self.tableView!.syncButton, self.tableView!.logoutButton,self.tableView!.editButtonItem] {
            b!.isEnabled = true
        }
        
        self.tableView!.tableView.allowsSelection = true
    }
}
