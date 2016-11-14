//
//  RecordViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/4/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation

class RecordViewController: UIViewController, UITextFieldDelegate, UITextViewDelegate, CLLocationManagerDelegate {
    
    
    // MARK: Properties
    
    /*
     The record being created or edited
     */
    var record: Record?
    var mode : String?
    
    /*
     This value will be filled with the date and time recorded when the view was opened
     */
    var dateTime: String?
    
    /* 
     The Core Location manager
     */
    let locationManager = CLLocationManager()
    
    /*
     These values will be filled with the user's location by the CLLocationManager delegate
     */
    var userLoc: [Double]?
    var gpsAcc = -1 as Double
    var gpsStab = -1 as Double
    var stabList = [Double]()
    var latestLoc: CLLocation?
    
    /*
     A flag indicating whether the user has allowed an uncertain location
     */
    var userOverrideStale = false
    
    /*
     Arrays to hold selected multi-pick items (tags and access levels)
     */
    var tagArray = [String]()
    var accessArray = [String]()
    
    /*
     Strings for measurement values
     */
    var species : String?
    var units : String?
    
    /*
     An image for photos
     */
    var selectedImage : UIImage?
    
    
    //MARK: Initialization

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style the navigation bar
        styleNavigationBar()
        
        // Set up views if editing an existing Record.
        if record != nil {
            mode = "old"
        } else {
            mode = "new"
            
            // Get the current datetime
            getDateTime()
            
            tagArray = UserVars.TagsDefaults
            accessArray = UserVars.AccessDefaults
            species = UserVars.SpecDefault
            units = UserVars.UnitsDefault
            
            // If location is authorized, start location services
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.requestWhenInUseAuthorization()
            locationManager.startUpdatingLocation()
        }
        setUpFields()
    }
    
    
    // MARK: UITextFieldDelegate
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    
    // MARK: UITextViewDelegate
    
    func textViewShouldReturn(_ textView: UITextView) -> Bool {
        //Hide the keyboard.
        textView.resignFirstResponder()
        return true
    }
    
    
    // MARK: Location methods
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        NSLog("Failed to find user's location: \(error.localizedDescription)")
        NSLog("No location found, using null island")
        
        // Set the coordinates array to null island
        let lon = 0.0
        let lat = 0.0
        let elev = 0.0
        self.userLoc = [lon, lat, elev]
        
        // Report a location failure to the user
        noGPS()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.last {
            
            //Calculate stability
            if let lastLoc = latestLoc {
                stabList.append(location.distance(from: lastLoc))
            } else {
                stabList.append(-1)
            }
            gpsStab = calculateStability()
            
            // Get the location properties and set the coordinates array
            gpsAcc = location.horizontalAccuracy
            let lon = location.coordinate.longitude
            let lat = location.coordinate.latitude
            let elev = location.altitude
            self.userLoc = [lon, lat, elev]
            
            // Call an update to the UI's GPS reporter
            updateGPS()
            
            // Set the newest location as the class' location property
            latestLoc = location
        }
    }
    
    func checkLocationOK() -> Bool {
        var staleLoc : Bool
        
        guard let loc = latestLoc else {
            let alertVC = UIAlertController(title: "Your location was not found", message: "Please wait until the GPS is stable", preferredStyle: .alert)
            let waitAction = UIAlertAction(title: "Wait", style: .default, handler: nil)
            let ignoreAction = UIAlertAction(title: "Ignore", style: .default, handler: ignoreLocWarning)
            alertVC.addAction(waitAction)
            alertVC.addAction(ignoreAction)
            present(alertVC, animated: true, completion: nil)
            
            return false
        }
        
        let elapsedTime = Date().compare(loc.timestamp).rawValue/60
        
        staleLoc = elapsedTime > UserVars.maxUpdateTime || (gpsAcc == -1 || gpsAcc > UserVars.minGPSAccuracy) || (gpsStab == -1 || gpsStab > UserVars.minGPSStability)
        
        if staleLoc {
            var outString = ""
            
            if elapsedTime > UserVars.maxUpdateTime {
                outString.append("Last location update: \(String(elapsedTime)) minutes ago.\n\n")
            }
            
            if gpsAcc == -1 {
                outString.append("Current accuracy: Locking\n\n")
            } else if gpsAcc > UserVars.minGPSAccuracy {
                outString.append("Current accuracy: \(gpsAcc) m\n\n")
            }
            
            if gpsStab == -1 {
                outString.append("Current stability: Locking\n\n")
            } else if gpsStab > UserVars.minGPSStability {
                outString.append("Current stability: \(gpsStab) m\n\n")
            }
            
            outString.append("Please wait until the GPS is stable")
            
            let alertVC = UIAlertController(title: "Your location may not be accurate.", message: outString, preferredStyle: .alert)
            let waitAction = UIAlertAction(title: "Wait", style: .default, handler: nil)
            let ignoreAction = UIAlertAction(title: "Ignore", style: .default, handler: ignoreLocWarning)
            alertVC.addAction(waitAction)
            alertVC.addAction(ignoreAction)
            present(alertVC, animated: true, completion: nil)
        }
        
        return !staleLoc
    }
    
    func ignoreLocWarning(action: UIAlertAction) -> Void {
        userOverrideStale = true
    }
    
    
    // MARK: Date methods
    
    func getDateTime(){
        let currentDate = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateTime = dateFormatter.string(from: currentDate)
    }
    
    
    // MARK: UI Methods
    
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
    
    
    // MARK: Data I/O
    
    func handleListPickerResult(secondType: String, secondVC: ListPickerViewController) {
        
        switch secondType {
        case "tags":
            for p in tagArray {
                if var tag = UserVars.Tags[p] {
                    if tag[0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        tag[1] = ((tag[1] as! Int - 1) as AnyObject)
                        if tag[1] as! Int == 0 {
                            UserVars.Tags.removeValue(forKey: p)
                        } else {
                            UserVars.Tags[p] = tag
                        }
                    }
                }
            }
            
            for t in secondVC.selectedItems {
                if !UserVars.Tags.keys.contains(t) {
                    UserVars.Tags[t] = ["Local" as AnyObject,1 as AnyObject]
                } else if !tagArray.contains(t) {
                    var tagInfo = UserVars.Tags[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Tags[t] = tagInfo
                    }
                }
            }
            
            tagArray = secondVC.selectedItems
            break
        case "species":
            if let spec = species {
                if spec != "" {
                    if var s = UserVars.Species[spec] {
                        if s[0] as! String == "Local" && !secondVC.selectedItems.contains(spec) {
                            s[1] = ((s[1] as! Int - 1) as AnyObject)
                            if s[1] as! Int == 0 {
                                UserVars.Species.removeValue(forKey: spec)
                            } else {
                                UserVars.Species[spec] = s
                            }
                        }
                    }
                }
            }
            
            for t in secondVC.selectedItems {
                if !UserVars.Species.keys.contains(t) {
                    UserVars.Species[t] = ["Local" as AnyObject,1 as AnyObject]
                } else if species != t{
                    var tagInfo = UserVars.Species[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Species[t] = tagInfo
                    }
                }
            }
            
            if secondVC.selectedItems.count > 0 {
                species = secondVC.selectedItems[0]
            } else {
                species = nil
            }
            break
        case "units":
            if let unit = units {
                if unit != "" {
                    if var u = UserVars.Units[unit] {
                        if u[0] as! String == "Local" && !secondVC.selectedItems.contains(unit) {
                            u[1] = ((u[1] as! Int - 1) as AnyObject)
                            if u[1] as! Int == 0 {
                                UserVars.Units.removeValue(forKey: unit)
                            } else {
                                UserVars.Units[unit] = u
                            }
                        }
                    }
                }
            }
            
            for t in secondVC.selectedItems {
                if !UserVars.Units.keys.contains(t) {
                    UserVars.Units[t] = ["Local" as AnyObject,1 as AnyObject]
                } else if units != t {
                    var tagInfo = UserVars.Units[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = ((tagInfo![1] as! Int + 1) as AnyObject)
                        UserVars.Units[t] = tagInfo
                    }
                }
            }
            
            if secondVC.selectedItems.count > 0 {
                units = secondVC.selectedItems[0]
            } else {
                units = nil
            }
            break
        case "access":
            accessArray = secondVC.selectedItems
            break
        default:
            break
        }
    }
    
    func saveRecord() -> Bool {
        if checkRequiredData() {
            let props = setItemsOut()
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: self.userLoc!, photo: selectedImage, props: props)
            
            return true
        } else {
            return false
        }
    }
    
    
    // MARK: Navigation
    
    func cancelView() {
        if mode == "new" {
            if #available(iOS 8.0, *) {
                let alertVC = UIAlertController(title: "Are you sure?", message: "All info in this record will be lost.", preferredStyle: .alert)
                let leaveAction = UIAlertAction(title: "Leave", style: .default, handler: self.handleLeave)
                let stayAction = UIAlertAction(title: "Stay", style: .default, handler: nil)
                alertVC.addAction(leaveAction)
                alertVC.addAction(stayAction)
                present(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "No GPS", message: "Can't pinpoint your location, using default", delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        } else {
            locationManager.stopUpdatingLocation()
            
            // Depending on style of presentation (modal or push), dismiss the view controller differently
            let isPresentingInAddRecordMode = presentingViewController is UINavigationController
            if isPresentingInAddRecordMode {
                dismiss(animated: true, completion: nil)
            } else {
                navigationController!.popViewController(animated: true)
            }
        }
    }
    
    func handleLeave(alert: UIAlertAction) -> Void {
        locationManager.stopUpdatingLocation()
        
        // Depending on style of presentation (modal or push), dismiss the view controller differently
        let isPresentingInAddRecordMode = presentingViewController is UINavigationController
        if isPresentingInAddRecordMode {
            dismiss(animated: true, completion: nil)
        } else {
            navigationController!.popViewController(animated: true)
        }
    }

    
    // MARK: Abstract Methods
    
    func setUpFields() {
        preconditionFailure("This method must be overriden")
    }
    
    func updateGPS() {
        preconditionFailure("This method must be overriden")
    }
    
    func checkRequiredData() -> Bool {
        preconditionFailure("This method must be overriden")
    }
    
    func setItemsOut() -> [String:AnyObject] {
        preconditionFailure("This method must be overriden")
    }
    
    
    // MARK: Helper Methods
    
    func calculateStability() -> Double {
        let numAv = 5
        let stabListSize = stabList.count
        var stabAdd: Double
        
        var counter = 0
        
        if stabListSize < numAv {
            stabAdd = stabList[0]
            counter += 1
            if stabListSize > 1 {
                for s in stabList[1...(stabListSize - 1)] {
                    if s != -1 {
                        stabAdd = stabAdd + s
                        counter += 1
                    }
                }
            }
        } else {
            stabAdd = stabList[stabListSize - numAv]
            counter += 1
            for s in stabList[(stabListSize - (numAv - 1))...(stabListSize - 1)] {
                if s != -1 {
                    stabAdd = stabAdd + s
                    counter += 1
                }
            }
        }
        
        if counter != 0 {
            return stabAdd/Double(counter)
        }
        
        return -1
    }
    
    func styleNavigationBar() {
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGray
    }
}
