//
//  ListPickerViewController.swift
//  EcoMapper
//
//  Created by Jon on 8/29/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class ListPickerViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate {
    
    // MARK: Properties
    
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var newText: UITextField!
    
    
    // Full data source for table view
    var fullItems = [String]()
    
    // Items to list in table view (after search)
    var listItems = [String]()
    
    var selectedItems = [String]()
    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.tableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: "cell")
        
        // TODO: Set dynamically through segue
        self.fullItems = UserVars.Tags
        
        // Load the full list into the table view
        self.listItems = fullItems
        
        // Set the new item text field delegate
        self.newText.delegate = self
    }
    
    // MARK: Table protocols
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        var sectionName = "Selected"
        
        if section == 0 {
            sectionName = "Selected"
        } else if section == 1 {
            sectionName = "Available"
        }
        
        return sectionName
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        var rowCount = 0
        
        if section == 0 {
            rowCount = selectedItems.count
        } else if section == 1 {
            rowCount = listItems.count
        }
        
        return rowCount
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell:UITableViewCell = self.tableView.dequeueReusableCellWithIdentifier("cell")! as UITableViewCell
        
        if indexPath.section == 0 {
            cell.textLabel?.text = self.selectedItems[indexPath.row]
        } else if indexPath.section == 1 {
            cell.textLabel?.text = self.listItems[indexPath.row]
        }
        
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if indexPath.section == 1 {
            selectedItems.append(listItems[indexPath.row])
            listItems.removeAtIndex(indexPath.row)
            
            tableView.reloadData()
        } else if indexPath.section == 0 {
            listItems.append(selectedItems[indexPath.row])
            selectedItems.removeAtIndex(indexPath.row)
            
            tableView.reloadData()
        }
    }
    
    // MARK: UITextFieldDelegate
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
        // If the entered text is not already in the list, add as a
        // selected item and refresh the table.
        if !(textField.text?.isEmpty)! && !listItems.contains(textField.text!) && !selectedItems.contains(textField.text!) {
            selectedItems.append(textField.text!)
            tableView.reloadData()
            textField.text = ""
        }
        
        
        
        //checkValidName()
    }

    // MARK: Actions
    
    @IBAction func cancel(sender: UIButton) {
        self.dismissViewControllerAnimated(false, completion: nil)
        print("cancel")
    }
    
    @IBAction func save(sender: UIButton) {
        let tagOut = selectedItems.joinWithSeparator(";")
        print(tagOut)
    }
    
}
