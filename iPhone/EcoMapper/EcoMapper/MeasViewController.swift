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
    
    let locationManager = CLLocationManager()
    
    let pickerData = UserVars.AccessLevels
    
    var accessLevel: String?
    
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
        nav?.barStyle = UIBarStyle.Black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGrayColor()
        
        // Add border to text view
        self.notesTextField.layer.borderWidth = 0.5
        self.notesTextField.layer.cornerRadius = 10
        self.notesTextField.layer.borderColor = UIColor.init(red: 200/255.0, green: 199/255.0, blue: 204/255.0, alpha: 1.0).CGColor
        
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
            accessTextField.text = record.props["access"] as? String
            measTextField.text = record.props["species"] as? String
            valTextField.text = record.props["value"] as? String
            unitsTextField.text = record.props["units"] as? String
            notesTextField.text = record.props["text"] as? String
            tagTextField.text = record.props["tags"] as? String
            dateTime = record.props["datetime"] as? String
            userLoc = record.coords
            gpsAccView.hidden = true
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
    
    func textFieldDidBeginEditing(textField: UITextField) {
        // Disable the Save button while editing.
        saveButton.enabled = false
    }
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
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
        
        saveButton.enabled = !(text1.isEmpty || text2.isEmpty || text3.isEmpty || text4.isEmpty || text5.isEmpty || text6.isEmpty || loc1 == nil || !checkMeasFloatVal())
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
    
    func textViewDidChange(textView: UITextView) {
        
    }
    
    func textViewShouldReturn(textView: UITextView) -> Bool {
        
        //Hide the keyboard.
        textView.resignFirstResponder()
        return true
    }
    
    // MARK: Location methods
    
    func noGPS() {
        if #available(iOS 8.0, *) {
            let alertVC = UIAlertController(title: "No GPS", message: "Can't pinpoint your location, using default", preferredStyle: .Alert)
            let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
            alertVC.addAction(okAction)
            presentViewController(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: "No GPS", message: "Can't pinpoint your location, using default", delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
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
    
    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
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
        let currentDate = NSDate()
        let dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateTime = dateFormatter.stringFromDate(currentDate)
    }
    
    // MARK: Navigation
    
    @IBAction func cancel(sender: UIBarButtonItem) {
        locationManager.stopUpdatingLocation()
        
        // Depending on style of presentation (modal or push), dismiss the view controller differently
        let isPresentingInAddRecordMode = presentingViewController is UINavigationController
        if isPresentingInAddRecordMode {
            dismissViewControllerAnimated(true, completion: nil)
        } else {
            navigationController!.popViewControllerAnimated(true)
        }
    }
    
    // This method lets you configure a view controller before it's presented.
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        locationManager.stopUpdatingLocation()
        
        if saveButton === sender {
            let name = nameTextField.text ?? ""
            
            print("Using location \(userLoc!) with best accuracy \(gpsAcc) m")
            
            let props = ["name": name, "tags": tagTextField.text!, "datatype": "meas", "datetime": dateTime!, "access": accessTextField.text!, "accuracy": gpsAcc, "text": notesTextField.text, "value": valTextField.text!, "species": measTextField.text!, "units": unitsTextField.text!] as [String:AnyObject]
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: self.userLoc!, photo: nil, props: props)
        }
        
        // If the add species button was pressed, present the item picker with a species item type
        if measPickerButton === sender {
            let secondVC = segue.destinationViewController as! ListPickerViewController
            secondVC.itemType = "species"
            
            // Send previous species to ListPicker
            if measTextField.text != "" {
                let measArray = measTextField.text?.componentsSeparatedByString(";")
                for m in measArray! {
                    secondVC.selectedItems.append(m)
                }
            }
        }
        
        // If the add units button was pressed, present the item picker with a units item type
        if unitsPickerButton === sender {
            let secondVC = segue.destinationViewController as! ListPickerViewController
            secondVC.itemType = "units"
            
            // Send previous species to ListPicker
            if unitsTextField.text != "" {
                let unitsArray = unitsTextField.text?.componentsSeparatedByString(";")
                for u in unitsArray! {
                    secondVC.selectedItems.append(u)
                }
            }
        }
        
        // If the add access button was pressed, present the item picker with an access item type
        if accessPickerButton === sender {
            let secondVC = segue.destinationViewController as! ListPickerViewController
            secondVC.itemType = "access"
            
            // Send previous access levels to ListPicker
            if accessTextField.text != "" {
                let accessArray = accessTextField.text?.componentsSeparatedByString(";")
                for a in accessArray! {
                    secondVC.selectedItems.append(a)
                }
            }
        }
        
        // If the add tags button was pressed, present the item picker with a tags item type
        if tagPickerButton === sender {
            let secondVC = segue.destinationViewController as! ListPickerViewController
            secondVC.itemType = "tags"
            
            // Send previous tags to ListPicker
            if tagTextField.text != "" {
                let tagArray = tagTextField.text?.componentsSeparatedByString(";")
                for t in tagArray! {
                    secondVC.selectedItems.append(t)
                }
            }
        }
    }
    
    @IBAction func unwindFromTagController(segue: UIStoryboardSegue) {
        let secondVC : ListPickerViewController = segue.sourceViewController as! ListPickerViewController
        
        let secondType = secondVC.itemType
        
        var targetField : UITextField?
        
        if secondType == "tags" {
            targetField = tagTextField
        } else if secondType == "species" {
            targetField = measTextField
        } else if secondType == "units" {
            targetField = unitsTextField
        } else if secondType == "access" {
            targetField = accessTextField
        }
        
        if targetField!.text != "" {
            let prevText = targetField!.text
            let prevArray = prevText!.componentsSeparatedByString(";")
            for p in prevArray {
                if secondType == "tags" {
                    var pTag = UserVars.Tags[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = pTag![1] as! Int - 1
                        if pTag![1] as! Int == 0 {
                            UserVars.Tags.removeValueForKey(p)
                        } else {
                            UserVars.Tags[p] = pTag!
                        }
                    }
                } else if secondType == "species" {
                    var pTag = UserVars.Species[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = pTag![1] as! Int - 1
                        if pTag![1] as! Int == 0 {
                            UserVars.Species.removeValueForKey(p)
                        } else {
                            UserVars.Species[p] = pTag!
                        }
                    }
                } else if secondType == "units" {
                    var pTag = UserVars.Units[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = pTag![1] as! Int - 1
                        if pTag![1] as! Int == 0 {
                            UserVars.Units.removeValueForKey(p)
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
                    UserVars.Tags[t] = ["Local",1]
                } else {
                    var tagInfo = UserVars.Tags[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = tagInfo![1] as! Int + 1
                        UserVars.Tags[t] = tagInfo
                    }
                }
            } else if secondType == "species" {
                if !UserVars.Species.keys.contains(t) {
                    UserVars.Species[t] = ["Local",1]
                } else {
                    var tagInfo = UserVars.Species[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = tagInfo![1] as! Int + 1
                        UserVars.Species[t] = tagInfo
                    }
                }
            } else if secondType == "units" {
                if !UserVars.Units.keys.contains(t) {
                    UserVars.Units[t] = ["Local",1]
                } else {
                    var tagInfo = UserVars.Units[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = tagInfo![1] as! Int + 1
                        UserVars.Units[t] = tagInfo
                    }
                }
            }
        }
        
        targetField!.text = secondVC.selectedItems.joinWithSeparator(";")
        
        checkValidName()
    }
    
    // MARK: Actions
    
    @IBAction func setDefaultNameText(sender: UIButton) {
        nameTextField.text = "Meas" + " - " + dateTime!
    }
    
}

