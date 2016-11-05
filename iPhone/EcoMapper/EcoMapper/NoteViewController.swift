//
//  NoteViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/21/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation

class NoteViewController: RecordViewController, UITextFieldDelegate, UITextViewDelegate, UINavigationControllerDelegate {

    // MARK: Properties
    // IB properties
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var accessTextField: UITextField!
    @IBOutlet weak var notesTextField: UITextView!
    @IBOutlet weak var tagTextField: UITextField!
    @IBOutlet weak var gpsAccView: UILabel!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var accessPickerButton: UIButton!
    @IBOutlet weak var tagPickerButton: UIButton!

    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style the navigation bar's background color and button colors
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGray
        
        // Add border to text view
        self.notesTextField.layer.borderWidth = 0.5
        self.notesTextField.layer.cornerRadius = 10
        self.notesTextField.layer.borderColor = UIColor.init(red: 200/255.0, green: 199/255.0, blue: 204/255.0, alpha: 1.0).cgColor
        
        // Handle text fields' user input through delegate callbacks.
        nameTextField.delegate = self
        accessTextField.delegate = self
        tagTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
        
        // Enable the Save button only if the required text fields have a valid name.
        checkValidName()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: UITextFieldDelegate
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        checkValidName()
    }
    
    
    // MARK: UI Methods
    
    override func setupEditingMode(record: Record) {
        navigationItem.title = "Editing Note"
        nameTextField.text = record.props["name"] as? String
        accessTextField.text = (record.props["access"] as! [String]).joined(separator: ", ")
        accessArray = record.props["access"] as! [String]
        notesTextField.text = record.props["text"] as? String
        tagTextField.text = (record.props["tags"] as! [String]).joined(separator: ", ")
        tagArray = record.props["tags"] as! [String]
        dateTime = record.props["datetime"] as? String
        userLoc = record.coords
        gpsAccView.isHidden = true
    }
    
    // MARK: UITextViewDelegate
    
    override func checkValidName() {
        // Disable the Save button if the required text fields are empty.
        let text1 = nameTextField.text ?? ""
        let text2 = notesTextField.text ?? ""
        let text3 = tagTextField.text ?? ""
        let text4 = accessTextField.text ?? ""
        let loc1 = userLoc ?? nil
        saveButton.isEnabled = !(text1.isEmpty || text2.isEmpty || text3.isEmpty || text4.isEmpty || loc1 == nil)
    }
    
    func textViewDidChange(_ textView: UITextView) {
        checkValidName()
    }
    
    // MARK: Location methods
    
    override func updateGPS() {
        gpsAccView.text = "Current GPS Accuracy: \(gpsAcc) m"
    }

    override func noGPS() {
        if #available(iOS 8.0, *) {
            let alertVC = UIAlertController(title: "No GPS", message: "Can't pinpoint your location, using default", preferredStyle: .alert)
            let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
            alertVC.addAction(okAction)
            present(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: "No GPS", message: "Can't pinpoint your location, using default", delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
    
    // MARK: Navigation
    
    @IBAction func cancel(_ sender: UIBarButtonItem) {
        locationManager.stopUpdatingLocation()
        
        // Depending on style of presentation (modal or push), dismiss the view controller differently
        let isPresentingInAddRecordMode = presentingViewController is UINavigationController
        if isPresentingInAddRecordMode {
            dismiss(animated: true, completion: nil)
        } else {
            navigationController!.popViewController(animated: true)
        }
    }
    
    // This method lets you configure a view controller before it's presented.
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        locationManager.stopUpdatingLocation()
        
        if sender is UIBarButtonItem && saveButton === (sender as! UIBarButtonItem) {
            let name = nameTextField.text ?? ""
            
            let props = ["name": name as AnyObject, "tags": tagArray as AnyObject, "datatype": "note" as AnyObject, "datetime": dateTime! as AnyObject, "access": accessArray as AnyObject, "accuracy": gpsAcc as AnyObject, "text": notesTextField.text as AnyObject] as [String:AnyObject]
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: userLoc!, photo: nil, props: props)
        }
        
        // If the add access button was pressed, present the item picker with an access item type
        if sender is UIButton && accessPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "access"
            
            // Send previous access levels to ListPicker
            if accessTextField.text != "" {
                for a in accessArray {
                    secondVC.selectedItems.append(a)
                }
            }
        }
        
        // If the add tags button was pressed, present the item picker with a tag item type
        if sender is UIButton && tagPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "tags"
            
            // TODO: Send previous tags to ListPicker
            if tagTextField.text != "" {
                for t in tagArray {
                    secondVC.selectedItems.append(t)
                }
            }
        }
    }
    
    @IBAction func unwindFromTagController(_ segue: UIStoryboardSegue) {
        let secondVC : ListPickerViewController = segue.source as! ListPickerViewController
        
        let secondType = secondVC.itemType
        
        var targetField : UITextField?
        
        if secondType == "tags" {
            targetField = tagTextField
        } else if secondType == "access" {
            targetField = accessTextField
        }
        
        if secondType == "tags" {
            if tagTextField.text != "" {
                let prevArray = tagArray
                for p in prevArray {
                    var pTag = UserVars.Tags[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = ((pTag![1] as! Int - 1) as AnyObject)
                        if pTag![1] as! Int == 0 {
                            UserVars.Tags.removeValue(forKey: p)
                        } else {
                            UserVars.Tags[p] = pTag!
                        }
                    }
                }
            }
            
            for t in secondVC.selectedItems {
                if !UserVars.Tags.keys.contains(t) {
                    UserVars.Tags[t] = ["Local" as AnyObject,1 as AnyObject]
                } else {
                    var tagInfo = UserVars.Tags[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Tags[t] = tagInfo
                    }
                }
            }
        }
        
        if secondType == "tags" {
            tagArray = secondVC.selectedItems
        } else if secondType == "access" {
            accessArray = secondVC.selectedItems
        }
        
        targetField!.text = secondVC.selectedItems.joined(separator: ", ")
        
        checkValidName()
    }
    
    // MARK: Actions
    
    @IBAction func setDefaultNameText(_ sender: UIButton) {
        nameTextField.text = "Note" + " - " + dateTime!
    }
    
}
