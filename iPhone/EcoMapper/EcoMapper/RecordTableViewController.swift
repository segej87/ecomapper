//
//  RecordTableViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/21/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class RecordTableViewController: UITableViewController {
    
    // MARK: Properties
    
    var records = [Record]()
    var guid = "7b5586d5-b297-473f-adbc-ec352ede4f26"
    struct syncMsg {
        var curVal = String()
        var newVal: String? {
            get {
                return curVal
            }
            set(msg) {
                curVal = msg!
            }
        }
    }
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style the navigation bar's background color and button colors
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.Black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGrayColor()
        
        // Use the edit button item provided by the table view controller.
        navigationItem.leftBarButtonItem = editButtonItem()
        
        // Load any saved records, otherwise, load nothing
        if let savedRecords = loadRecords() {
            records += savedRecords
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source

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
        
        cell.nameLabel.text = record.props["name"] as? String
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
            cell.photoImageView.image = record.photo
        }
        cell.dateLabel.text = record.props["datetime"] as? String
        let recTags = record.props["tags"] as! String
        let dispTags = recTags.stringByReplacingOccurrencesOfString(";", withString: ", ", options: NSStringCompareOptions.LiteralSearch, range: nil)
        switch dt {
            case "meas":
                cell.tagLabel.text = "\(record.props["species"]!):  \(record.props["value"]!) \(record.props["units"]!)"
        default:
            cell.tagLabel.text = dispTags
        }
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let selectedRecord = records[indexPath.row]
        let dt = selectedRecord.props["datatype"] as! String
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
            // Delete the row from the data source
            records.removeAtIndex(indexPath.row)
            saveRecords()
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
        if Reachability.isConnectedToNetwork() {
            var bigDict = [AnyObject]()
            for record in records {
                let dictItem = record.prepareForJSON()
                bigDict.append(dictItem)
            }
            let biggerDict = ["type":"FeatureCollection", "features": bigDict]
            
            var msg = syncMsg(curVal: "No message")
            do {
                let biggestDict = try NSJSONSerialization.dataWithJSONObject(biggerDict, options: NSJSONWritingOptions())
                let dataString = NSString(data: biggestDict, encoding: NSUTF8StringEncoding)
                let request = NSMutableURLRequest(URL: NSURL(string: "http://ecocollector.azurewebsites.net/add_records.php")!)
                request.HTTPMethod = "POST"
                let postString = "GUID=\(guid) & geojson=\(dataString!)"
                request.HTTPBody = postString.dataUsingEncoding(NSUTF8StringEncoding)
                let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in guard error == nil && data != nil else {
                    print("error=\(error!)")
                    msg.newVal = "Error: \(error!)"
                    //self.syncMsg = "Error: \(error!)"
                    return
                    }
                    
                    if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {
                        print("statusCode should be 200, but is \(httpStatus.statusCode)")
                        print("response = \(response!)")
                        msg.newVal = "Error: \(response)"
                        //self.syncMsg = "Error: \(response)"
                    }
                    
                    let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
                    print("responseString = \(responseString!)")
                    msg.newVal = "Result: \(responseString!)"
                    //set { self.syncMsg = "Result: \(responseString!)" }
                }
                task.resume()
            } catch let error as NSError {
                print(error)
            }
            
            let alertVC = UIAlertController(title: "Sync status", message: msg.newVal, preferredStyle: .Alert)
            let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
            alertVC.addAction(okAction)
            self.presentViewController(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertController(title: "No connection detected", message: "Can't sync without a data connection", preferredStyle: .Alert)
            let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
            alertVC.addAction(okAction)
            presentViewController(alertVC, animated: true, completion: nil)
        }
    }
    
    
//    @IBAction func selectCell(sender: UITapGestureRecognizer) {
//        selectCell(sender)
//    }

    // MARK: Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
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
        } else if let sourceViewController = sender.sourceViewController as? PhotoViewController, record = sourceViewController.record {
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
    
    func loadRecords() -> [Record]? {
        return NSKeyedUnarchiver.unarchiveObjectWithFile(Record.ArchiveURL.path!) as? [Record]
    }
    
}
