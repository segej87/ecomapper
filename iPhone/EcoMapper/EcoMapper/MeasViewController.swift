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

class MeasViewController: UIViewController, UITextFieldDelegate, UITextViewDelegate, UINavigationControllerDelegate, CLLocationManagerDelegate, UIPickerViewDataSource, UIPickerViewDelegate {
    
    // MARK: Properties
    
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var photoImageView: UIImageView!
    @IBOutlet weak var accessPicker: UIPickerView!
    @IBOutlet weak var measTextField: UITextField!
    @IBOutlet weak var valTextField: UITextField!
    @IBOutlet weak var unitsTextField: UITextField!
    @IBOutlet weak var notesTextField: UITextView!
    @IBOutlet weak var tagTextField: UITextField!
    @IBOutlet weak var photoStackView: UIStackView!
    @IBOutlet weak var measStackView: UIStackView!
    @IBOutlet weak var noteLabel: UILabel!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    
    let locationManager = CLLocationManager()
    
    let pickerData = ["public", "institution", "private"]
    
    var accessLevel: String?
    
    /*
     This value will be filled with the user's location by the CLLocationManager delegate
     */
    var userLoc: [Double]?
    
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
        
        // Add border to text view
        self.notesTextField.layer.borderWidth = 0.5
        self.notesTextField.layer.cornerRadius = 10
        self.notesTextField.layer.borderColor = UIColor.init(red: 200/255.0, green: 199/255.0, blue: 204/255.0, alpha: 1.0).CGColor
        
        // If location is authorized, start location services
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestLocation()
        
        // Handle text fields' user input through delegate callbacks.
        nameTextField.delegate = self
        measTextField.delegate = self
        valTextField.delegate = self
        unitsTextField.delegate = self
        tagTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
        
        // Handle the access picker's user input.
        accessPicker.dataSource = self
        accessPicker.delegate = self
        
        // Set up views if editing an existing Record.
        if let record = record {
            navigationItem.title = "Editing Measurement"
            nameTextField.text = record.props["name"] as? String
            accessPicker.selectRow(pickerData.indexOf(record.props["access"] as! String)!, inComponent: 0, animated: true)
            accessLevel = pickerData[accessPicker.selectedRowInComponent(0)]
            measTextField.text = record.props["species"] as? String
            valTextField.text = record.props["value"] as? String
            unitsTextField.text = record.props["units"] as? String
            notesTextField.text = record.props["text"] as? String
            tagTextField.text = record.props["tags"] as? String
            dateTime = record.props["datetime"] as? String
        } else {
            // Set a default index for the picker to prevent errors.
            //TODO: Set default access from another menu
            let defaultRowForAccess = 0
            accessPicker.selectRow(defaultRowForAccess, inComponent: 0, animated: false)
            accessLevel = pickerData[accessPicker.selectedRowInComponent(0)]
            
            // Get the current datetime
            getDateTime()
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
    
    func checkValidName() {
        // Disable the Save button if the required text fields are empty.
        let text1 = nameTextField.text ?? ""
        let text2 = measTextField.text ?? ""
        let text3 = valTextField.text ?? ""
        let text4 = unitsTextField.text ?? ""
        let text5 = tagTextField.text ?? ""
        saveButton.enabled = !(text1.isEmpty || text2.isEmpty || text3.isEmpty || text4.isEmpty || text5.isEmpty)
    }
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
        checkValidName()
    }
    
    // MARK: UITextViewDelegate
    
    func textViewDidChange(textView: UITextView) {
        
    }
    
    // MARK: UIPicker delegate and data sources
        // MARK: Data Sources
    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return pickerData.count
    }
    
        // Mark: Delegates
    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return pickerData[row]
    }
    
    func pickerView(pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        self.accessLevel = pickerData[row]
    }
    
    // MARK: Location methods
        // TODO: only take if available, otherwise throw up error
        // TODO: implement stability check before allowing reading
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            let lon = location.coordinate.longitude
            let lat = location.coordinate.latitude
            self.userLoc = [lon, lat]
        }
    }
    
    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
        print("Failed to find user's location: \(error.localizedDescription)")
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
        if saveButton === sender {
            let name = nameTextField.text ?? ""
            
            // TODO: Make this pull from GPS (if available)
            var coords: [Double]?
            if userLoc == nil {
                print("No location found, using default")
                coords = [-123.45, 67.89]
            } else {
                print("Location found:  \(userLoc)")
                coords = userLoc
            }
            //let coords = userLoc!
            
            let props = ["name": name, "tags": tagTextField.text, "datatype": "meas", "datetime": dateTime, "access": accessLevel, "text": notesTextField.text, "value": valTextField.text, "species": measTextField.text, "units": unitsTextField.text]
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: coords!, photo: nil, props: props)
        }
    }
    
    // MARK: Actions
    
    @IBAction func setDefaultNameText(sender: UIButton) {
        nameTextField.text = "Meas" + " - " + dateTime!
    }
    
}

