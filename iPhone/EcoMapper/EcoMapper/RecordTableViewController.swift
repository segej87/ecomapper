//
//  RecordTableViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/21/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import Photos

class RecordTableViewController: UITableViewController {
    
    // MARK: Properties
    
    // Array for holding saved records.
    var records = [Record]()
    
    // Array for holding names and paths to media on device.
    var medias = [Media]()
    
    // URL to PHP script for uploading new records via POST.
    let recordAddScript = "http://ecocollector.azurewebsites.net/add_records.php"
    
    // URL to blob storage Account.
    let blobRootURLString = "https://ecomapper.blob.core.windows.net/"
    
    // MARK: Initialization
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style the navigation bar's background color and button colors.
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.Black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGrayColor()
        
        // Use the edit button item provided by the table view controller.
        navigationItem.leftBarButtonItem = editButtonItem()
        
        // Load any saved records, otherwise, load nothing.
        if let savedRecords = loadRecords() {
            records += savedRecords
        }
        
        // Load any saved media, otherwise, load nothing.
        if let savedMedia = loadMedia() {
            medias += savedMedia
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
            return records.count
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "RecordTableViewCell"
        let cell = tableView.dequeueReusableCellWithIdentifier(cellIdentifier, forIndexPath: indexPath) as! RecordTableViewCell
        
        // Fetches the appropriate record for the data source layout.
        let record = records[indexPath.row]
        
        // Display the name of the record.
        cell.nameLabel.text = record.props["name"] as? String
        
        // If the record is not a photo, display a different icon depending on the datatype.
        let dt = record.props["datatype"] as! String
        if record.photo == nil {
            switch dt {
                case "meas":
                    cell.photoImageView.image = UIImage(named: "measIcon")
                case "note":
                    cell.photoImageView.image = UIImage(named: "noteIcon")
            default:
                cell.photoImageView.image = UIImage(named: "defaultImage")
            }
        } else {
            
            // If the record is a photo datatype, display the image.
            cell.photoImageView.image = record.photo
        }
        
        // Display the datetime associated with the record.
        cell.dateLabel.text = record.props["datetime"] as? String
        
        // Get the tags associated with the record.
        let recTags = record.props["tags"] as! String
        
        // Replace semi-colons with commas for display
        let dispTags = recTags.stringByReplacingOccurrencesOfString(";", withString: ", ", options: NSStringCompareOptions.LiteralSearch, range: nil)
        
        // If the record is a measurement, show the measured item, value, and units.
        // Otherwise show the record tags.
        switch dt {
            case "meas":
                cell.tagLabel.text = "\(record.props["species"]!):  \(record.props["value"]!) \(record.props["units"]!)"
        default:
            cell.tagLabel.text = dispTags
        }
        
        return cell
    }
    
    // Override to support selection of cells in the table view.
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        // Get the selected record, and determine its datatype.
        let selectedRecord = records[indexPath.row]
        let dt = selectedRecord.props["datatype"] as! String
        
        // Perform a segue to show detail depending on the record's datatype.
        if dt == "meas" {
            self.performSegueWithIdentifier("ShowMeasDetail", sender: self)
        } else if dt == "note" {
            self.performSegueWithIdentifier("ShowNoteDetail", sender : self)
        } else if dt == "photo" {
            self.performSegueWithIdentifier("ShowPhotoDetail", sender : self)
        }
    }

    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            
            // Before deleting a photo, the corresponding item in the media list needs to be found and deleted.
            if records[indexPath.row].props["datatype"] as! String == "photo" {
                
                // Get the filepath of the photo record.
                let oldMediaPath = records[indexPath.row].props["filepath"] as! String
                
                // Remove the root URL string to get the media name (media paths are formatted root URL/mediaName.
                let oldMediaName = oldMediaPath.stringByReplacingOccurrencesOfString("\(blobRootURLString)\(UserVars.guid!)/", withString: "")
                
                // Find the index in the media array corresponding to the media name.
                let oldMediaIndex = indexOfMedia(oldMediaName)
                
                // Before deleting the record row, delete the media reference.
                if oldMediaIndex != -1 {
                    medias.removeAtIndex(oldMediaIndex)
                }
            }
            
            // Delete the record from the data source.
            records.removeAtIndex(indexPath.row)
            
            // Save the modified data.
            saveRecords()
            saveMedia()
            
            // Delete the row from the table.
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }

    /*
    // Override to support rearranging the table view.
    override func tableView(tableView: UITableView, moveRowAtIndexPath fromIndexPath: NSIndexPath, toIndexPath: NSIndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */
    
    // MARK: Actions
    
    @IBAction func attemptSync(sender: UIBarButtonItem) {
        
        // Check if the device is connected to a network
        if Reachability.isConnectedToNetwork() {
            
            // Loop through all records and add to features array
            var features = [AnyObject]()
            for i in 0..<records.count {
                let record = records[i]
                
                // Prepare the record to be written in JSON format and add to features array
                let dictItem = record.prepareForJSON()
                features.append(dictItem)
            }
            
            // Add requred formatting for geojson, and start uploading procedure
            let biggerDict = ["type":"FeatureCollection", "features": features]
            uploadRecords(biggerDict)
            
            // Move to an asynchronous thread and upload the media from the media array
            let  priority = DISPATCH_QUEUE_PRIORITY_DEFAULT
            dispatch_async(dispatch_get_global_queue(priority, 0)){
                self.uploadMedia()
            }
        } else {
            
            // If no network connection, show an alert
            if #available(iOS 8.0, *) {
                let alertVC = UIAlertController(title: "No connection detected", message: "Can't sync without a data connection", preferredStyle: .Alert)
                let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                alertVC.addAction(okAction)
                presentViewController(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "No connection detected", message: "Can't sync without a data connection", delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        }
    }
    
    // MARK: Helper methods
    
    // Find the index of an item in the Media array based on the media name
    func indexOfMedia(medianame: String) -> Int {
        var oldMediaIndex = 0
        while oldMediaIndex < medias.count{
            if medias[oldMediaIndex].mediaName == medianame {
                return oldMediaIndex
            }
            oldMediaIndex += 1
        }
        return -1
    }
    
    func uploadRecords(biggerDict: NSDictionary) {
        do {
            // Try to encode the passed data as JSON
            let biggestDict = try NSJSONSerialization.dataWithJSONObject(biggerDict, options: NSJSONWritingOptions())
            
            // Encode the JSON data as a string for use in POST
            let dataString = NSString(data: biggestDict, encoding: NSUTF8StringEncoding)
            
            // Establish a request to the server-side PHP script, and define the method as POST
            let request = NSMutableURLRequest(URL: NSURL(string: recordAddScript)!)
            request.HTTPMethod = "POST"
            
            // Create the POST string with necessary variables, and put in HTTP body
            let postString = "GUID=\(UserVars.guid!)&geojson=\(dataString!)"
            request.HTTPBody = postString.dataUsingEncoding(NSUTF8StringEncoding)
            
            // Create a session with the PHP script, and attempt to upload records
            let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                
                // Make sure there are no errors creating the session and that some data is being passed
                guard error == nil && data != nil else {
                    print("error=\(error!)")
                    return
                }
                
                // Check if HTTP resposne code is 200 ("OK"). If not, print an error
                if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {
                    print("Status code should be 200, but it's \(httpStatus.statusCode)")
                    print("response = \(response!)")
                }
                
                // Get the PHP script's response to the session
                let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
                
                // Once background task finishes, if successful, delete all records using main thread
                dispatch_async(dispatch_get_main_queue()) {
                    
                    // For reference, print the response string to the log
                    print(responseString!)
                    
                    // Check if response string contains "Success!" If so, the records were uploaded successfully, and should be deleted form the phone
                    if responseString!.stringByReplacingOccurrencesOfString("Success!", withString: "") != responseString! {
                        
                        // Create an array of UITableView index paths corresponding to records in phone storage
                        var iPs = [NSIndexPath]()
                        for i in 0..<self.records.count{
                            let iP = NSIndexPath(forRow: i, inSection: 0)
                            iPs.append(iP)
                        }
                        
                        // Delete all records
                        self.records.removeAll()
                        
                        // Delete UITableView rows from the index paths above
                        self.tableView.deleteRowsAtIndexPaths(iPs, withRowAnimation: .Fade)
                        
                        // Save the new (empty) records list
                        self.saveRecords()
                    }
                }
            }
            task.resume()
        } catch let error as NSError {
            print(error)
        }
    }
    
    func uploadMedia() {
        
        // Connect to Azure blob storage container for media
        let connectionString = "DefaultEndpointsProtocol=https;AccountName=ecomapper;AccountKey=c0h6WIRF2ObRNWwAkp9arNRLb1KUa0/fZwnKohRwgZfrbVca5WXPxIqJKPeSVyK1oPdAgbIghCpPJNayrId1tw=="
        let containerName = UserVars.guid!
        
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
        
        // Create a dictionary of media names and paths for upload loop
        var mediaList = [String:NSURL]()
        for i in medias.indices {
            mediaList[medias[i].mediaName!] = medias[i].mediaPath
        }
        
        // Loop through media dictionary
        for m in mediaList.keys{
            let mName = m
            let mPath = mediaList[m]
            
            // PHAsset only works on iOS 8.0 or above
            if #available(iOS 8.0, *) {
                
                // Fetch the image from phone storage using the image URL
                let asset = PHAsset.fetchAssetsWithALAssetURLs([mPath!], options: nil)
                guard let result = asset.firstObject where result is PHAsset else {
                    print("No asset found")
                    return
                }
                
                // Create an image manager
                let imageManager = PHImageManager.defaultManager()
                
                // Set options for retrieving image data
                let options = PHImageRequestOptions()
                options.deliveryMode = PHImageRequestOptionsDeliveryMode.Opportunistic
                
                // Retrieve image data, and, if retrieved, attempt to upload and delete
                imageManager.requestImageDataForAsset(result as! PHAsset, options: options) { (imageData, dataUTI, orientation, info) -> Void in
                    if let imageData = imageData {
                        
                        // Attempt to upload the image data to the correct blob container
                        let blob = container.blockBlobReferenceFromName(mName)
                        blob.uploadFromData(imageData, completionHandler: { (error: NSError?) -> Void in
                            if error == nil {
                                // Upload was successful
                                print("Blob uploaded")
                                
                                //  Delete the entry from the media list and save list
                                self.medias.removeAtIndex(self.indexOfMedia(mName))
                                self.saveMedia()
                                
                                // Check to make sure the deletion occurred succesfully
                                if self.indexOfMedia(mName) == -1 {
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
                }
            } else {
                // TODO: Fallback on earlier versions
            }
        }
    }
    
    // MARK: Navigation

    // Prepare before navigating away from table view
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "ShowMeasDetail" {
            let recordDetailViewController = segue.destinationViewController as! MeasViewController
            
            // Get the cell that generated this segue.
            if let selectedRecordCell = tableView.cellForRowAtIndexPath(tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPathForCell(selectedRecordCell)!
                let selectedRecord = records[indexPath.row]
                recordDetailViewController.record = selectedRecord
            }
        } else if segue.identifier == "ShowNoteDetail" {
            let recordDetailViewController = segue.destinationViewController as! NoteViewController
            
            // Get the cell that generated this segue.
            if let selectedRecordCell = tableView.cellForRowAtIndexPath(tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPathForCell(selectedRecordCell)!
                let selectedRecord = records[indexPath.row]
                recordDetailViewController.record = selectedRecord
            }
        } else if segue.identifier == "ShowPhotoDetail" {
            let recordDetailViewController = segue.destinationViewController as! PhotoViewController
            
            // Get the cell that generated this segue.
            if let selectedRecordCell = tableView.cellForRowAtIndexPath(tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPathForCell(selectedRecordCell)!
                let selectedRecord = records[indexPath.row]
                recordDetailViewController.record = selectedRecord
            }
        }
    }
    
    @IBAction func unwindToRecordList(sender: UIStoryboardSegue) {
        if let sourceViewController = sender.sourceViewController as? MeasViewController, record = sourceViewController.record {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                // Update an existing record.
                records[selectedIndexPath.row] = record
                tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
            } else {
                // Add a new record
                let newIndexPath = NSIndexPath(forRow: records.count, inSection: 0)
                records.append(record)
                tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Bottom)
            }
            
            // Save the records.
            saveRecords()
        } else if let sourceViewController = sender.sourceViewController as? PhotoViewController, record = sourceViewController.record, media = sourceViewController.media {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {

                // Before updating the photo, find its corresponding record in the media list.
                let oldMediaPath = records[selectedIndexPath.row].props["filepath"] as! String
                let oldMediaName = oldMediaPath.stringByReplacingOccurrencesOfString("\(blobRootURLString)\(UserVars.guid!)/", withString: "")
                let oldMediaIndex = indexOfMedia(oldMediaName)
                if oldMediaIndex != -1 {
                    medias[oldMediaIndex] = media
                }
                
                record.props["filepath"] = "\(blobRootURLString)\(UserVars.guid!)/\(media.mediaName!)"
                
                // Update the existing record.
                records[selectedIndexPath.row] = record
                
                tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
            } else {
                // Add a new record
                let newIndexPath = NSIndexPath(forRow: records.count, inSection: 0)
                record.props["filepath"] = "\(blobRootURLString)\(UserVars.guid!)/\(media.mediaName!)"
                records.append(record)
                tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Bottom)
                
                // Add a new media reference
                medias.append(media)
            }
            
            // Save the records.
            saveRecords()
            
            // Save the media.
            saveMedia()
        } else if let sourceViewController = sender.sourceViewController as? NoteViewController, record = sourceViewController.record {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                // Update an existing record.
                records[selectedIndexPath.row] = record
                tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
            } else {
                // Add a new record
                let newIndexPath = NSIndexPath(forRow: records.count, inSection: 0)
                records.append(record)
                tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Bottom)
            }
            
            // Save the records.
            saveRecords()
        }
    }
    
    // MARK: NSCoding
    
    func saveRecords() {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(records, toFile: Record.ArchiveURL.path!)
        
        if !isSuccessfulSave {
            print("Failed to save records...")
        }
    }
    
    func saveMedia() {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(medias, toFile: Media.ArchiveURL.path!)
        
        if !isSuccessfulSave {
            print("Failed to save media...")
        }
    }
    
    func loadRecords() -> [Record]? {
        return NSKeyedUnarchiver.unarchiveObjectWithFile(Record.ArchiveURL.path!) as? [Record]
    }
    
    func loadMedia() -> [Media]? {
        return NSKeyedUnarchiver.unarchiveObjectWithFile(Media.ArchiveURL.path!) as? [Media]
    }
    
}
