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
    
    // Button to send and receive data from server.
    @IBOutlet weak var syncButton: UIBarButtonItem!
    
    // Create an object to store user info after sync
    var loginInfo = LoginInfo(uuid: "", accessLevels: nil, tags: nil, species: nil, units: nil)
    
    // Create an object to store lists retrieved from server
    var listString : NSString?
    
    // Array for holding saved records.
    var records = [Record]()
    
    // Array for holding names and paths to media on device.
    var medias = [Media]()
    
    // MARK: Initialization
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style the navigation bar's background color and button colors.
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGray
        
        // Add an edit button item provided by the table view controller.
        navigationItem.leftBarButtonItem = editButtonItem
        
        // Load any saved records. If none, load nothing.
        if let savedRecords = loadRecords() {
            records += savedRecords
        }
        
        // Load any saved media. If none, load nothing.
        if let savedMedia = loadMedia() {
            medias += savedMedia
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return records.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "RecordTableViewCell"
        let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as! RecordTableViewCell
        
        // Fetches the appropriate record for the data source layout.
        let record = records[(indexPath as NSIndexPath).row]
        
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
        let recTags = record.props["tags"] as? [String]
        
        // Replace semi-colons with commas for display
        let dispTags = recTags!.joined(separator: ", ")
            // recTags.replacingOccurrences(of: ";", with: ", ", options: NSString.CompareOptions.literal, range: nil)
        
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
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        // Get the selected record, and determine its datatype.
        let selectedRecord = records[(indexPath as NSIndexPath).row]
        let dt = selectedRecord.props["datatype"] as! String
        
        // Perform a segue to show detail depending on the record's datatype.
        if dt == "meas" {
            self.performSegue(withIdentifier: "ShowMeasDetail", sender: self)
        } else if dt == "note" {
            self.performSegue(withIdentifier: "ShowNoteDetail", sender : self)
        } else if dt == "photo" {
            self.performSegue(withIdentifier: "ShowPhotoDetail", sender : self)
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
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            
            // Before deleting a photo, the corresponding item in the media list needs to be found and deleted.
            if records[(indexPath as NSIndexPath).row].props["datatype"] as! String == "photo" {
                
                // Get the filepath of the photo record.
                let oldMediaPath = records[(indexPath as NSIndexPath).row].props["filepath"] as! String
                
                // Remove the root URL string to get the media name (media paths are formatted root URL/mediaName.
                let oldMediaName = oldMediaPath.replacingOccurrences(of: "\(UserVars.blobRootURLString)\(UserVars.uuid!)/", with: "")
                
                // Find the index in the media array corresponding to the media name.
                let oldMediaIndex = indexOfMedia(oldMediaName)
                
                // Before deleting the record row, delete the media reference.
                if oldMediaIndex != -1 {
                    medias.remove(at: oldMediaIndex)
                }
            }
            
            // Modify the UserVar lists associated with the record
            let prevArray = records[(indexPath as NSIndexPath).row].props["tags"] as? [String]
            for p in prevArray! {
                var pTag = UserVars.Tags[p]
                if pTag![0] as! String == "Local" {
                    pTag![1] = ((pTag![1] as! Int - 1) as AnyObject)
                    if pTag![1] as! Int == 0 {
                        UserVars.Tags.removeValue(forKey: p)
                    } else {
                        UserVars.Tags[p] = pTag!
                    }
                }
            }
            
            if records[(indexPath as NSIndexPath).row].props["datatype"] as! String == "meas" {
                let prevArray = records[(indexPath as NSIndexPath).row].props["species"]?.components(separatedBy: ";")
                for p in prevArray! {
                    var pTag = UserVars.Species[p]
                    if pTag![0] as! String == "Local" {
                        pTag![1] = ((pTag![1] as! Int - 1) as AnyObject)
                        if pTag![1] as! Int == 0 {
                            UserVars.Species.removeValue(forKey: p)
                        } else {
                            UserVars.Species[p] = pTag!
                        }
                    }
                }
            }
            
            // Delete the record from the data source.
            records.remove(at: (indexPath as NSIndexPath).row)
            
            // Save the modified data.
            saveRecords()
            saveMedia()
            
            // Delete the row from the table.
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
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
    
    @IBAction func attemptSync(_ sender: UIBarButtonItem) {
        
        
        // Temporary
        print(UserVars.Species)
        
        // Deactivate the sync button while uploading
        syncButton.isEnabled = false
        
        // Initialize the class for uploading data
        let ud = UploadData(tableView: self)!
        
        // Check if the device is connected to a network
        if Reachability.isConnectedToNetwork() {
            
            // Loop through all records and add to features array
            var features = [AnyObject]()
            for i in 0..<records.count {
                let record = records[i]
                
                // Prepare the record to be written in JSON format and add to features array
                let dictItem = record.prepareForJSON()
                features.append(dictItem as AnyObject)
            }
            
            // Add requred formatting for geojson, and start uploading procedure
            let biggerDict = ["type":"FeatureCollection", "features": features] as [String : Any]
            ud.uploadRecords(biggerDict as NSDictionary)
            
            // TODO: make sure this executes so "Local" items are changed to "Server"
            getListsUsingUUID(UserVars.uuid!)
            
            // Move to an asynchronous thread and upload the media from the media array
            let  priority = DispatchQueue.GlobalQueuePriority.default
            DispatchQueue.global(priority: priority).async{
                ud.uploadMedia()
            }
        } else {
            
            // If no network connection, show an alert
            if #available(iOS 8.0, *) {
                let alertVC = UIAlertController(title: "No connection detected", message: "Can't sync without a data connection", preferredStyle: .alert)
                let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                alertVC.addAction(okAction)
                present(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "No connection detected", message: "Can't sync without a data connection", delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
            
            // Reactivate the sync button
            syncButton.isEnabled = true
        }
    }
    
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
            self.listString = responseString!
            
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
                        
                        // Write the user variables to the login object and save
                        self.loginInfo?.uuid = UserVars.uuid
                        self.loginInfo?.accessLevels = UserVars.AccessLevels
                        self.loginInfo?.tags = UserVars.Tags
                        self.loginInfo?.species = UserVars.Species
                        self.loginInfo?.units = UserVars.Units
                        
                        self.saveLogin()
                    } catch let error as NSError {
                        print(error.localizedDescription)
                    }
                } else {
                    // Show the error to the user as an alert controller
                    var errorString: String?
                    if self.listString!.replacingOccurrences(of: "Error", with: "") != self.listString! as String {
                        errorString = self.listString!.replacingOccurrences(of: "Error: ",with: "")
                    } else {
                        errorString = "Can't connect to the server - please check your internet connection"
                    }
                    
                    // Present an alert to the user
                    if #available(iOS 9.0, *) {
                        let alertVC = UIAlertController(title: "Login Error", message: "\(errorString!)", preferredStyle: .alert)
                        let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                        alertVC.addAction(okAction)
                        self.present(alertVC, animated: true, completion: nil)
                    } else {
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString!)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
        }) 
        task.resume()
    }
    
    func saveLogin() {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(loginInfo!, toFile: LoginInfo.ArchiveURL.path)
        
        if !isSuccessfulSave {
            print("Failed to save login info...")
        }
    }
    
    // MARK: Helper methods
    
    // Find the index of an item in the Media array based on the media name
    func indexOfMedia(_ medianame: String) -> Int {
        var oldMediaIndex = 0
        while oldMediaIndex < medias.count{
            if medias[oldMediaIndex].mediaName == medianame {
                return oldMediaIndex
            }
            oldMediaIndex += 1
        }
        return -1
    }
    
    // MARK: Navigation

    // Prepare before navigating away from table view
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ShowMeasDetail" {
            let recordDetailViewController = segue.destination as! MeasViewController
            
            // Get the cell that generated this segue.
            if let selectedRecordCell = tableView.cellForRow(at: tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPath(for: selectedRecordCell)!
                let selectedRecord = records[(indexPath as NSIndexPath).row]
                recordDetailViewController.record = selectedRecord
            }
        } else if segue.identifier == "ShowNoteDetail" {
            let recordDetailViewController = segue.destination as! NoteViewController
            
            // Get the cell that generated this segue.
            if let selectedRecordCell = tableView.cellForRow(at: tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPath(for: selectedRecordCell)!
                let selectedRecord = records[(indexPath as NSIndexPath).row]
                recordDetailViewController.record = selectedRecord
            }
        } else if segue.identifier == "ShowPhotoDetail" {
            let recordDetailViewController = segue.destination as! PhotoViewController
            
            // Get the cell that generated this segue.
            if let selectedRecordCell = tableView.cellForRow(at: tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPath(for: selectedRecordCell)!
                let selectedRecord = records[(indexPath as NSIndexPath).row]
                recordDetailViewController.record = selectedRecord
            }
        } else if segue.identifier == "Logout" {
            
        }
    }
    
    @IBAction func unwindToRecordList(_ sender: UIStoryboardSegue) {
        if let sourceViewController = sender.source as? MeasViewController, let record = sourceViewController.record {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                // Update an existing record.
                records[(selectedIndexPath as NSIndexPath).row] = record
                tableView.reloadRows(at: [selectedIndexPath], with: .none)
            } else {
                // Add a new record
                let newIndexPath = IndexPath(row: records.count, section: 0)
                records.append(record)
                tableView.insertRows(at: [newIndexPath], with: .bottom)
            }
            
            // Save the records.
            saveRecords()
        } else if let sourceViewController = sender.source as? PhotoViewController, let record = sourceViewController.record, let media = sourceViewController.media {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {

                // Before updating the photo, find its corresponding record in the media list.
                let oldMediaPath = records[(selectedIndexPath as NSIndexPath).row].props["filepath"] as! String
                let oldMediaName = oldMediaPath.replacingOccurrences(of: "\(UserVars.blobRootURLString)\(UserVars.uuid!)/", with: "")
                let oldMediaIndex = indexOfMedia(oldMediaName)
                if oldMediaIndex != -1 {
                    medias[oldMediaIndex] = media
                }
                
                record.props["filepath"] = "\(UserVars.blobRootURLString)\(UserVars.uuid!)/\(media.mediaName!)" as AnyObject?
                
                // Update the existing record.
                records[(selectedIndexPath as NSIndexPath).row] = record
                
                tableView.reloadRows(at: [selectedIndexPath], with: .none)
            } else {
                // Add a new record
                let newIndexPath = IndexPath(row: records.count, section: 0)
                record.props["filepath"] = "\(UserVars.blobRootURLString)\(UserVars.uuid!)/\(media.mediaName!)" as AnyObject?
                records.append(record)
                tableView.insertRows(at: [newIndexPath], with: .bottom)
                
                // Add a new media reference
                medias.append(media)
            }
            
            // Save the records.
            saveRecords()
            
            // Save the media.
            saveMedia()
        } else if let sourceViewController = sender.source as? NoteViewController, let record = sourceViewController.record {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                // Update an existing record.
                records[(selectedIndexPath as NSIndexPath).row] = record
                tableView.reloadRows(at: [selectedIndexPath], with: .none)
            } else {
                // Add a new record
                let newIndexPath = IndexPath(row: records.count, section: 0)
                records.append(record)
                tableView.insertRows(at: [newIndexPath], with: .bottom)
            }
            
            // Save the records.
            saveRecords()
        }
        
        loginInfo!.uuid = UserVars.uuid
        loginInfo!.accessLevels = UserVars.AccessLevels
        loginInfo!.tags = UserVars.Tags
        loginInfo!.species = UserVars.Species
        loginInfo!.units = UserVars.Units
        saveLogin()
    }
    
    // MARK: NSCoding
    
    func saveRecords() {
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(records, toFile: UserVars.RecordsURL!.path)
        
        if !isSuccessfulSave {
            print("Failed to save records...")
        }
    }
    
    func saveMedia() {
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(medias, toFile: UserVars.MediasURL!.path)
        
        if !isSuccessfulSave {
            print("Failed to save media...")
        }
    }
    
    func loadRecords() -> [Record]? {
        if let recURL = UserVars.RecordsURL?.path {
            return NSKeyedUnarchiver.unarchiveObject(withFile: recURL) as? [Record]
        }
        return nil
    }
    
    func loadMedia() -> [Media]? {
        if let medURL = UserVars.MediasURL?.path {
            return NSKeyedUnarchiver.unarchiveObject(withFile: medURL) as? [Media]
        }
        return nil
    }
    
}
