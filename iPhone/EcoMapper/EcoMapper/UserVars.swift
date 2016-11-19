//
//  UserVars.swift
//  EcoMapper
//
//  Created by Jon on 6/30/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation
import UIKit

struct UserVars {
    
    /*
     The user ID of the current user, which sets record and media save paths
     */
    static var UUID: String? {
        didSet {
            if let uuid = UUID {
                UserVarsURL = DocumentsDirectory.appendingPathComponent("UserVars-\(uuid)")
                RecordsURL = DocumentsDirectory.appendingPathComponent("Records-\(uuid)")
                MediasURL = DocumentsDirectory.appendingPathComponent("Media-\(uuid)")
            } else {
                UserVarsURL = nil
                RecordsURL = nil
                MediasURL = nil
            }
        }
    }
    
    /*
     The username of the current user
     */
    static var UName: String?
    
    /*
     Filepath to save these user variables
     */
    static var UserVarsURL: URL?
    
    /*
     Filepath to save records
     */
    static var RecordsURL: URL?
    
    /*
     Filepath to save medias
     */
    static var MediasURL: URL?
    
    /*
     The documents directory for the application
     */
    static let DocumentsDirectory = FileManager()
        .urls(for: .documentDirectory, in: .userDomainMask).first!
    
    /*
     Filepath to save photos
     */
    static let PhotosURL = DocumentsDirectory.appendingPathComponent("Photos", isDirectory: true)
    
    /*
     An array of access levels, with the built-in public and private options
     */
    static var AccessLevels = ["Public", "Private"]
    
    /*
     New key-value pairs object for tags
     */
    static var Tags = [String:[AnyObject]]()
    
    /*
     New key-value pairs object for species
     */
    static var Species = [String:[AnyObject]]()
    
    /*
     New key-value pairs object for Units
     */
    static var Units = [String:[AnyObject]]()
    
    /*
     Defaults
     */
    static var AccessDefaults = [String]()
    static var TagsDefaults = [String]()
    static var SpecDefault: String?
    static var UnitsDefault: String?
    
    /*
     Location settings
     */
//    static let maxUpdateTime = 1
//    static let minGPSAccuracy = 50 as Double
//    static let minGPSStability = 50 as Double
    
    /*
     Static string server URLs
     */
    
    // URL of the authorization script
    static let authScript = "http://ecocollector.azurewebsites.net/get_login.php"
    
    //URL of the list-getting script
    static let listScript = "http://ecocollector.azurewebsites.net/get_lists.php"
    
    // URL to PHP script for uploading new records via POST.
    static let recordAddScript = "http://ecocollector.azurewebsites.net/add_records.php"
    
    // URL to blob storage Account.
    static let blobRootURLString = "https://ecomapper.blob.core.windows.net/"
    
    // MARK: General functions
    
    static func saveLogin(loginInfo: LoginInfo) {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(loginInfo, toFile: LoginInfo.ArchiveURL.path)
        
        if !isSuccessfulSave {
            NSLog("Failed to save login info...")
        }
    }
    
    static func meshUserVars(array: [String:AnyObject]) {
        // Initialize an array of all keys to read from the server response
        let keys = ["institutions","tags","species","units"]
        
        // Read the arrays corresponding to the keys, and write to user variables
        for k in keys {
            let kArray = array[k] as! [String]
            
            if !(kArray.count == 1 && kArray[0].contains("Warning:")) {
                for i in kArray {
                    switch k {
                    case "institutions":
                        if !AccessLevels.contains(i) {
                            AccessLevels.append(i)
                        }
                    case "tags":
                        if !Tags.keys.contains(i) || (Tags.keys.contains(i) && Tags[i]![0] as! String == "Local") {
                            Tags[i] = ["Server" as AnyObject,0 as AnyObject]
                        }
                    case "species":
                        if !Species.keys.contains(i) || (Species.keys.contains(i) && Species[i]![0] as! String == "Local") {
                            Species[i] = ["Server" as AnyObject,0 as AnyObject]
                        }
                    case "units":
                        if !Units.keys.contains(i) || (Units.keys.contains(i) && Units[i]![0] as! String == "Local") {
                            Units[i] = ["Server" as AnyObject,0 as AnyObject]
                        }
                    default:
                        print("Login list retrieval error: unexpected key")
                    }
                }
                
                //TODO: deal with any items that are no longer on the server.
            }
        }
    }
    
    static func saveUserVars() {
        let userVars = UserVarsSaveFile(userName: UName, accessLevels: AccessLevels, tags: Tags, species: Species, units: Units, accessDefaults: AccessDefaults, tagDefaults: TagsDefaults, speciesDefault: SpecDefault, unitsDefault: UnitsDefault)
        
        NSLog("Attempting to save user variables to \(UserVarsURL!.path)")
        
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(userVars, toFile: UserVarsURL!.path)
        
        if !isSuccessfulSave {
            NSLog("Failed to save user variables...")
        }
    }
    
    static func loadUserVars(uuid: String) -> Bool {
        if let path = UserVarsURL?.path {
            if let loadedUserVars = NSKeyedUnarchiver.unarchiveObject(withFile: path) as? UserVarsSaveFile {
                NSLog("Loading user variables")
                UName = loadedUserVars.userName
                AccessLevels = loadedUserVars.accessLevels!
                Species = loadedUserVars.species!
                Tags = loadedUserVars.tags!
                Units = loadedUserVars.units!
                AccessDefaults = loadedUserVars.accessDefaults
                TagsDefaults = loadedUserVars.tagDefaults
                SpecDefault = loadedUserVars.speciesDefault
                UnitsDefault = loadedUserVars.unitsDefault
                
                return true;
            }
        }
        
        return false;
    }
    
    static func clearUserVars() {
        // Clear all of the user variables
        UUID = nil
        UName = nil
        AccessLevels = ["Public", "Private"]
        Tags = [String:[AnyObject]]()
        Species = [String:[AnyObject]]()
        Units = [String:[AnyObject]]()
        AccessDefaults = [String]()
        TagsDefaults = [String]()
        SpecDefault = nil
        UnitsDefault = nil
    }
    
    static func handleDeletedRecord(record: Record) {
        // Modify the UserVar lists associated with the record
        let prevArray = record.props["tags"] as? [String]
        for p in prevArray! {
            if var pTag = UserVars.Tags[p] {
                if pTag[0] as! String == "Local" {
                    pTag[1] = ((pTag[1] as! Int - 1) as AnyObject)
                    if pTag[1] as! Int == 0 {
                        UserVars.Tags.removeValue(forKey: p)
                    } else {
                        UserVars.Tags[p] = pTag
                    }
                }
            }
        }
        
        if record.props["datatype"] as! String == "meas" {
            let specArray = record.props["species"]?.components(separatedBy: ", ")
            for s in specArray! {
                if var sTag = UserVars.Species[s] {
                    if sTag[0] as! String == "Local" {
                        sTag[1] = ((sTag[1] as! Int - 1) as AnyObject)
                        if sTag[1] as! Int == 0 {
                            UserVars.Species.removeValue(forKey: s)
                        } else {
                            UserVars.Species[s] = sTag
                        }
                    }
                }
            }
            
            let unitArray = record.props["units"]?.components(separatedBy: ", ")
            for u in unitArray! {
                if var uTag = UserVars.Units[u] {
                    if uTag[0] as! String == "Local" {
                        uTag[1] = ((uTag[1] as! Int - 1) as AnyObject)
                        if uTag[1] as! Int == 0 {
                            UserVars.Units.removeValue(forKey: u)
                        } else {
                            UserVars.Units[u] = uTag
                        }
                    }
                }
            }
        }
    }
}
