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
    static var AccessDefaults: [String]?
    static var TagsDefaults: [String]?
    static var SpecDefault: String?
    static var UnitsDefault: String?
    
    /*
     Location settings
     */
    static let maxUpdateTime = 1
    static let minGPSAccuracy = 50
    static let minGPSStability = 50
    
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
}
