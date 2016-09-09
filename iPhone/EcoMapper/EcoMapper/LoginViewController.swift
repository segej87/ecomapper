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
    
    @IBOutlet weak var usernameView: UITextField!
    @IBOutlet weak var passwordView: UITextField!
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var activityView: UIActivityIndicatorView!
    
    // Will store the UUID to pass in to the record table view
    var loginString: NSString?
    
    // Will store the institutions related to the user
    var accessLevels: [String]?
    
    // Create an object to store successful login info
    var loginInfo = LoginInfo(uuid: "", accessLevels: nil, tags: nil, species: nil)
    
    // MARK: Initialization
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Set the text field delegate to the class
        usernameView.delegate = self
        passwordView.delegate = self
        
        // Deactivate the login button when username and password are blank
        loginButton.enabled = false
        
        // If a uuid is already available (user is already logged in) go directly to record table view.
        if let savedLogin = loadLogin() {
            loginInfo = savedLogin
            UserVars.uuid = loginInfo!.uuid
            UserVars.AccessLevels = loginInfo!.accessLevels!
            UserVars.Tags = loginInfo!.tags!
            UserVars.Species = loginInfo!.species!
        }
        dispatch_async(dispatch_get_main_queue()) {
            if let uvuuid = UserVars.uuid {
                if uvuuid != "" {
                    self.performSegueWithIdentifier("Login", sender: "Saved Login")
                }
            }
        }
    }
    
    // MARK: UITextFieldDelegate
    
    func textFieldDidBeginEditing(textField: UITextField) {
        
    }
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        //Hide the keyboard.
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
        if usernameView.text != "" && passwordView.text != "" {
            loginButton.enabled = true
        }
    }
    
    // MARK: Navigation
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Perform any actions necessary for segue
        print("Logging in with \(sender!)")
    }
    
    @IBAction func unwindToLogin(sender: UIStoryboardSegue) {
        usernameView.text = ""
        passwordView.text = ""
        loginString = ""
        accessLevels = ["public", "private"]
        UserVars.uuid = self.loginString?.lowercaseString
        UserVars.AccessLevels = self.accessLevels!
        UserVars.Tags = [String]()
        UserVars.Species = [String]()
        self.loginInfo!.uuid = UserVars.uuid
        self.loginInfo!.accessLevels = UserVars.AccessLevels
        self.loginInfo!.tags = [String]()
        self.loginInfo!.species = [String]()
        saveLogin()
    }
    
    // MARK: Actions

    @IBAction func tryLogin(sender: UIButton) {
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
        
        // TODO: Perform authentication against a server here
        if Reachability.isConnectedToNetwork() {
            checkCredentialsAndGetUUID(uname!, pword: pword!)
        } else {
            // Stop the activity indicator
            self.activityView.stopAnimating()
            
            if #available(iOS 9.0, *) {
                let alertVC = UIAlertController(title: "Login Error", message: "No internet - please check your connection", preferredStyle: .Alert)
                let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                alertVC.addAction(okAction)
                self.presentViewController(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "Login Error", message: "No internet - please check your connection", delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        }
    }
    
    // MARK: Helper methods
    
    func checkCredentialsAndGetUUID(uname: String, pword: String) {
        // Establish a request to the server-side PHP script, and define the method as POST
        let request = NSMutableURLRequest(URL: NSURL(string: UserVars.authScript)!)
        request.HTTPMethod = "POST"
        
        // Create the POST string with necessary variables, and put in HTTP body
        let postString = "username=\(uname)&password=\(pword)"
        request.HTTPBody = postString.dataUsingEncoding(NSUTF8StringEncoding)
        
        // Create a session with the PHP script, and attempt to login
        let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
            
            // Make sure there are no errors creating the session and that some data is being passed
            guard error == nil && data != nil else {
                print("error=\(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {
                print("Unexpected http status code: \(httpStatus.statusCode)")
                print("response = \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
            
            // Perform rest of login procedure after background server session finishes
            dispatch_async(dispatch_get_main_queue()) {
                
                // For reference, print the response string to the log
                print("Response: \(responseString!)")
                
                // Set the instance property loginString to the server's response
                self.loginString = responseString
                
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let loginSuccess = responseString != nil && responseString!.length == 36
                
                // If the login attempt was successful, set the structure variable uuid for use by other classes and segue to the record table view controller. If the attempt was unsuccessful, present an alert with the login error.
                if loginSuccess {
                    UserVars.uuid = self.loginString?.lowercaseString
                    self.loginInfo!.uuid = UserVars.uuid
                    //self.saveLogin()
                    
                    self.getListsUsingUUID(UserVars.uuid!)
                    
                } else {
                    
                    // Stop the activity indicator
                    self.activityView.stopAnimating()
                    
                    var errorString: String?
                    if self.loginString!.stringByReplacingOccurrencesOfString("Error", withString: "") != self.loginString! {
                        errorString = self.loginString!.stringByReplacingOccurrencesOfString("Error: ",withString: "")
                    } else {
                        errorString = "Can't connect to the server - please check your internet connection"
                    }
                    
                    if #available(iOS 9.0, *) {
                        let alertVC = UIAlertController(title: "Login Error", message: "\(errorString!)", preferredStyle: .Alert)
                        let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                        alertVC.addAction(okAction)
                        self.presentViewController(alertVC, animated: true, completion: nil)
                    } else {
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString!)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
        }
        task.resume()
    }
    
    func getListsUsingUUID(uuid: String) {
        // Establish a request to the server-side PHP script, and define the method as POST
        let request = NSMutableURLRequest(URL: NSURL(string: UserVars.listScript)!)
        request.HTTPMethod = "POST"
        
        // Create the POST string with necessary variables, and put in HTTP body
        let postString = "GUID=\(uuid)"
        request.HTTPBody = postString.dataUsingEncoding(NSUTF8StringEncoding)
        
        // Create a session with the PHP script, and attempt to login
        let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
            
            // Make sure there are no errors creating the session and that some data is being passed
            guard error == nil && data != nil else {
                print("error=\(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {
                print("Unexpected http status code: \(httpStatus.statusCode)")
                print("response = \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
            
            // Perform rest of login procedure after background server session finishes
            dispatch_async(dispatch_get_main_queue()) {
                
                // For reference, print the response string to the log
                print("Response: \(responseString!)")
                
                // Set the instance property loginString to the server's response
                self.loginString = responseString
                
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let institSuccess = responseString! != ""
                
                // Stop the activity indicator
                self.activityView.stopAnimating()
                
                // If the login attempt was successful, set the structure variable uuid for use by other classes and segue to the record table view controller. If the attempt was unsuccessful, present an alert with the login error.
                if institSuccess {
                    do {
                        let responseData = responseString!.dataUsingEncoding(NSUTF8StringEncoding)
                        
                        let responseArray = try NSJSONSerialization.JSONObjectWithData(responseData!, options: NSJSONReadingOptions()) as! [String:AnyObject]
                        
                        let institArray = responseArray["institutions"] as! [String]
                        
                        print(institArray.joinWithSeparator(","))
                        
                        if institArray.count == 1 && institArray[0].containsString("Error:") {
                        } else {
                            for i in institArray {
                                if !UserVars.AccessLevels.contains(i) {
                                    UserVars.AccessLevels.append(i)
                                }
                            }
                        }
                        
                        self.loginInfo?.accessLevels = UserVars.AccessLevels
                        
                        let tagsArray = responseArray["tags"] as! [String]
                        
                        if tagsArray.count == 1 && tagsArray[0].containsString("Error:") {
                            print("Error getting tag array")
                        } else {
                            for t in tagsArray {
                                if !UserVars.Tags.contains(t) {
                                    UserVars.Tags.append(t)
                                }
                            }
                        }
                        
                        self.loginInfo?.tags = UserVars.Tags
                        
                        let speciesArray = responseArray["species"] as! [String]
                        
                        if speciesArray.count == 1 && speciesArray[0].containsString("Error:") {
                            
                        } else {
                            for s in speciesArray {
                                if !UserVars.Species.contains(s) {
                                    UserVars.Species.append(s)
                                }
                            }
                        }
                        
                        self.loginInfo?.species = UserVars.Species
                        
                        self.saveLogin()
                        
                        self.performSegueWithIdentifier("Login", sender: "New Login")
                        
                    } catch let error as NSError {
                        print(error.localizedDescription)
                    }
                } else {
                    
                    // Show the error to the user as an alert controller
                    var errorString: String?
                    if self.loginString!.stringByReplacingOccurrencesOfString("Error", withString: "") != self.loginString! {
                        errorString = self.loginString!.stringByReplacingOccurrencesOfString("Error: ",withString: "")
                    } else {
                        errorString = "Can't connect to the server - please check your internet connection"
                    }
                    
                    if #available(iOS 9.0, *) {
                        let alertVC = UIAlertController(title: "Login Error", message: "\(errorString!)", preferredStyle: .Alert)
                        let okAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                        alertVC.addAction(okAction)
                        self.presentViewController(alertVC, animated: true, completion: nil)
                    } else {
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString!)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
        }
        task.resume()
    }
    
    func saveLogin() {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(loginInfo!, toFile: LoginInfo.ArchiveURL.path!)
        
        if !isSuccessfulSave {
            print("Failed to save login info...")
        }
    }
    
    func loadLogin() -> LoginInfo? {
        return NSKeyedUnarchiver.unarchiveObjectWithFile(LoginInfo.ArchiveURL.path!) as? LoginInfo
    }
    
}