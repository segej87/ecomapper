//
//  StartScreenViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/8/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class StartScreenViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        checkAndSegue()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    // MARK: - Navigation
    
    // Actions to perform before segue away from login view controller
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Print login status to log.
        if sender is String && sender as! String == "saved login" {
            NSLog("Logging in to username \(UserVars.UName!) with \(sender!)")
        } else {
            NSLog("Moving to login")
        }
    }
    
    func checkAndSegue() {
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
            } else {
                self.performSegue(withIdentifier: "Login", sender: self)
            }
        }
    }
    
    // MARK: Helper Methods
    
    func loadLogin() -> LoginInfo? {
        return NSKeyedUnarchiver.unarchiveObject(withFile: LoginInfo.ArchiveURL.path) as? LoginInfo
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
                        
                        // Save the user variables
                        UserVars.saveUserVars()
                        
                        // Perform a segue to the notebook view controller
                        self.performSegue(withIdentifier: "Notebook", sender: sender)
                        
                    } catch let error as NSError {
                        NSLog("Login list retrieve parse error: \(error.localizedDescription)")
                    }
                } else {
                    
                    // Show the error to the user as an alert controller
                    var errorString: String?
                    if responseString!.contains("Error") {
                        errorString = responseString!.replacingOccurrences(of: "Error: ",with: "")
                    } else {
                        errorString = "Network Error - check your Internet connection"
                    }
                }
            }
        })
        task.resume()
    }

}
