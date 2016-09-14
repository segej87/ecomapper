//
//  MeasViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/20/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation
import QuartzCore

class MeasViewController: UIViewController, UITextFieldDelegate, UITextViewDelegate, UINavigationControllerDelegate, CLLocationManagerDelegate {
    
    // MARK: Properties
    // IB Properties
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var accessTextField: UITextField!
    @IBOutlet weak var measTextField: UITextField!
    @IBOutlet weak var valTextField: UITextField!
    @IBOutlet weak var unitsTextField: UITextField!
    @IBOutlet weak var notesTextField: UITextView!
    @IBOutlet weak var tagTextField: UITextField!
    @IBOutlet weak var noteLabel: UILabel!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var mainScrollView: UIScrollView!
    @IBOutlet weak var gpsAccView: UILabel!
    @IBOutlet weak var measPickerButton: UIButton!
    @IBOutlet weak var tagPickerButton: UIButton!
    @IBOutlet weak var unitsPickerButton: UIButton!
    @IBOutlet weak var accessPickerButton: UIButton!
    
    // Class variables
    // The Core Location manager
    let locationManager = CLLocationManager()
    
    // Array to hold selected multi-pick items (tags and access levels)
    var tagArray = [String]()
    var accessArray = [String]()
    
    /*
     This value will be filled with the user's location by the CLLocationManager delegate
     */
    var userLoc: [Double]?
    var gpsAcc = 0.0
    
    /*
     This value will be filled with the date and time recorded when the view was opened
     */
    var dateTime: String?
    
    /*
     This value is either passed by 'RecordTableViewController' in 'prepareForSegue(_:sender:)' or constructed as part of adding a new record.
     */
    var record: Record?
    
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
        measTextField.delegate = self
        valTextField.delegate = self
        unitsTextField.delegate = self
        tagTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
        
        // Set up views if editing an existing Record.
        if let record = record {
            navigationItem.title = "Editing Measurement"
            nameTextField.text = record.props["name"] as? String
            accessTextField.text = (record.props["access"] as? [String])?.joined(separator: ", ")
            accessArray = (record.props["access"] as? [String])!
            measTextField.text = record.props["species"] as? String
            valTextField.text = record.props["value"] as? String
            unitsTextField.text = record.props["units"] as? String
            notesTextField.text = record.props["text"] as? String
            tagTextField.text = (record.props["tags"] as? [String])?.joined(separator: ", ")
            tagArray = (record.props["tags"] as? [String])!
            dateTime = record.props["datetime"] as? String
            userLoc = record.coords
            gpsAccView.isHidden = true
        } else {
            // Get the current datetime
            getDateTime()
            
            // If location is authorized, start location services
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            if #available(iOS 8.0, *) {
                locationManager.requestWhenInUseAuthorization()
            } else {
                // Do nothing
            }
            locationManager.startUpdatingLocation()
        }
        
        // Enable the Save button only if the required text fields have a valid name.
        checkValidName()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: UITextFieldDelegate
    
    func textFieldDidBeginEditing(_ textField: UITextField) {
        // Disable the Save button while editing.
        saveButton.isEnabled = false
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        checkValidName()
    }
    
    func checkValidName() {
        // Disable the Save button if the required text fields are empty.
        let text1 = nameTextField.text ?? ""
        let text2 = measTextField.text ?? ""
        let text3 = valTextField.text ?? ""
        let text4 = unitsTextField.text ?? ""
        let text5 = tagTextField.text ?? ""
        let text6 = accessTextField.text ?? ""
        let loc1 = userLoc ?? nil
        
        saveButton.isEnabled = !(text1.isEmpty || text2.isEmpty || text3.isEmpty || text4.isEmpty || text5.isEmpty || text6.isEmpty || loc1 == nil || !checkMeasFloatVal())
    }
    
    // TODO: Improve this check (if string starts with valid float it will pass, even if letter is in there)
    func checkMeasFloatVal() -> Bool {
        let valFloat = (valTextField.text! as NSString).floatValue
        if (valFloat == 0.0 && !(valTextField.text! == "0.0")) {
            return false
        }
        return true
    }
    
    // MARK: UITextViewDelegate
    
    func textViewDidChange(_ textView: UITextView) {
        
    }
    
    func textViewShouldReturn(_ textView: UITextView) -> Bool {
        
        //Hide the keyboard.
        textView.resignFirstResponder()
        return true
    }
    
    // MARK: Location methods
    
    func noGPS() {
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
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.last {
            // Current implementation of best accuracy algorithm
            if location.horizontalAccuracy < gpsAcc || gpsAcc == 0.0 {
                gpsAcc = location.horizontalAccuracy
                let lon = location.coordinate.longitude
                let lat = location.coordinate.latitude
                self.userLoc = [lon, lat]
                print("New best accuracy: \(gpsAcc) m")
                
                gpsAccView.text = "Current GPS Accuracy: \(gpsAcc) m"
            }
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Failed to find user's location: \(error.localizedDescription)")
        print("No location found, using default")
        noGPS()
        let lon = -123.45
        let lat = 67.89
        self.userLoc = [lon, lat]
        checkValidName()
    }
    
    // MARK: Date methods
    
    func getDateTime(){
        let currentDate = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateTime = dateFormatter.string(from: currentDate)
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
            
            print("Using location \(userLoc!) with best accuracy \(gpsAcc) m")
            
            let props = ["name": name as AnyObject, "tags": tagArray as AnyObject, "datatype": "meas" as AnyObject, "datetime": dateTime! as AnyObject, "access": accessArray as AnyObject, "accuracy": gpsAcc as AnyObject, "text": notesTextField.text as AnyObject, "value": valTextField.text! as AnyObject, "species": measTextField.text! as AnyObject, "units": unitsTextField.text! as AnyObject] as [String:AnyObject]
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: self.userLoc!, photo: nil, props: props)
        }
        
        // If the add species button was pressed, present the item picker with a species item type
        if sender is UIButton && measPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "species"
            
            // Send previous species to ListPicker
            if measTextField.text != "" {
                let measArray = measTextField.text?.components(separatedBy: ";")
                for m in measArray! {
                    secondVC.selectedItems.append(m)
                }
            }
        }
        
        // If the add units button was pressed, present the item picker with a units item type
        if sender is UIButton && unitsPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "units"
            
            // Send previous species to ListPicker
            if unitsTextField.text != "" {
                let unitsArray = unitsTextField.text?.components(separatedBy: ";")
                for u in unitsArray! {
                    secondVC.selectedItems.append(u)
                }
            }
        }
        
        // If the add access button was pressed, present the item picker with an access item type
        if sender is UIButton && accessPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "access"
            
            // Send previous access levels to ListPicker
            if accessTextField.text != "" {
                let accessArray = accessTextField.text?.components(separatedBy: ";")
                for a in accessArray! {
                    secondVC.selectedItems.append(a)
                }
            }
        }
        
        // If the add tags button was pressed, present the item picker with a tags item type
        if sender is UIButton && tagPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "tags"
            
            // Send previous tags to ListPicker
            if tagTextField.text != "" {
                let tagArray = tagTextField.text?.components(separatedBy: ", ")
                for t in tagArray! {
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
            tagArray = secondVC.selectedItems
        } else if secondType == "species" {
            targetField = measTextField
        } else if secondType == "units" {
            targetField = unitsTextField
        } else if secondType == "access" {
            targetField = accessTextField
            accessArray = secondVC.selectedItems
        }
        
        if targetField!.text != "" {
            let prevText = targetField!.text
            let prevArray = prevText!.components(separatedBy: ";")
            for p in prevArray {
                if secondType == "tags" {
                    var pTag = UserVars.Tags[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = ((pTag![1] as! Int - 1) as AnyObject)
                        if pTag![1] as! Int == 0 {
                            UserVars.Tags.removeValue(forKey: p)
                        } else {
                            UserVars.Tags[p] = pTag!
                        }
                    }
                } else if secondType == "species" {
                    var pTag = UserVars.Species[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = ((pTag![1] as! Int - 1) as AnyObject)
                        if pTag![1] as! Int == 0 {
                            UserVars.Species.removeValue(forKey: p)
                        } else {
                            UserVars.Species[p] = pTag!
                        }
                    }
                } else if secondType == "units" {
                    var pTag = UserVars.Units[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = ((pTag![1] as! Int - 1) as AnyObject)
                        if pTag![1] as! Int == 0 {
                            UserVars.Units.removeValue(forKey: p)
                        } else {
                            UserVars.Units[p] = pTag!
                        }
                    }
                }
            }
        }
        
        for t in secondVC.selectedItems {
            if secondType == "tags" {
                if !UserVars.Tags.keys.contains(t) {
                    UserVars.Tags[t] = ["Local" as AnyObject,1 as AnyObject]
                } else {
                    var tagInfo = UserVars.Tags[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Tags[t] = tagInfo
                    }
                }
            } else if secondType == "species" {
                if !UserVars.Species.keys.contains(t) {
                    UserVars.Species[t] = ["Local" as AnyObject,1 as AnyObject]
                } else {
                    var tagInfo = UserVars.Species[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Species[t] = tagInfo
                    }
                }
            } else if secondType == "units" {
                if !UserVars.Units.keys.contains(t) {
                    UserVars.Units[t] = ["Local" as AnyObject,1 as AnyObject]
                } else {
                    var tagInfo = UserVars.Units[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Units[t] = tagInfo
                    }
                }
            }
        }
        
        
        targetField!.text = secondVC.selectedItems.joined(separator: ", ")
        
        checkValidName()
    }
    
    // MARK: Actions
    
    @IBAction func setDefaultNameText(_ sender: UIButton) {
        nameTextField.text = "Meas" + " - " + dateTime!
    }
    
}

