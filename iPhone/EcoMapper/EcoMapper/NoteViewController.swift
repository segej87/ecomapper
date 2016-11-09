//
//  NoteViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/21/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation

class NoteViewController: RecordViewController, UINavigationControllerDelegate {

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
    @IBOutlet weak var gpsStabView: UILabel!
    @IBOutlet weak var gpsReportArea: UIView!

    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    // MARK: UI Methods
    
    override func setUpFields() {
        if mode == "new" {
            accessTextField.text = accessArray.joined(separator: ", ")
            tagTextField.text = tagArray.joined(separator: ", ")
        } else {
            if let record = record {
                navigationItem.title = "Editing Note"
                nameTextField.text = record.props["name"] as? String
                accessTextField.text = (record.props["access"] as! [String]).joined(separator: ", ")
                accessArray = record.props["access"] as! [String]
                notesTextField.text = record.props["text"] as? String
                tagTextField.text = (record.props["tags"] as! [String]).joined(separator: ", ")
                tagArray = record.props["tags"] as! [String]
                dateTime = record.props["datetime"] as? String
                userLoc = record.coords
                gpsReportArea.isHidden = true
            }
        }
        
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
    }
    
    
    // MARK: Location methods
    
    override func updateGPS() {
        if gpsAcc == -1 {
            gpsAccView.text = "Locking"
            gpsAccView.textColor = UIColor.red
        } else {
            gpsAccView.text = String(format: "%.1f m", abs(gpsAcc))
            if gpsAcc <= UserVars.minGPSAccuracy {
                gpsAccView.textColor = UIColor.green
            } else {
                gpsAccView.textColor = UIColor.red
            }
        }
        
        if gpsStab == -1 {
            gpsStabView.text = "Locking"
            gpsStabView.textColor = UIColor.red
        } else {
            gpsStabView.text = String(format: "%.1f m", abs(gpsStab))
            if gpsStab <= UserVars.minGPSStability {
                gpsStabView.textColor = UIColor.green
            } else {
                gpsStabView.textColor = UIColor.red
            }
        }
    }
    
    // MARK: Navigation
    
    @IBAction func cancel(_ sender: UIBarButtonItem) {
        cancelView()
    }
    
    // This method lets you configure a view controller before it's presented.
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        locationManager.stopUpdatingLocation()
        
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
    
    @IBAction func unwindFromListPicker(_ segue: UIStoryboardSegue) {
        // The view controller that initiated the segue
        let secondVC : ListPickerViewController = segue.source as! ListPickerViewController
        
        // The type of the view controller that initiated the segue
        let secondType = secondVC.itemType
        
        // The text field that should be modified by the results of the list picker
        var targetField : UITextField?
        
        // Set the target text field based on the second view controller's type
        switch secondType! {
        case "tags":
            targetField = tagTextField
            break
        case "access":
            targetField = accessTextField
            break
        default:
            targetField = nil
        }
        
        // Handle changes to User Variables due to the list picker activity
        handleListPickerResult(secondType: secondType!, secondVC: secondVC)
        
        targetField!.text = secondVC.selectedItems.joined(separator: ", ")
    }
    
    // MARK: Actions
    
    @IBAction func setDefaultNameText(_ sender: UIButton) {
        nameTextField.text = "Note" + " - " + dateTime!
    }
    
    @IBAction func attemptSave(_ sender: UIBarButtonItem) {
        if saveRecord() {
            self.performSegue(withIdentifier: "exitSegue", sender: self)
        }
    }
    
    
    // MARK: Helper Methods
    
    override func checkRequiredData() -> Bool {
        var errorString : String?
        
        let dateCheck = dateTime != nil && dateTime != ""
        
        let locCheck = mode == "old" || (userOverrideStale || checkLocationOK())
        
        if !(nameTextField.text != nil && nameTextField.text != "") {
            errorString = "The Name field is required."
        } else if !(accessArray.count > 0) {
            errorString = "Select at least one Access Level."
        } else if !(notesTextField.text != nil && notesTextField.text != "") {
            errorString = "The Note field is required."
        } else if !(tagArray.count > 0) {
            errorString = "Select at least one Tag."
        }
        
        if let error = errorString {
            if #available(iOS 8.0, *) {
                let alertVC = UIAlertController(title: "Missing required data.", message: error, preferredStyle: .alert)
                let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                alertVC.addAction(okAction)
                present(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "Missing required data.", message: error, delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        }
        
        return errorString == nil && dateCheck && locCheck
    }
    
    override func setItemsOut() -> [String:AnyObject] {
        return ["name": nameTextField.text! as AnyObject, "tags": tagArray as AnyObject, "datatype": "note" as AnyObject, "datetime": dateTime! as AnyObject, "access": accessArray as AnyObject, "accuracy": gpsAcc as AnyObject, "text": notesTextField.text as AnyObject] as [String:AnyObject]
    }
}
