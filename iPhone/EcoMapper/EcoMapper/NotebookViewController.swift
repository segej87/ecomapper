//
//  NotebookViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/3/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class NotebookViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var syncButton: UIBarButtonItem!
    @IBOutlet weak var logoutButton: UIBarButtonItem!

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
        styleNavigationBar()
        
        // Add an edit button item to the navigation bar
        navigationItem.leftBarButtonItem = editButtonItem
        
        // Load any saved records. If none, load nothing.
        if let savedRecords = loadRecords() {
            records += savedRecords
        }
        
        // Load any saved media. If none, load nothing.
        if let savedMedia = loadMedia() {
            medias += savedMedia
        }
        
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return records.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
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
        
        // Join the tags array with commas
        let dispTags = recTags!.joined(separator: ", ")
        
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
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
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
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        
        if editingStyle == .delete {
            
            // Before deleting a photo, the corresponding item in the media list needs to be found and deleted.
            if records[(indexPath as NSIndexPath).row].props["datatype"] as! String == "photo" {
                
                // Get the filepath of the photo record.
                let oldMediaPath = records[(indexPath as NSIndexPath).row].props["filepath"] as! String
                
                // Remove the root URL string to get the media name (media paths are formatted root URL/mediaName.
                let oldMediaName = oldMediaPath.replacingOccurrences(of: "\(UserVars.blobRootURLString)\(UserVars.UUID!)/", with: "")
                
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
                let specArray = records[(indexPath as NSIndexPath).row].props["species"]?.components(separatedBy: ", ")
                for s in specArray! {
                    var sTag = UserVars.Species[s]
                    if sTag![0] as! String == "Local" {
                        sTag![1] = ((sTag![1] as! Int - 1) as AnyObject)
                        if sTag![1] as! Int == 0 {
                            UserVars.Species.removeValue(forKey: s)
                        } else {
                            UserVars.Species[s] = sTag!
                        }
                    }
                }
                
                let unitArray = records[(indexPath as NSIndexPath).row].props["units"]?.components(separatedBy: ", ")
                for u in unitArray! {
                    var uTag = UserVars.Units[u]
                    if uTag![0] as! String == "Local" {
                        uTag![1] = ((uTag![1] as! Int - 1) as AnyObject)
                        if uTag![1] as! Int == 0 {
                            UserVars.Units.removeValue(forKey: u)
                        } else {
                            UserVars.Units[u] = uTag!
                        }
                    }
                }
            }
            
            // Delete the record from the data source.
            records.remove(at: (indexPath as NSIndexPath).row)
            
            // Save the modified data.
            saveRecords()
            saveMedia()
            
            // Save the lists for logins.
            saveLogin()
            
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
    
//    @IBAction func attemptSync(_ sender: UIBarButtonItem) {
//        
//        // Deactivate the buttons while uploading
//        deactivateButtons()
//        
//        // Initialize the class for uploading data
//        let ud = UploadData(tableView: self)!
//        
//        // Check if the device is connected to a network
//        if Reachability.isConnectedToNetwork() {
//            
//            // Loop through all records and add to features array
//            var features = [AnyObject]()
//            for i in 0..<records.count {
//                let record = records[i]
//                
//                // Prepare the record to be written in JSON format and add to features array
//                let dictItem = record.prepareForJSON()
//                features.append(dictItem as AnyObject)
//            }
//            
//            // Add requred formatting for geojson, and start uploading procedure
//            let biggerDict = ["type":"FeatureCollection", "features": features] as [String : Any]
//            ud.uploadRecords(biggerDict as NSDictionary)
//            
//            // Move to an asynchronous thread and upload the media from the media array
//            let  priority = DispatchQueue.GlobalQueuePriority.default
//            DispatchQueue.global(priority: priority).async{
//                ud.uploadMedia()
//            }
//        } else {
//            
//            // If no network connection, show an alert
//            if #available(iOS 8.0, *) {
//                let alertVC = UIAlertController(title: "No connection detected", message: "Can't sync without a data connection", preferredStyle: .alert)
//                let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
//                alertVC.addAction(okAction)
//                present(alertVC, animated: true, completion: nil)
//            } else {
//                let alertVC = UIAlertView(title: "No connection detected", message: "Can't sync without a data connection", delegate: self, cancelButtonTitle: "OK")
//                alertVC.show()
//            }
//            
//            // Reactivate the buttons
//            deactivateButtons()
//        }
//    }
    
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
    
    func styleNavigationBar() {
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGray
    }
    
    func deactivateButtons () {
        for b in [syncButton, logoutButton, editButtonItem] {
            b!.isEnabled = false
        }
        
        tableView.allowsSelection = false
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
                let oldMediaName = oldMediaPath.replacingOccurrences(of: "\(UserVars.blobRootURLString)\(UserVars.UUID!)/", with: "")
                let oldMediaIndex = indexOfMedia(oldMediaName)
                if oldMediaIndex != -1 {
                    medias[oldMediaIndex] = media
                }
                
                record.props["filepath"] = "\(UserVars.blobRootURLString)\(UserVars.UUID!)/\(media.mediaName!)" as AnyObject?
                
                // Update the existing record.
                records[(selectedIndexPath as NSIndexPath).row] = record
                
                tableView.reloadRows(at: [selectedIndexPath], with: .none)
            } else {
                // Add a new record
                let newIndexPath = IndexPath(row: records.count, section: 0)
                record.props["filepath"] = "\(UserVars.blobRootURLString)\(UserVars.UUID!)/\(media.mediaName!)" as AnyObject?
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
        
        saveUserVars()
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
    
    func saveLogin() {
        // Create a login object with the user variables
        let loginInfo = LoginInfo(uuid: UserVars.UUID)
        
        // Save the login object
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(loginInfo!, toFile: LoginInfo.ArchiveURL.path)
        
        if !isSuccessfulSave {
            print("Failed to save login info...")
        }
    }
    
    func saveUserVars() {
        let userVars = UserVarsSaveFile(userName: UserVars.UName, accessLevels: UserVars.AccessLevels, tags: UserVars.Tags, species: UserVars.Species, units: UserVars.Units, accessDefaults: UserVars.AccessDefaults, tagDefaults: UserVars.TagsDefaults, speciesDefault: UserVars.SpecDefault, unitsDefault: UserVars.UnitsDefault)
        
        NSLog("Attempting to save user variables to \(UserVars.UserVarsURL!.path)")
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(userVars, toFile: (UserVars.UserVarsURL!.path))
        
        if !isSuccessfulSave {
            NSLog("Failed to save user variables...")
        }
    }
}
