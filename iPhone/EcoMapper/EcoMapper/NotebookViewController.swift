//
//  NotebookViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/3/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class NotebookViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    
    // MARK: User Variables
    
    /*
     UI References
     */
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var syncButton: UIBarButtonItem!
    @IBOutlet weak var logoutButton: UIBarButtonItem!
    @IBOutlet weak var editButton: UIBarButtonItem!
    @IBOutlet weak var mediaMonitor: UIView!
    @IBOutlet weak var mediaProgress: UIActivityIndicatorView!
    @IBOutlet weak var mediaCounter: UILabel!
    
    /*
     An array to hold saved records.
     */
    var records = [Record]()
    
    /*
     An array for holding names and paths to media on this device.
     */
    var medias = [Media]()
    
    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Start the reachability class
        if NetworkTests.reachability == nil {
            NetworkTests.setupReachability(nil)
        }
        
        // Style the navigation bar's background color and button colors.
        styleNavigationBar()
        
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
        
        mediaMonitorManager()
    }
    
    
    //MARK: Table View Data Source Methods
    
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
    
    // If a row is selected, begin editing the record for that row.
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
    
    // Override to support editing the table view.
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        
        // First, get the selected row and record
        let selectedRow = (indexPath as NSIndexPath).row
        let selectedRecord = records[selectedRow]
        
        if editingStyle == .delete {
            
            // Before deleting a photo, the corresponding item in the media list needs to be found and deleted, along with the photo itself in the device's storage.
            if selectedRecord.props["datatype"] as! String == "photo" {
                
                // Get the filepath of the photo record.
                let oldMediaPath = selectedRecord.props["filepath"] as! String
                
                // Remove the root URL string to get the media name (media paths are formatted root URL/mediaName.
                let oldMediaName = oldMediaPath.replacingOccurrences(of: "\(UserVars.blobRootURLString)\(UserVars.UUID!)/", with: "")
                
                // Find the index in the media array corresponding to the media name.
                let oldMediaIndex = indexOfMedia(oldMediaName)
                
                let oldLocalPath = medias[oldMediaIndex].mediaPath!
                
                // Before deleting the record row, delete the media reference.
                if oldMediaIndex != -1 {
                    medias.remove(at: oldMediaIndex)
                }
                
                // Try to delete the photo file
                do {
                    try FileManager.default.removeItem(at: oldLocalPath)
                    NSLog("Deleted photo \(oldLocalPath)")
                } catch let error as NSError {
                    NSLog("Could not delete photo \(oldLocalPath): \(error.localizedDescription)")
                }
            }
            
            // Update the user variables in response to removing the record
            UserVars.handleDeletedRecord(record: selectedRecord)
            
            // Delete the record from the data source.
            records.remove(at: selectedRow)
            
            // Save the modified data and user variables
            saveRecords()
            saveMedia()
            UserVars.saveUserVars()
            
            // Delete the row from the table.
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }
    }
    
    
    // MARK: Actions
    
    @IBAction func editButtonHandler(_ sender: UIBarButtonItem) {
        if isEditing {
            editButton.title = "Edit"
            self.setEditing(false, animated: true)
            tableView.setEditing(false, animated: true)
        } else {
            editButton.title = "Done"
            self.setEditing(true, animated: true)
            tableView.setEditing(true, animated: true)
        }
    }
    
    @IBAction func attemptMediaUpload(_ sender: UITapGestureRecognizer) {
        // Initialize the class for uploading data
        let ud = UploadData(tableView: self)
        
        NSLog("Attempting to upload medias")
        mediaProgress.startAnimating()
        ud.uploadMedia()
    }
    
    @IBAction func attemptSync(_ sender: UIBarButtonItem) {
        if #available(iOS 8.0, *) {
            let alertVC = UIAlertController(title: "Are you sure?", message: "All records will be removed after syncing.", preferredStyle: .alert)
            let syncAction = UIAlertAction(title: "Sync", style: .default, handler: self.executeSync)
            let cancelAction = UIAlertAction(title: "Cancel", style: .default, handler: nil)
            alertVC.addAction(syncAction)
            alertVC.addAction(cancelAction)
            present(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: "No GPS", message: "Can't pinpoint your location, using default", delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
    
    func executeSync(action: UIAlertAction) -> Void {
        // Deactivate the buttons while uploading
        toggleButtons(enabled: false)
        
        // Initialize the class for uploading data
        let ud = UploadData(tableView: self)
        
        // Check if the device is connected to a network
        guard let reach = NetworkTests.reachability
            else { return }
        if reach.isReachable() {
            
            let biggerDict = recordsToJson()
            ud.uploadRecords(biggerDict as NSDictionary)
            self.markMedias()
            
            // Move to an asynchronous thread and upload the media from the media array
            let priority = DispatchQoS.QoSClass.default
            DispatchQueue.global(qos: priority).async{
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
            
            // Reactivate the buttons
            toggleButtons(enabled: true)
        }
    }
    
    
    // MARK: Navigation
    
    // Handle the logout button
    @IBAction func handleLogout(_ sender: UIBarButtonItem) {
        if #available(iOS 8.0, *) {
            let alertVC = UIAlertController(title: "Logout?", message: "Are you sure you want to leave Kora?", preferredStyle: .alert)
            let leaveAction = UIAlertAction(title: "Logout", style: .default, handler: self.executeLogout)
            let stayAction = UIAlertAction(title: "Stay", style: .default, handler: nil)
            alertVC.addAction(leaveAction)
            alertVC.addAction(stayAction)
            present(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: "No GPS", message: "Can't pinpoint your location, using default", delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
    
    func executeLogout(action: UIAlertAction) -> Void {
        // Clear all of the user variables
        UserVars.clearUserVars()
        UserVars.saveLogin(loginInfo: LoginInfo(uuid: UserVars.UUID))
        self.performSegue(withIdentifier: "LogoutSegue", sender: self)
    }
    
    
    // Prepare before navigating away from table view
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        var recordDetailViewController: RecordViewController?
        if let identifier = segue.identifier {
            switch(identifier) {
            case "ShowMeasDetail":
                recordDetailViewController = segue.destination as! MeasViewController
                break;
            case "ShowNoteDetail":
                recordDetailViewController = segue.destination as! NoteViewController
                break;
            case "ShowPhotoDetail":
                recordDetailViewController = segue.destination as! PhotoViewController
                break;
            default:
                break;
            }
        }
        
        // Get the cell that generated this segue.
        if recordDetailViewController != nil {
            if let selectedRecordCell = tableView.cellForRow(at: tableView.indexPathForSelectedRow!) as? RecordTableViewCell {
                let indexPath = tableView.indexPath(for: selectedRecordCell)!
                let selectedRecord = records[(indexPath as NSIndexPath).row]
                recordDetailViewController!.record = selectedRecord
            }
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
        } else if let sourceViewController = sender.source as? PhotoViewController, let record = sourceViewController.record {
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                
                // Update the existing record.
                records[(selectedIndexPath as NSIndexPath).row] = record
                tableView.reloadRows(at: [selectedIndexPath], with: .none)
            } else if let media = sourceViewController.media {
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
        
        UserVars.saveUserVars()
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
    
    
    // MARK: Helper methods
    
    func recordsToJson() -> [String : Any] {
        // Loop through all records and add to features array
        var features = [AnyObject]()
        for i in 0..<records.count {
            let record = records[i]
            
            // Prepare the record to be written in JSON format and add to features array
            let dictItem = record.prepareForJSON()
            features.append(dictItem as AnyObject)
        }
        
        // Add requred formatting for geojson, and return
        return ["type":"FeatureCollection", "features": features] as [String : Any]
    }
    
    func markMedias() {
        for m in medias {
            m.marked = true
        }
        
        saveMedia()
        
        mediaMonitorManager()
    }
    
    func mediaMonitorManager() {
        var mmSize = 0
        for m in medias {
            if m.marked! {
                mmSize += 1
            }
        }
        
        if mmSize == 0 {
            mediaMonitor.isHidden = true
        } else {
            if mediaMonitor.isHidden {
                mediaMonitor.isHidden = false
            }
            
            mediaCounter.text = String(mmSize)
        }
    }
    
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
    
    func toggleButtons (enabled: Bool) {
        for b in [syncButton, logoutButton, editButtonItem] {
            b!.isEnabled = enabled
        }
        
        tableView.allowsSelection = enabled
    }
}
