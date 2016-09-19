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
    
    // IB Properties
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var photoImageView: UIImageView!
    @IBOutlet weak var accessTextField: UITextField!
    @IBOutlet weak var notesTextField: UITextView!
    @IBOutlet weak var tagTextField: UITextField!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var gpsAccView: UILabel!
    @IBOutlet weak var accessPickerButton: UIButton!
    @IBOutlet weak var tagPickerButton: UIButton!
    
    // Class Variables
    // The Core Location manager
    let locationManager = CLLocationManager()
    
    // Array to hold selected multi-pick items (tags and access levels)
    var tagArray = [String]()
    var accessArray = [String]()
    
    // A flag indicating whether a new photo is being taken
    var newPhoto = false
    
    // The path to the photo on the device's drive
    var photoURL: URL?
    
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
        styleNavigationBar()
        
        // Add border to text view
        styleTextView()
        
        // Handle text fields' user input through delegate callbacks.
        nameTextField.delegate = self
        accessTextField.delegate = self
        tagTextField.delegate = self
        
        // Handle the notes field's user input through delegate callbacks.
        notesTextField.delegate = self
        
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
        
        // Enable the Save button only if the required text fields have a valid name.
        checkValidName()
    }
    
    // MARK: Camera alert
    
    func noCamera() {
        if #available(iOS 8.0, *) {
            let alertVC = UIAlertController(title: "No Camera", message: "Sorry, this device doesn't have a camera", preferredStyle: .alert)
            let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
            alertVC.addAction(okAction)
            present(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: "No Camera", message: "Sorry, this device doesn't have a camera", delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
    
    // MARK: UIImagePickerControllerDelegate
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        // Dismiss the picker if the user canceled.
        dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        // The info dictionary contains multiple representations of the image, and this uses the original.
        let selectedImage = info[UIImagePickerControllerOriginalImage] as! UIImage
        
        // Save the image if it was taken from the camera.
        if newPhoto {
            
            // Set the name of the photo
            medOutName = "Photo_\(dateTime!.replacingOccurrences(of: "-", with: "").replacingOccurrences(of: ":", with: "").replacingOccurrences(of: " ", with: "_")).jpg"
            
            // Set path of photo to be saved
            photoURL = UserVars.PhotosURL.appendingPathComponent(medOutName!)
            
            // Create an NSCoded photo object
            let outPhoto = NewPhoto(photo: selectedImage)
            
            // Save the photo to the photos directory
            savePhoto(outPhoto!)
            
        } else {
            
            // Get the url of the selected asset and set it to the instance variable
            let imageURL = info[UIImagePickerControllerReferenceURL] as! URL
            
            photoURL = imageURL
            
            // Set the name of the photo
            medOutName = "Photo_\(dateTime!.replacingOccurrences(of: "-", with: "").replacingOccurrences(of: ":", with: "").replacingOccurrences(of: " ", with: "_")).\(imageURL.absoluteString.substring(from: imageURL.absoluteString.range(of: "ext=")!.upperBound))"
        }
        
        // Set the aspect ratio of the image in the view
        photoImageView.contentMode = .scaleAspectFit
        
        // Set photoImageView to display the selected image.
        photoImageView.image = selectedImage
        
        // Dismiss the picker.
        dismiss(animated: true, completion: nil)
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
        let text3 = accessTextField.text ?? ""
        let photo1 = photoImageView.image ?? nil
        let loc1 = userLoc ?? nil
        saveButton.isEnabled = !(text1.isEmpty || text2.isEmpty || text3.isEmpty || photo1 == nil || loc1 == nil)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        checkValidName()
    }
    
    // MARK: UITextViewDelegate
    
    func textViewDidChange(_ textView: UITextView) {
        
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
            // TODO: Allow user to choose whether to use photo location or current location
            
            let name = nameTextField.text ?? ""
            let urlOut = photoURL!.absoluteString
            
            let props = ["name": name as AnyObject, "tags": tagArray as AnyObject, "datatype": "photo" as AnyObject, "datetime": dateTime! as AnyObject, "access": accessArray as AnyObject, "accuracy": gpsAcc as AnyObject, "text": notesTextField.text as AnyObject, "filepath": urlOut as AnyObject] as [String:AnyObject]
            
            // Set the record to be passed to RecordTableViewController after the unwind segue.
            record = Record(coords: userLoc!, photo: photoImageView.image, props: props)
            
            // Set the media reference to be passed to RecordTableViewController after the unwind segue.
            media = Media(name: medOutName!, path: photoURL)
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
        
        // If the add tags button was pressed, present the item picker with a tags item type
        if sender is UIButton && tagPickerButton === (sender as! UIButton) {
            let secondVC = segue.destination as! ListPickerViewController
            secondVC.itemType = "tags"
            
            // Send previous tags to ListPicker
            if tagTextField.text != "" {
                for t in tagArray {
                    secondVC.selectedItems.append(t)
                }
            }
        }
    }
    
    @IBAction func unwindFromListPicker(_ segue: UIStoryboardSegue) {
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
    
    @IBAction func selectImage(_ sender: UITapGestureRecognizer) {
        
        // Hide the keyboard.
        nameTextField.resignFirstResponder()
        
        // UIImagePickerController is a view controller that lets a user pick media from their photo library.
        let imagePickerController = UIImagePickerController()
        
        // Allow photos to be picked from existing photos.
        imagePickerController.sourceType = .photoLibrary
        
        // Indicate that a previously existing photo is being selected.
        newPhoto = false
        
        // Make sure ViewController is notified when the user picks an image.
        imagePickerController.delegate = self
        present(imagePickerController, animated: true, completion: nil)
    }
    
    @IBAction func takeImage(_ sender: UIButton) {
        
        if UIImagePickerController.availableCaptureModes(for: .rear) != nil {
            // Create a controller for handling the camera action
            let imageTakerController = UIImagePickerController()
            
            // Set camera options
            imageTakerController.allowsEditing = false
            imageTakerController.sourceType = .camera
            imageTakerController.cameraCaptureMode = .photo
            imageTakerController.modalPresentationStyle = .fullScreen
            
            // Indicate that a new photo is being selected.
            newPhoto = true
            
            // Make sure ViewController is notified when the user takes an image.
            imageTakerController.delegate = self
            present(imageTakerController, animated: true, completion: nil)
        } else {
            noCamera()
        }
        
    }
    
    @IBAction func setDefaultNameText(_ sender: UIButton) {
        nameTextField.text = "Photo" + " - " + dateTime!
    }

    // MARK: NSCoding
    
    func savePhoto(_ photo: NewPhoto) {

        do {
            try FileManager.default.createDirectory(atPath: UserVars.PhotosURL.path, withIntermediateDirectories: false, attributes: nil)
        } catch let error as NSError {
            print(error.localizedDescription);
        }
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(photo, toFile: photoURL!.path)
        
        if !isSuccessfulSave {
            print("Failed to save records...")
        }
    }
    
    // MARK: Helper methods
    func styleNavigationBar() {
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGray
    }
    
    func styleTextView() {
        self.notesTextField.layer.borderWidth = 0.5
        self.notesTextField.layer.cornerRadius = 10
        self.notesTextField.layer.borderColor = UIColor.init(red: 200/255.0, green: 199/255.0, blue: 204/255.0, alpha: 1.0).cgColor
    }
    
    func setupEditingMode(record: Record) {
        navigationItem.title = "Editing Photo"
        nameTextField.text = record.props["name"] as? String
        accessTextField.text = (record.props["access"] as? [String])?.joined(separator: ", ")
        accessArray = record.props["access"] as! [String]
        photoImageView.image = record.photo
        notesTextField.text = record.props["text"] as? String
        tagTextField.text = (record.props["tags"] as? [String])?.joined(separator: ", ")
        tagArray = record.props["tags"] as! [String]
        dateTime = record.props["datetime"] as? String
        userLoc = record.coords
        gpsAccView.isHidden = true
    }
}
