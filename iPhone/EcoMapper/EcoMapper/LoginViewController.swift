//
//  LoginViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/29/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController, UITextFieldDelegate, UINavigationControllerDelegate {
    
    // MARK: Properties
    // IB properties
    @IBOutlet weak var usernameView: UITextField!
    @IBOutlet weak var passwordView: UITextField!
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var activityView: UIActivityIndicatorView!
    
    // Class variables
    // Will store the UUID to pass in to the record table view
    var serverString: NSString?
    
    // MARK: Initialization
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Set the text field delegate to the class
        usernameView.delegate = self
        passwordView.delegate = self
        
        // Set a target to monitor the text fields for changes
        for t in [usernameView, passwordView] {
            t?.addTarget(self, action: #selector(LoginViewController.textFieldDidChange(_:)), for: UIControlEvents.editingChanged)
        }
        
        // Deactivate the login button when username and password are blank
        loginButton.isEnabled = false
        
        // If a uuid is already available (user is already logged in) go directly to record table view.
        if let savedLogin = loadLogin() {
            UserVars.uuid = savedLogin.uuid
            UserVars.AccessLevels = savedLogin.accessLevels!
            UserVars.Tags = savedLogin.tags!
            UserVars.Species = savedLogin.species!
            UserVars.Units = savedLogin.units!
        }
        DispatchQueue.main.async {
            if let uvuuid = UserVars.uuid {
                if uvuuid != "" {
                    self.performSegue(withIdentifier: "Login", sender: "Saved Login")
                }
            }
        }
    }
    
    // MARK: UITextFieldDelegate
    // Method to allow activation of the login button as the text fields change
    func textFieldDidChange(_ textField: UITextField) {
        // If a username and password have been provided, enable the login button
        if usernameView.text != "" && passwordView.text != "" {
            loginButton.isEnabled = true
        }
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    // MARK: Navigation
    // Actions to perform before segue away from login view controller
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Print login status to log.
        print("Writing records to \(UserVars.RecordsURL!)")
        print("Writing media list to \(UserVars.MediasURL!)")
        print("Saving photos to \(UserVars.PhotosURL)")
        print("Logging in with \(sender!)")
    }
    
    // When the app returns to the login page, clear key variables
    @IBAction func unwindToLogin(_ sender: UIStoryboardSegue) {
        // Make sure the text fields and login result are blank
        usernameView.text = ""
        passwordView.text = ""
        serverString = ""
        
        // Clear all of the user variables
        UserVars.uuid = self.serverString?.lowercased
        UserVars.AccessLevels = ["Public", "Private"]
        UserVars.Tags = [String:[AnyObject]]()
        UserVars.Species = [String:[AnyObject]]()
        UserVars.Units = [String:[AnyObject]]()
        
        saveLogin(loginInfo: LoginInfo(uuid: UserVars.uuid, accessLevels: UserVars.AccessLevels, tags: UserVars.Tags, species: UserVars.Species, units: UserVars.Units)!)
    }
    
    // MARK: Actions

    @IBAction func tryLogin(_ sender: UIButton) {
        // Start the activity indicator
        activityView.startAnimating()
        
        // Try to login
        self.attemptLogin()
    }
    
    // MARK: Login authentication
    
    func attemptLogin() {
        // Get user-entered info from text fields
        let uname = usernameView.text
        let pword = passwordView.text
        
        // Check if the device is connected. If not, stop and present an error
        if Reachability.isConnectedToNetwork() {
            checkCredentialsAndGetUUID(uname!, pword: pword!)
        } else {
            // Stop the activity indicator
            self.activityView.stopAnimating()
            
            // Present an error message indicating that there is no connection
            if #available(iOS 9.0, *) {
                let alertVC = UIAlertController(title: "Login Error", message: "No internet - please check your connection", preferredStyle: .alert)
                let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                alertVC.addAction(okAction)
                self.present(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "Login Error", message: "No internet - please check your connection", delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        }
    }
    
    // MARK: Server communication helper methods
    
    func checkCredentialsAndGetUUID(_ uname: String, pword: String) {
        // Initialize a request to the server-side PHP script, and define the method as POST
        let request = NSMutableURLRequest(url: URL(string: UserVars.authScript)!)
        request.httpMethod = "POST"
        
        // Create the POST string with necessary variables, and put in HTTP body
        let postString = "username=\(uname)&password=\(pword)"
        request.httpBody = postString.data(using: String.Encoding.utf8)
        
        // Create a session with the PHP script, and attempt to login
        let task = URLSession.shared.dataTask(with: request as URLRequest, completionHandler: { data, response, error in
            
            // Make sure there are no errors creating the session and that some data is being passed
            guard error == nil && data != nil else {
                print("Login error: \(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? HTTPURLResponse , httpStatus.statusCode != 200 {
                print("Unexpected http status code: \(httpStatus.statusCode)")
                print("Login server response: \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
            
            // Perform rest of login procedure after background server session finishes
            DispatchQueue.main.async {
                // For reference, print the response string to the log
                print("Login server response: \(responseString!)")
                
                // Set the instance property loginString to the server's response
                self.serverString = responseString
                
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let loginSuccess = responseString != nil && responseString!.length == 36
                
                /* If the login attempt was successful, set the structure variable uuid and segue to the record table view controller. If the attempt was unsuccessful, present an alert with the login error.
                 */
                if loginSuccess {
                    UserVars.uuid = self.serverString?.lowercased
                    
                    self.getListsUsingUUID(UserVars.uuid!)
                } else {
                    // Stop the activity indicator
                    self.activityView.stopAnimating()
                    
                    /* If the login wasn't successful, check if the response contains the word Error,
                     as configured on the PHP server
                     */
                    var errorString: String?
                    if self.serverString!.replacingOccurrences(of: "Error", with: "") != self.serverString! as String {
                        errorString = self.serverString!.replacingOccurrences(of: "Error: ",with: "")
                    } else {
                        errorString = "Can't connect to the server - please check your internet connection"
                    }
                    
                    // Present the error to the user
                    if #available(iOS 9.0, *) {
                        let alertVC = UIAlertController(title: "Login Error", message: "\(errorString!)", preferredStyle: .alert)
                        let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                        alertVC.addAction(okAction)
                        self.present(alertVC, animated: true, completion: nil)
                    } else {
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString!)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
        }) 
        task.resume()
    }
    
    func getListsUsingUUID(_ uuid: String) {
        // Establish a request to the server-side PHP script, and define the method as POST
        let request = NSMutableURLRequest(url: URL(string: UserVars.listScript)!)
        request.httpMethod = "POST"
        
        // Create the POST string with necessary variables, and put in HTTP body
        let postString = "GUID=\(uuid)"
        request.httpBody = postString.data(using: String.Encoding.utf8)
        
        // Create a session with the PHP script, and attempt to login
        let task = URLSession.shared.dataTask(with: request as URLRequest, completionHandler: { data, response, error in
            
            // Make sure there are no errors creating the session and that some data is being passed
            guard error == nil && data != nil else {
                print("Login list retrieve error: \(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? HTTPURLResponse , httpStatus.statusCode != 200 {
                print("Unexpected http status code: \(httpStatus.statusCode)")
                print("Login list response: \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
            
            // Perform rest of login procedure after background server session finishes
            DispatchQueue.main.async {
                
                // For reference, print the response string to the log
                print("Login list response: \(responseString!)")
                
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let listSuccess = responseString! != ""
                
                // If the login attempt was successful, set the structure variable uuid for use by other classes and segue to the record table view controller. If the attempt was unsuccessful, present an alert with the login error.
                if listSuccess {
                    do {
                        // Encode the response string as data, then parse JSON
                        let responseData = responseString!.data(using: String.Encoding.utf8.rawValue)
                        let responseArray = try JSONSerialization.jsonObject(with: responseData!, options: JSONSerialization.ReadingOptions()) as! [String:AnyObject]
                        
                        // Initialize an array of all keys to read from the server response
                        let keys = ["institutions","tags","species","units"]
                        
                        // Read the arrays corresponding to the keys, and write to user variables
                        for k in keys {
                            let kArray = responseArray[k] as! [String]
                            
                            if kArray.count == 1 && kArray[0].contains("Error:") {
                                
                            } else {
                                for i in kArray {
                                    switch k {
                                    case "institutions":
                                        if !UserVars.AccessLevels.contains(i) {
                                            UserVars.AccessLevels.append(i)
                                        }
                                    case "tags":
                                        if !UserVars.Tags.keys.contains(i) || (UserVars.Tags.keys.contains(i) && UserVars.Tags[i]![0] as! String == "Local") {
                                            UserVars.Tags[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    case "species":
                                        if !UserVars.Species.keys.contains(i) || (UserVars.Species.keys.contains(i) && UserVars.Species[i]![0] as! String == "Local") {
                                            UserVars.Species[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    case "units":
                                        if !UserVars.Units.keys.contains(i) || (UserVars.Units.keys.contains(i) && UserVars.Units[i]![0] as! String == "Local") {
                                            UserVars.Units[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    default:
                                        print("Login list retrieval error: unexpected key")
                                    }
                                }
                            }
                        }
                        
                        // Write the user variables to the login object and save
                        self.saveLogin(loginInfo: LoginInfo(uuid: UserVars.uuid, accessLevels: UserVars.AccessLevels, tags: UserVars.Tags, species: UserVars.Species, units: UserVars.Units)!)
                        
                        // Stop the activity indicator
                        self.activityView.stopAnimating()
                        
                        // Perform a segue to the record table view controller
                        self.performSegue(withIdentifier: "Login", sender: "New Login")
                        
                    } catch let error as NSError {
                        print("Login list retrieve parse error: \(error.localizedDescription)")
                    }
                } else {
                    // Stop the activity indicator
                    self.activityView.stopAnimating()
                    
                    // Show the error to the user as an alert controller
                    var errorString: String?
                    if self.serverString!.replacingOccurrences(of: "Error", with: "") != self.serverString! as String {
                        errorString = self.serverString!.replacingOccurrences(of: "Error: ",with: "")
                    } else {
                        errorString = "Can't connect to the server - please check your internet connection"
                    }
                    
                    // Present an alert to the user
                    if #available(iOS 9.0, *) {
                        let alertVC = UIAlertController(title: "Login Error", message: "\(errorString!)", preferredStyle: .alert)
                        let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                        alertVC.addAction(okAction)
                        self.present(alertVC, animated: true, completion: nil)
                    } else {
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString!)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
        }) 
        task.resume()
    }
    
    // MARK: NSCoding
    
    func saveLogin(loginInfo: LoginInfo) {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(loginInfo, toFile: LoginInfo.ArchiveURL.path)
        
        if !isSuccessfulSave {
            print("Failed to save login info...")
        }
    }
    
    func loadLogin() -> LoginInfo? {
        return NSKeyedUnarchiver.unarchiveObject(withFile: LoginInfo.ArchiveURL.path) as? LoginInfo
    }
}
