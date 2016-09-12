//
//  PhotoViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/21/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import CoreLocation

class PhotoViewController: UIViewController, UITextFieldDelegate, UITextViewDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, CLLocationManagerDelegate {
    
    // MARK: Properties
    
    // Outlets
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var photoImageView: UIImageView!
    @IBOutlet weak var accessTextField: UITextField!
    @IBOutlet weak var notesTextField: UITextView!
    @IBOutlet weak var tagTextField: UITextField!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var gpsAccView: UILabel!
    @IBOutlet weak var accessPickerButton: UIButton!
    @IBOutlet weak var tagPickerButton: UIButton!
    
    // Location manager for GPS location
    let locationManager = CLLocationManager()
    
    // A flag indicating whether a new photo is being taken
    var newPhoto = false
    
    // The path to the photo on the device's drive
    var photoURL: NSURL?
    
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
    var media: Media?
    
    var medOutName: String?
    
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
        tagTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
        
        // Set up views if editing an existing Record.
        if let record = record {
            navigationItem.title = "Editing Photo"
            nameTextField.text = record.props["name"] as? String
            accessTextField.text = record.props["access"] as? String
            photoImageView.image = record.photo
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
    
    // MARK: Camera alert
    
    func noCamera() {
        if #available(iOS 8.0, *) {
            let alertVC = UIAlertController(title: "No Camera", message: "Sorry, this device doesn't have a camera", preferredStyle: .Alert)
            let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
            alertVC.addAction(okAction)
            presentViewController(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: "No Camera", message: "Sorry, this device doesn't have a camera", delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
    
    // MARK: UIImagePickerControllerDelegate
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        
        // Dismiss the picker if the user canceled.
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        
        // The info dictionary contains multiple representations of the image, and this uses the original.
        let selectedImage = info[UIImagePickerControllerOriginalImage] as! UIImage
        
        // Save the image if it was taken from the camera.
        if newPhoto {
            
            // Set the name of the photo
            medOutName = "Photo_\(dateTime!.stringByReplacingOccurrencesOfString("-", withString: "").stringByReplacingOccurrencesOfString(":", withString: "").stringByReplacingOccurrencesOfString(" ", withString: "_")).jpg"
            
            // Set path of photo to be saved
            photoURL = UserVars.PhotosURL.URLByAppendingPathComponent(medOutName!)
            
            // Create an NSCoded photo object
            let outPhoto = NewPhoto(photo: selectedImage)
            
            // Save the photo to the photos directory
            savePhoto(outPhoto!)
            
        } else {
            
            // Get the url of the selected asset and set it to the instance variable
            let imageURL = info[UIImagePickerControllerReferenceURL] as! NSURL
            
            photoURL = imageURL
            
            // Set the name of the photo
            medOutName = "Photo_\(dateTime!.stringByReplacingOccurrencesOfString("-", withString: "").stringByReplacingOccurrencesOfString(":", withString: "").stringByReplacingOccurrencesOfString(" ", withString: "_")).\(imageURL.absoluteString.substringFromIndex(imageURL.absoluteString.rangeOfString("ext=")!.endIndex))"
        }
        
        // Set the aspect ratio of the image in the view
        photoImageView.contentMode = .ScaleAspectFit
        
        // Set photoImageView to display the selected image.
        photoImageView.image = selectedImage
        
        // Dismiss the picker.
        dismissViewControllerAnimated(true, completion: nil)
    }

    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: UITextFieldDelegate
    
    func checkValidName() {
        // Disable the Save button if the required text fields are empty.
        let text1 = nameTextField.text ?? ""
        let text2 = tagTextField.text ?? ""
        let photo1 = photoImageView.image ?? nil
        let loc1 = userLoc ?? nil
        saveButton.enabled = !(text1.isEmpty || text2.isEmpty || photo1 == nil || loc1 == nil)
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
            // TODO: Allow user to choose whether to use photo location or current location
            
            let name = nameTextField.text ?? ""
            let urlOut = photoURL!.absoluteString
            
            let props = ["name": name, "tags": tagTextField.text!, "datatype": "photo", "datetime": dateTime!, "access": accessTextField.text!, "accuracy": gpsAcc, "text": notesTextField.text, "filepath": urlOut] as [String:AnyObject]
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: userLoc!, photo: photoImageView.image, props: props)
            
            // Set the media reference to be passed to RecordTableViewController after the unwind segue.
            media = Media(name: medOutName!, path: photoURL)
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
    
    @IBAction func unwindFromListPicker(segue: UIStoryboardSegue) {
        let secondVC : ListPickerViewController = segue.sourceViewController as! ListPickerViewController
        
        let secondType = secondVC.itemType
        
        var targetField : UITextField?
        
        if secondType == "tags" {
            targetField = tagTextField
        } else if secondType == "access" {
            targetField = accessTextField
        }
        
        if secondType == "tags" {
            if tagTextField.text != "" {
                let prevText = tagTextField.text
                let prevArray = prevText!.componentsSeparatedByString(";")
                for p in prevArray {
                    var pTag = UserVars.Tags[p]
                    if pTag![0] as! String == "Local" && !secondVC.selectedItems.contains(p) {
                        pTag![1] = pTag![1] as! Int - 1
                        if pTag![1] as! Int == 0 {
                            UserVars.Tags.removeValueForKey(p)
                        } else {
                            UserVars.Tags[p] = pTag!
                        }
                    }
                }
            }
            
            for t in secondVC.selectedItems {
                if !UserVars.Tags.keys.contains(t) {
                    UserVars.Tags[t] = ["Local",1]
                } else {
                    var tagInfo = UserVars.Tags[t]
                    if tagInfo![0] as! String == "Local" {
                        tagInfo![1] = tagInfo![1] as! Int + 1
                        UserVars.Tags[t] = tagInfo
                    }
                }
            }
        }
        
        
        targetField!.text = secondVC.selectedItems.joinWithSeparator(";")
        
        checkValidName()
    }
    
    // MARK: Actions
    
    @IBAction func selectImage(sender: UITapGestureRecognizer) {
        
        // Hide the keyboard.
        nameTextField.resignFirstResponder()
        
        // UIImagePickerController is a view controller that lets a user pick media from their photo library.
        let imagePickerController = UIImagePickerController()
        
        // Allow photos to be picked from existing photos.
        imagePickerController.sourceType = .PhotoLibrary
        
        // Indicate that a previously existing photo is being selected.
        newPhoto = false
        
        // Make sure ViewController is notified when the user picks an image.
        imagePickerController.delegate = self
        presentViewController(imagePickerController, animated: true, completion: nil)
    }
    
    @IBAction func takeImage(sender: UIButton) {
        
        if UIImagePickerController.availableCaptureModesForCameraDevice(.Rear) != nil {
            // Create a controller for handling the camera action
            let imageTakerController = UIImagePickerController()
            
            // Set camera options
            imageTakerController.allowsEditing = false
            imageTakerController.sourceType = .Camera
            imageTakerController.cameraCaptureMode = .Photo
            imageTakerController.modalPresentationStyle = .FullScreen
            
            // Indicate that a new photo is being selected.
            newPhoto = true
            
            // Make sure ViewController is notified when the user takes an image.
            imageTakerController.delegate = self
            presentViewController(imageTakerController, animated: true, completion: nil)
        } else {
            noCamera()
        }
        
    }
    
    @IBAction func setDefaultNameText(sender: UIButton) {
        nameTextField.text = "Photo" + " - " + dateTime!
    }

    // MARK: NSCoding
    
    func savePhoto(photo: NewPhoto) {

        do {
            try NSFileManager.defaultManager().createDirectoryAtPath(UserVars.PhotosURL.path!, withIntermediateDirectories: false, attributes: nil)
        } catch let error as NSError {
            print(error.localizedDescription);
        }
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(photo, toFile: photoURL!.path!)
        
        if !isSuccessfulSave {
            print("Failed to save records...")
        }
    }
    
}
