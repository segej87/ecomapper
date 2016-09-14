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
        
        self.tableView.register(UITableViewCell.self, forCellReuseIdentifier: "cell")
        
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
            newText.isEnabled = false
            newText.placeholder = "Can't add new access level remotely"
        }
        
        // Since previously selected items may have been loaded during the segue,
        // check if anything should be removed from the full list
        for f in self.fullItems {
            if self.selectedItems.contains(f) {
                self.fullItems.remove(at: self.fullItems.index(of: f)!)
            }
        }
        
        // Set the new item text field delegate
        self.newText.delegate = self
        
        // Set the search bar delegate
        self.searchBar.delegate = self
    }
    
    // MARK: Table protocols
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        var sectionName = "Selected"
        
        if section == 0 {
            sectionName = "Selected"
        } else if section == 1 {
            sectionName = "Available"
        }
        
        return sectionName
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
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
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell:UITableViewCell = self.tableView.dequeueReusableCell(withIdentifier: "cell")! as UITableViewCell
        
        if (indexPath as NSIndexPath).section == 0 {
            cell.textLabel?.text = self.selectedItems[(indexPath as NSIndexPath).row]
        } else if (indexPath as NSIndexPath).section == 1 {
            if (searchActive) {
                if listItems.count > 0 {
                    cell.textLabel?.text = self.listItems[(indexPath as NSIndexPath).row]
                }
            } else {
                cell.textLabel?.text = self.fullItems[(indexPath as NSIndexPath).row]
            }
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        // If the selected row is in the available section, move
        // the item from the available section to the selected section.
        // If the selected row is in the selected section, move to the
        // available section.
        if (indexPath as NSIndexPath).section == 1 {
            if (searchActive) {
                if listItems[(indexPath as NSIndexPath).row] != "No item found" {
                    selectedItems.append(listItems[(indexPath as NSIndexPath).row])
                    
                    // Remove the item from the full list so you can't create duplicates
                    fullItems.remove(at: fullItems.index(of: listItems[(indexPath as NSIndexPath).row])!)
                    
                    // Remove the item from the searched list
                    listItems.remove(at: (indexPath as NSIndexPath).row)
                }
            } else {
                selectedItems.append(fullItems[(indexPath as NSIndexPath).row])
                fullItems.remove(at: (indexPath as NSIndexPath).row)
            }
            
            if (itemType == "species" || itemType == "units" && selectedItems.count > 1) {
                for i in 0..<(selectedItems.count-1) {
                    fullItems.append(selectedItems[i])
                    listItems.append(selectedItems[i])
                    selectedItems.remove(at: i)
                }
            }
            
            tableView.reloadData()
        } else if (indexPath as NSIndexPath).section == 0 {
            if (searchActive) {
                // Add the item to the full list
                fullItems.append(selectedItems[(indexPath as NSIndexPath).row])
                
                // Add the item to the searched list
                listItems.append(selectedItems[(indexPath as NSIndexPath).row])
                selectedItems.remove(at: (indexPath as NSIndexPath).row)
            } else {
                fullItems.append(selectedItems[(indexPath as NSIndexPath).row])
                selectedItems.remove(at: (indexPath as NSIndexPath).row)
            }
            
            // Reload the table view after moving row.
            tableView.reloadData()
        }
    }
    
    // MARK: UITextFieldDelegate
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        
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
            fullItems.remove(at: fullItems.index(of: textField.text!)!)
            textField.text = ""
        } else {
            textField.text = ""
        }
        
        if ((itemType == "species" || itemType == "units") && selectedItems.count > 1) {
            for i in 0..<(selectedItems.count-1) {
                fullItems.append(selectedItems[i])
                listItems.append(selectedItems[i])
                selectedItems.remove(at: i)
            }
        }
        
        tableView.reloadData()
    }
    
    // MARK: Search bar delegates
    
    func searchBarTextDidBeginEditing(_ searchBar: UISearchBar) {
        searchActive = true;
    }
    
    func searchBarTextDidEndEditing(_ searchBar: UISearchBar) {
        searchActive = false;
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        searchActive = false;
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchActive = false;
    }
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        
        // Get the items from fullItems that match the search text
        listItems = fullItems.filter({ (text) -> Bool in
            let tmp: NSString = text as NSString
            let range = tmp.range(of: searchText, options: NSString.CompareOptions.caseInsensitive)
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
    
    @IBAction func cancel(_ sender: UIButton) {
        self.dismiss(animated: false, completion: nil)
        print("cancel")
    }
    
    @IBAction func save(_ sender: UIButton) {
        itemOut = selectedItems.joined(separator: ";")
        print(itemOut!)
        
    }
}
