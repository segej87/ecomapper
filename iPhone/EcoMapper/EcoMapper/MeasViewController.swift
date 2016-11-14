//
//  MeasViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/20/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import QuartzCore

class MeasViewController: RecordViewController, UINavigationControllerDelegate {
    
    
    // MARK: Properties
    
    // IB Properties
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var accessTextField: UITextField!
    @IBOutlet weak var measTextField: UITextField!
    @IBOutlet weak var valTextField: UITextField!
    @IBOutlet weak var unitsTextField: UITextField!
    @IBOutlet weak var notesTextField: UITextView!
    @IBOutlet weak var tagTextField: UITextField!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var gpsAccView: UILabel!
    @IBOutlet weak var gpsStabView: UILabel!
    @IBOutlet weak var measPickerButton: UIButton!
    @IBOutlet weak var tagPickerButton: UIButton!
    @IBOutlet weak var unitsPickerButton: UIButton!
    @IBOutlet weak var accessPickerButton: UIButton!
    @IBOutlet weak var gpsReportArea: UIView!
    
    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    
    // MARK: UI Methods
    
    override func setUpFields() {
        if mode == "old" {
            if let record = record {
                navigationItem.title = "Editing Measurement"
                nameTextField.text = record.props["name"] as? String
                accessArray = record.props["access"] as! [String]
                species = record.props["species"] as? String
                valTextField.text = record.props["value"] as? String
                units = record.props["units"] as? String
                notesTextField.text = record.props["text"] as? String
                tagArray = record.props["tags"] as! [String]
                dateTime = record.props["datetime"] as? String
                userLoc = record.coords
                gpsReportArea.isHidden = true
            }
        }
        
        // Fill data into text fields
        accessTextField.text = accessArray.joined(separator: ", ")
        measTextField.text = species
        unitsTextField.text = units
        tagTextField.text = tagArray.joined(separator: ", ")
        
        // Add border to text view
        self.notesTextField.layer.borderWidth = 0.5
        self.notesTextField.layer.cornerRadius = 10
        self.notesTextField.layer.borderColor = UIColor.init(red: 200/255.0, green: 199/255.0, blue: 204/255.0, alpha: 1.0).cgColor
        
        // Handle text fields' user input through delegate callbacks.
        nameTextField.delegate = self
        valTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
    }
    
    
    // MARK: Location Methods
    
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
    
    // Prepare to segue away from Measurement view
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        locationManager.stopUpdatingLocation()
        
        if sender is UIButton {
            switch (sender as! UIButton) {
            case measPickerButton:
                let secondVC = segue.destination as! ListPickerViewController
                secondVC.itemType = "species"
                
                // Send previous species to ListPicker
                if let spec = species {
                    if spec != "" {
                        let measArray = [spec]
                        for m in measArray {
                            secondVC.selectedItems.append(m)
                        }
                    }
                }
                break
            case unitsPickerButton:
                let secondVC = segue.destination as! ListPickerViewController
                secondVC.itemType = "units"
                
                // Send previous species to ListPicker
                if let unit = units {
                    if unit != "" {
                        let unitsArray = [unit]
                        for u in unitsArray {
                            secondVC.selectedItems.append(u)
                        }
                    }
                }
                break
            case accessPickerButton:
                let secondVC = segue.destination as! ListPickerViewController
                secondVC.itemType = "access"
                
                // Send previous access levels to ListPicker
                if accessArray.count > 0 {
                    for a in accessArray {
                        secondVC.selectedItems.append(a)
                    }
                }
                break
            case tagPickerButton:
                if sender is UIButton && tagPickerButton === (sender as! UIButton) {
                    let secondVC = segue.destination as! ListPickerViewController
                    secondVC.itemType = "tags"
                    
                    // Send previous tags to ListPicker
                    if tagArray.count > 0 {
                        for t in tagArray {
                            secondVC.selectedItems.append(t)
                        }
                    }
                }
                break
            default:
                break
            }
        }
    }
    
    // Handle returns from list pickers
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
        case "species":
            targetField = measTextField
            break
        case "units":
            targetField = unitsTextField
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
        nameTextField.text = "Meas - " + dateTime!
    }
    
    @IBAction func attemptSave(_ sender: AnyObject) {
        if saveRecord() {
            self.performSegue(withIdentifier: "exitSegue", sender: self)
        }
    }
    
    
    // MARK: Helper methods
    
    override func checkRequiredData() -> Bool {
        var errorString : String?
        
        let dateCheck = dateTime != nil && dateTime != ""
        
        let locCheck = mode == "old" || (userOverrideStale || checkLocationOK())
        
        let valCheck = checkMeasFloatVal()
        
        if !(nameTextField.text != nil && nameTextField.text != "") {
            errorString = "The Name field is required."
        } else if !(accessArray.count > 0) {
            errorString = "Select at least one Access Level."
        } else if !(measTextField.text != nil && measTextField.text != "") {
            errorString = "Select a Measured Item."
        } else if !valCheck {
            errorString = "Enter a valid number in the Value field."
        } else if !(unitsTextField.text != nil && unitsTextField.text != "") {
            errorString = "Select Units."
        } else if !(tagArray.count > 0) {
            errorString = "Select at least one Tag."
        }
        
        if let error = errorString {
            if #available(iOS 8.0, *) {
                let alertVC = UIAlertController(title: "Missing required data", message: error, preferredStyle: .alert)
                let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                alertVC.addAction(okAction)
                present(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "Missing required data", message: error, delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        }
        
        return errorString == nil && dateCheck && locCheck
    }
    
    // Check that the supplied measurement is a valid float value
    func checkMeasFloatVal() -> Bool {
        let charset = CharacterSet(charactersIn: "abcdefghijklmnopqrstuvwxyz/\\!@#$%^&*()")
        if valTextField.text?.lowercased().rangeOfCharacter(from: charset) != nil {
            return false
        }
        
        let valFloat = (valTextField.text! as NSString).floatValue
        if (valFloat == 0.0 && !(valTextField.text! == "0.0")) {
            return false
        }
        return true
    }
    
    // Write the properties array for the record
    override func setItemsOut() -> [String:AnyObject] {
        return ["name": nameTextField.text! as AnyObject, "tags": tagArray as AnyObject, "datatype": "meas" as AnyObject, "datetime": dateTime! as AnyObject, "access": accessArray as AnyObject, "accuracy": gpsAcc as AnyObject, "text": notesTextField.text as AnyObject, "value": valTextField.text! as AnyObject, "species": measTextField.text! as AnyObject, "units": unitsTextField.text! as AnyObject] as [String:AnyObject]
    }
}
