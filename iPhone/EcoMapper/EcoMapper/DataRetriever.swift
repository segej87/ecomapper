//
//  DataRetriever.swift
//  EcoMapper
//
//  Created by Jon on 9/12/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation

open class DataRetriever {
    
    var listString : NSString?
    
    func getListsUsingUUID(_ uuid: String) {
        // Establish a request to the server-side PHP script, and define the method as POST
        let request = NSMutableURLRequest(url: URL(string: UserVars.listScript)!)
        request.httpMethod = "POST"
        
        // Create the POST string with necessary variables, and put in HTTP body
        let postString = "GUID=\(uuid)"
        request.httpBody = postString.data(using: String.Encoding.utf8)
        
        // Create a session with the PHP script, and attempt to login
        let task = URLSession.shared.dataTask(with: request as URLRequest) {
            data, response, error in
            
            // Make sure there are no errors creating the session and that some data is being passed
            guard error == nil && data != nil else {
                print("error=\(error!)")
                return
            }
            
            // Check if HTTP response code is 200 ("OK"). If not, print an error
            if let httpStatus = response as? HTTPURLResponse , httpStatus.statusCode != 200 {
                print("Unexpected http status code: \(httpStatus.statusCode)")
                print("response = \(response!)")
            }
            
            // Get the PHP script's response to the session
            let responseString = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
            
            // Perform rest of login procedure after background server session finishes
            DispatchQueue.main.async {
                
                // For reference, print the response string to the log
                print("Response: \(responseString!)")
                
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
                                        if !UserVars.Tags.keys.contains(i) {
                                            UserVars.Tags[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    case "species":
                                        if !UserVars.Species.keys.contains(i) {
                                            UserVars.Species[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    case "units":
                                        if !UserVars.Units.keys.contains(i) {
                                            UserVars.Units[i] = ["Server" as AnyObject,0 as AnyObject]
                                        }
                                    default:
                                        print("Unexpected key")
                                    }
                                }
                            }
                        }
                    } catch let error as NSError {
                        print(error.localizedDescription)
                    }
                } else {
                }
            }
        }
        task.resume()
    }
}
