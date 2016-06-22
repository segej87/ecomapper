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
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Load the sample data.
        loadSampleRecords()
    }
    
    func loadSampleRecords() {
        let record1 = Record(coords: [-122.22, 31.96, 0], photo: nil, props: ["name":"First measurement", "value": 0.0543, "species": "Nitrate", "units": "ppm", "datetime": "2016-05-14 12:00:00", "tags": "First Campaign;Water;Well Water", "datatype": "meas"])!
        
        let record2 = Record(coords: [-122.15, 31.86, 15], photo: nil, props: ["name":"First note", "text": "I saw a big coyote here", "datetime": "2016-05-14 12:56:00", "tags": "First Campaign;Wildlife;Coyote", "datatype": "note"])!
        
        let record3 = Record(coords: [-122.15, 31.86, 0], photo: UIImage(named: "samplePhoto"), props: ["name":"First photo", "text": "These are berries from dad's garden", "datetime": "2016-05-14 16:21:00", "tags": "Berries;Dad;Garden", "datatype": "photo"])!
        
        records += [record1, record2, record3]
        
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
        if record.photo == nil {
            let dt = record.props["datatype"] as! String
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
        cell.tagLabel.text = dispTags
        
        return cell
    }

    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

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

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */
    
    @IBAction func unwindToRecordList(sender: UIStoryboardSegue) {
        if let sourceViewController = sender.sourceViewController as? MeasViewController, record = sourceViewController.record {
            // Add a new record
            let newIndexPath = NSIndexPath(forRow: records.count, inSection: 0)
            records.append(record)
            tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Bottom)
        } else if let sourceViewController = sender.sourceViewController as? PhotoViewController, record = sourceViewController.record {
            // Add a new record
            let newIndexPath = NSIndexPath(forRow: records.count, inSection: 0)
            records.append(record)
            tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Bottom)
        }
    }
    
}
