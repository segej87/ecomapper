//
//  RecordViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/4/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation

class RecordViewController: UIViewController, CLLocationManagerDelegate {
    
    /*
     The record being created or edited
     */
    var record: Record?
    
    /*
     This value will be filled with the date and time recorded when the view was opened
     */
    var dateTime: String?
    
    // The Core Location manager
    let locationManager = CLLocationManager()
    
    /*
     This value will be filled with the user's location by the CLLocationManager delegate
     */
    var userLoc: [Double]?
    var gpsAcc = 0.0
    
    
    /* 
     Arrays to hold selected multi-pick items (tags and access levels)
     */
    var tagArray = [String]()
    var accessArray = [String]()
    
    
    //MARK: Initialization

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Set up views if editing an existing Record.
        if let savedRecord = record {
            setupEditingMode(record: savedRecord)
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
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    // MARK: Location methods
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.last {
            // Current implementation of best accuracy algorithm
            if location.horizontalAccuracy < gpsAcc || gpsAcc == 0.0 {
                gpsAcc = location.horizontalAccuracy
                let lon = location.coordinate.longitude
                let lat = location.coordinate.latitude
                self.userLoc = [lon, lat]
                print("New best accuracy: \(gpsAcc) m")
                
                updateGPS()
            }
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Failed to find user's location: \(error.localizedDescription)")
        print("No location found, using null island")
        noGPS()
        let lon = 0.0
        let lat = 0.0
        self.userLoc = [lon, lat]
        checkValidName()
    }
    
    
    // MARK: Date methods
    func getDateTime(){
        let currentDate = Date()
        let dateFormatter = DateFormatter()
        //TODO: Get locale!!!!!!!!!!!!!!!!!
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateTime = dateFormatter.string(from: currentDate)
    }

    
    // MARK: Abstract Methods
    
    func setupEditingMode(record: Record) {
        preconditionFailure("This method must be overridden")
    }
    
    func updateGPS() {
        preconditionFailure("This method must be overriden")
    }
    
    func noGPS() {
        preconditionFailure("This method must be overriden")
    }
    
    func checkValidName() {
        preconditionFailure("This method must be overriden")
    }
}
