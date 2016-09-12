//
//  ListPickerViewController.swift
//  EcoMapper
//
//  Created by Jon on 8/29/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class ListPickerViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate, UISearchBarDelegate {
    
    // MARK: Properties
    // IB Properties
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var newText: UITextField!
    
    // Class variables
    // A key indicating the type of list to use (sent via segue)
    var itemType:String?
    
    // Full data source for table view
    var fullItems = [String]()
    
    // Items to list in table view (after search)
    var listItems = [String]()
    
    // For storing items selected by the user
    var selectedItems = [String]()
    
    // The final joined string of selected list items
    var itemOut : String?
    
    // Boolean indicating whether a search is active
    var searchActive : Bool = false
    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        print(itemType!)
        
        self.tableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: "cell")
        
        // TODO: Set dynamically through segue
        // Load the initial full data source for the table view
        if itemType == "tags" {
            self.fullItems = Array(UserVars.Tags.keys)
            newText.placeholder = "Add new tag"
        } else if itemType == "species" {
            self.fullItems = Array(UserVars.Species.keys)
            newText.placeholder = "Add new measured item"
        } else if itemType == "units" {
            self.fullItems = Array(UserVars.Units.keys)
            newText.placeholder = "Add new unit"
        } else if itemType == "access" {
            self.fullItems = UserVars.AccessLevels
            newText.enabled = false
            newText.placeholder = "Can't add new access level remotely"
        }
        
        // Since previously selected items may have been loaded during the segue,
        // check if anything should be removed from the full list
        for f in self.fullItems {
            if self.selectedItems.contains(f) {
                self.fullItems.removeAtIndex(self.fullItems.indexOf(f)!)
            }
        }
        
        // Set the new item text field delegate
        self.newText.delegate = self
        
        // Set the search bar delegate
        self.searchBar.delegate = self
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
            if (searchActive) {
                rowCount = listItems.count
            } else {
                rowCount = fullItems.count
            }
        }
        
        return rowCount
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell:UITableViewCell = self.tableView.dequeueReusableCellWithIdentifier("cell")! as UITableViewCell
        
        if indexPath.section == 0 {
            cell.textLabel?.text = self.selectedItems[indexPath.row]
        } else if indexPath.section == 1 {
            if (searchActive) {
                if listItems.count > 0 {
                    cell.textLabel?.text = self.listItems[indexPath.row]
                }
            } else {
                cell.textLabel?.text = self.fullItems[indexPath.row]
            }
        }
        
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        // If the selected row is in the available section, move
        // the item from the available section to the selected section.
        // If the selected row is in the selected section, move to the
        // available section.
        if indexPath.section == 1 {
            if (searchActive) {
                if listItems[indexPath.row] != "No item found" {
                    selectedItems.append(listItems[indexPath.row])
                    
                    // Remove the item from the full list so you can't create duplicates
                    fullItems.removeAtIndex(fullItems.indexOf(listItems[indexPath.row])!)
                    
                    // Remove the item from the searched list
                    listItems.removeAtIndex(indexPath.row)
                }
            } else {
                selectedItems.append(fullItems[indexPath.row])
                fullItems.removeAtIndex(indexPath.row)
            }
            
            if (itemType == "species" || itemType == "units" && selectedItems.count > 1) {
                for i in 0..<(selectedItems.count-1) {
                    fullItems.append(selectedItems[i])
                    listItems.append(selectedItems[i])
                    selectedItems.removeAtIndex(i)
                }
            }
            
            tableView.reloadData()
        } else if indexPath.section == 0 {
            if (searchActive) {
                // Add the item to the full list
                fullItems.append(selectedItems[indexPath.row])
                
                // Add the item to the searched list
                listItems.append(selectedItems[indexPath.row])
                selectedItems.removeAtIndex(indexPath.row)
            } else {
                fullItems.append(selectedItems[indexPath.row])
                selectedItems.removeAtIndex(indexPath.row)
            }
            
            // Reload the table view after moving row.
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
        
        if searchBar.text != "" {
            searchBar.text = ""
            // searchActive = false
        }
        
        // If the entered text is not already in the list, add as a
        // selected item and refresh the table.
        if !(textField.text?.isEmpty)! && !fullItems.contains(textField.text!) && !selectedItems.contains(textField.text!) {
            selectedItems.append(textField.text!)
            textField.text = ""
        } else if !(textField.text?.isEmpty)! && fullItems.contains(textField.text!) && !selectedItems.contains(textField.text!) {
            selectedItems.append(textField.text!)
            fullItems.removeAtIndex(fullItems.indexOf(textField.text!)!)
            textField.text = ""
        } else {
            textField.text = ""
        }
        
        if ((itemType == "species" || itemType == "units") && selectedItems.count > 1) {
            for i in 0..<(selectedItems.count-1) {
                fullItems.append(selectedItems[i])
                listItems.append(selectedItems[i])
                selectedItems.removeAtIndex(i)
            }
        }
        
        tableView.reloadData()
    }
    
    // MARK: Search bar delegates
    
    func searchBarTextDidBeginEditing(searchBar: UISearchBar) {
        searchActive = true;
    }
    
    func searchBarTextDidEndEditing(searchBar: UISearchBar) {
        searchActive = false;
    }
    
    func searchBarCancelButtonClicked(searchBar: UISearchBar) {
        searchActive = false;
    }
    
    func searchBarSearchButtonClicked(searchBar: UISearchBar) {
        searchActive = false;
    }
    
    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        
        // Get the items from fullItems that match the search text
        listItems = fullItems.filter({ (text) -> Bool in
            let tmp: NSString = text
            let range = tmp.rangeOfString(searchText, options: NSStringCompareOptions.CaseInsensitiveSearch)
            return range.location != NSNotFound
        })
        
        if (listItems.count == 0 && searchBar.text != ""){
            listItems = ["No item found"]
        }
        
        if (searchBar.text == "") {
            listItems = fullItems
        }
        
        self.tableView.reloadData()
    }

    // MARK: Actions
    
    @IBAction func cancel(sender: UIButton) {
        self.dismissViewControllerAnimated(false, completion: nil)
        print("cancel")
    }
    
    @IBAction func save(sender: UIButton) {
        itemOut = selectedItems.joinWithSeparator(";")
        print(itemOut!)
        
        // TODO: Add new items to the full list
        
    }
}
