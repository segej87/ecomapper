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
    
    // Button to send data to server.
    @IBOutlet weak var syncButton: UIBarButtonItem!
    
    // Array for holding saved records.
    var records = [Record]()
    
    // Array for holding names and paths to media on device.
    var medias = [Media]()
    
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
                let oldMediaName = oldMediaPath.stringByReplacingOccurrencesOfString("\(UserVars.blobRootURLString)\(UserVars.uuid!)/", withString: "")
                
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
        
        // Deactivate the sync button while uploading
        syncButton.enabled = false
        
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
                features.append(dictItem)
            }
            
            // Add requred formatting for geojson, and start uploading procedure
            let biggerDict = ["type":"FeatureCollection", "features": features]
            ud.uploadRecords(biggerDict)
            
            // Move to an asynchronous thread and upload the media from the media array
            let  priority = DISPATCH_QUEUE_PRIORITY_DEFAULT
            dispatch_async(dispatch_get_global_queue(priority, 0)){
                ud.uploadMedia()
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
            
            // Reactivate the sync button
            syncButton.enabled = true
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
        } else if segue.identifier == "Logout" {
            
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
                let oldMediaName = oldMediaPath.stringByReplacingOccurrencesOfString("\(UserVars.blobRootURLString)\(UserVars.uuid!)/", withString: "")
                let oldMediaIndex = indexOfMedia(oldMediaName)
                if oldMediaIndex != -1 {
                    medias[oldMediaIndex] = media
                }
                
                record.props["filepath"] = "\(UserVars.blobRootURLString)\(UserVars.uuid!)/\(media.mediaName!)"
                
                // Update the existing record.
                records[selectedIndexPath.row] = record
                
                tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
            } else {
                // Add a new record
                let newIndexPath = NSIndexPath(forRow: records.count, inSection: 0)
                record.props["filepath"] = "\(UserVars.blobRootURLString)\(UserVars.uuid!)/\(media.mediaName!)"
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
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(records, toFile: UserVars.RecordsURL!.path!)
        
        if !isSuccessfulSave {
            print("Failed to save records...")
        }
    }
    
    func saveMedia() {
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(medias, toFile: UserVars.MediasURL!.path!)
        
        if !isSuccessfulSave {
            print("Failed to save media...")
        }
    }
    
    func loadRecords() -> [Record]? {
        if let recURL = UserVars.RecordsURL?.path {
            return NSKeyedUnarchiver.unarchiveObjectWithFile(recURL) as? [Record]
        }
        return nil
    }
    
    func loadMedia() -> [Media]? {
        if let medURL = UserVars.MediasURL?.path {
            return NSKeyedUnarchiver.unarchiveObjectWithFile(medURL) as? [Media]
        }
        return nil
    }
    
}
