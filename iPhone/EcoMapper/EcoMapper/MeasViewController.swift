//
//  MeasViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/20/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation

class MeasViewController: UIViewController, UITextFieldDelegate, UITextViewDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, CLLocationManagerDelegate, UIPickerViewDataSource, UIPickerViewDelegate {
    
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
        
        // If location is authorized, start location services
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestLocation()
        
        // Get the current datetime
        getDateTime()
        
        // Handle text fields' user input through delegate callbacks.
        nameTextField.delegate = self
        measTextField.delegate = self
        valTextField.delegate = self
        unitsTextField.delegate = self
        tagTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
        
        // Handle the access picker's user input
        accessPicker.dataSource = self
        accessPicker.delegate = self
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: UITextFieldDelegate
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
        
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

