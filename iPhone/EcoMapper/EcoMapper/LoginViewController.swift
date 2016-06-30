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
    
    // URL of the authorization script
    let authScript = "http://ecocollector.azurewebsites.net/get_login.php"
    
    // Create an object to store successful login info
    var loginInfo = LoginInfo(guid: "")
    
    // MARK: Initialization
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Style the navigation bar's background color and button colors
        let nav = self.navigationController?.navigationBar
        nav?.barStyle = UIBarStyle.Black
        nav?.backgroundColor = UIColor(red: 0/255 as CGFloat, green: 0/255 as CGFloat, blue: 96/255 as CGFloat, alpha: 1)
        self.navigationController?.navigationBar.tintColor = UIColor.lightGrayColor()
        
        // Set the text field delegate to the class
        usernameView.delegate = self
        passwordView.delegate = self
        
        // Deactivate the login button when username and password are blank
        loginButton.enabled = false
        
        // TODO: Make this operational after adding a way to log out. If a guid is already available (user is already logged in) go directly to record table view.
//        if let savedLogin = loadLogin() {
//            loginInfo = savedLogin
//            UserVars.guid = loginInfo!.guid
//        }
        if UserVars.guid != nil {
            performSegueWithIdentifier("login", sender: nil)
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
    }
    
    // MARK: Actions

    @IBAction func tryLogin(sender: UIButton) {
        // Start the activity indicator
        activityView.startAnimating()
        
        // Try to login
        self.attemptLogin()
    }
    
    // MARK: Helper methods
    
    func attemptLogin() {
        // Get user-entered info from text fields
        let uname = usernameView.text
        let pword = passwordView.text
        
        // TODO: Perform authentication against a server here
        if Reachability.isConnectedToNetwork() {
            // Establish a request to the server-side PHP script, and define the method as POST
            let request = NSMutableURLRequest(URL: NSURL(string: authScript)!)
            request.HTTPMethod = "POST"
            
            // Create the POST string with necessary variables, and put in HTTP body
            let postString = "username=\(uname!)&password=\(pword!)"
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
                    let loginSuccess = self.loginString != nil && self.loginString!.stringByReplacingOccurrencesOfString("Error", withString: "") == self.loginString!
                    
                    // Stop the activity indicator
                    self.activityView.stopAnimating()
                    
                    // If the login attempt was successful, set the structure variable guid for use by other classes and segue to the record table view controller. If the attempt was unsuccessful, present an alert with the login error.
                    if loginSuccess {
                        UserVars.guid = self.loginString?.lowercaseString
                        self.loginInfo!.guid = UserVars.guid
                        self.saveLogin()
                        self.performSegueWithIdentifier("login", sender: nil)
                    } else {
                        let errorString = self.loginString!.stringByReplacingOccurrencesOfString("Error: ",withString: "")
                        let alertVC = UIAlertView(title: "Login Error", message: "\(errorString)", delegate: self, cancelButtonTitle: "OK")
                        alertVC.show()
                    }
                }
            }
            task.resume()
        }
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
