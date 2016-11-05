//
//  LoginViewController.swift
//  EcoMapper
//
//  Created by Jon on 6/29/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController, UITextFieldDelegate, UINavigationControllerDelegate {
    
    
    // MARK: Class Variables
    
    /* 
    UI References
     */
    @IBOutlet weak var usernameView: UITextField!
    @IBOutlet weak var passwordView: UITextField!
    @IBOutlet weak var activityView: UIActivityIndicatorView!
    @IBOutlet weak var loginButton: UIButton!
    
    
    // MARK: Initialization
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Set up the login form.
        usernameView.delegate = self
        passwordView.delegate = self
        
        // Set targets to monitor the text fields for changes.
        for t in [usernameView, passwordView] {
            t?.addTarget(self, action: #selector(LoginViewController.textFieldDidChange(_:)), for: UIControlEvents.editingChanged)
        }
        
        // Deactivate the login button when username and password are blank
        loginButton.isEnabled = false
        
        // If a uuid is already available (user is already logged in) go directly to record table view.
        if let savedLogin = loadLogin() {
            UserVars.UUID = savedLogin.uuid
        }
        DispatchQueue.main.async {
            if let uvuuid = UserVars.UUID {
                if UserVars.loadUserVars(uuid: uvuuid) {
                    if Reachability.isConnectedToNetwork() {
                        self.getListsUsingUUID(uvuuid, sender: "saved login")
                    } else {
                        self.performSegue(withIdentifier: "Notebook", sender: "saved login")
                    }
                }
            }
        }
    }
    
    
    // MARK: UITextField Delegates
    
    // Method to allow activation of the login button as the text fields change
    func textFieldDidChange(_ textField: UITextField) {
        // If a username and password have been provided, enable the login button
        if usernameView.text != "" && passwordView.text != "" {
            loginButton.isEnabled = true
        } else {
            loginButton.isEnabled = false
        }
    }
    
    // Hides the keyboard when the text field is returned
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //Hide the keyboard.
        textField.resignFirstResponder()
        
        if usernameView.text != "" && passwordView.text != "" {
            loginButton.isEnabled = true
        } else {
            loginButton.isEnabled = false
        }
        return true
    }
    
    
    // MARK: Navigation
    
    // Actions to perform before segue away from login view controller
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Print login status to log.
        NSLog("Logging in to username \(UserVars.UName!) with \(sender!)")
    }
    
    // When the app returns to the login page, clear key variables
    @IBAction func unwindToLogin(_ sender: UIStoryboardSegue) {
        // Make sure the text fields and login result are blank
        usernameView.text = ""
        passwordView.text = ""
        
        // Clear all of the user variables
        UserVars.clearUserVars()
        
        saveLogin(loginInfo: LoginInfo(uuid: UserVars.UUID))
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
            showAlertDialog(title: "Network Error", message: "Make sure you are connected to the Internet")
        }
    }
    
    
    // MARK: Server Ops
    
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
                NSLog("Login error: \(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? HTTPURLResponse , httpStatus.statusCode != 200 {
                NSLog("Unexpected http status code: \(httpStatus.statusCode)")
                NSLog("Login server response: \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
            
            // Perform rest of login procedure after background server session finishes
            DispatchQueue.main.async {
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let loginSuccess = responseString != nil && !responseString!.contains("Error")
                
                /* If the login attempt was successful, set the UUID User Variable and load lists. If the attempt was unsuccessful, present an alert with the login error.
                 */
                if loginSuccess {
                    // Set the instance property loginString to the server's response
                    UserVars.UUID = responseString?.lowercased
                    
                    if UserVars.loadUserVars(uuid: UserVars.UUID!) {
                        NSLog("Loading user variables")
                    }
                    UserVars.UName = self.usernameView.text
                    
                    self.getListsUsingUUID(UserVars.UUID!, sender: "new login")
                } else {
                    // Stop the activity indicator
                    self.activityView.stopAnimating()
                    
                    /* If the login wasn't successful, check if the response contains the word Error,
                     as configured on the PHP server
                     */
                    var errorString: String?
                    if responseString!.contains("Error") {
                        errorString = responseString!.replacingOccurrences(of: "Error: ", with: "")
                    } else {
                        errorString = "Network Error - check your Internet connection"
                    }
                    
                    // Present the error to the user
                    self.showAlertDialog(title: "Login Error", message: errorString!)
                }
            }
        }) 
        task.resume()
    }
    
    func getListsUsingUUID(_ uuid: String, sender: String) {
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
                NSLog("List server response: \(responseString!)")
                
                // Boolean to check whether the server's response was nil, or whether an error was returned
                let listSuccess = responseString! != "" && !responseString!.contains("Error")
                
                // If the attempt was successful, set the User Variables and segue to the notebook view controller. If the attempt was unsuccessful, present an alert dialog.
                if listSuccess {
                    do {
                        // Encode the response string as data, then parse JSON
                        let responseData = responseString!.data(using: String.Encoding.utf8.rawValue)
                        let responseArray = try JSONSerialization.jsonObject(with: responseData!, options: JSONSerialization.ReadingOptions()) as! [String:AnyObject]
                        
                        // Mesh the server's response with saved user variables
                        UserVars.meshUserVars(array: responseArray)
                        
                        // Write the user variables to the login object and save
                        self.saveLogin(loginInfo: LoginInfo(uuid: UserVars.UUID))
                        
                        // Save the user variables
                        UserVars.saveUserVars()
                        
                        // Stop the activity indicator
                        self.activityView.stopAnimating()
                        
                        // Perform a segue to the notebook view controller
                        self.performSegue(withIdentifier: "Notebook", sender: sender)
                        
                    } catch let error as NSError {
                        NSLog("Login list retrieve parse error: \(error.localizedDescription)")
                    }
                } else {
                    // Stop the activity indicator
                    self.activityView.stopAnimating()
                    
                    // Show the error to the user as an alert controller
                    var errorString: String?
                    if responseString!.contains("Error") {
                        errorString = responseString!.replacingOccurrences(of: "Error: ",with: "")
                    } else {
                        errorString = "Network Error - check your Internet connection"
                    }
                    
                    self.showAlertDialog(title: "Login Error", message: errorString!)
                }
            }
        }) 
        task.resume()
    }
    
    
    // MARK: NSCoding
    
    func saveLogin(loginInfo: LoginInfo) {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(loginInfo, toFile: LoginInfo.ArchiveURL.path)
        
        if !isSuccessfulSave {
            NSLog("Failed to save login info...")
        }
    }
    
    func loadLogin() -> LoginInfo? {
        return NSKeyedUnarchiver.unarchiveObject(withFile: LoginInfo.ArchiveURL.path) as? LoginInfo
    }
    
    
    // MARK: Helper Methods
    
    func showAlertDialog(title: String, message: String) {
        if #available(iOS 9.0, *) {
            let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)
            let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
            alertVC.addAction(okAction)
            self.present(alertVC, animated: true, completion: nil)
        } else {
            let alertVC = UIAlertView(title: title, message: message, delegate: self, cancelButtonTitle: "OK")
            alertVC.show()
        }
    }
}
