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
    static var uuid: String? {
        didSet {
            RecordsURL = DocumentsDirectory.URLByAppendingPathComponent("Records-\(uuid)")
            MediasURL = DocumentsDirectory.URLByAppendingPathComponent("Media-\(uuid)")
        }
    }
    
    static let DocumentsDirectory = NSFileManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first!
    
    static var RecordsURL: NSURL?
    
    static var MediasURL: NSURL?
    
    static let PhotosURL = DocumentsDirectory.URLByAppendingPathComponent("Photos", isDirectory: true)
    
    static var AccessLevels = ["public", "private"]
    
    static var Tags = [String]()
    
    static var Species = [String]()
    
    // URL of the authorization script
    static let authScript = "http://ecocollector.azurewebsites.net/get_login.php"
    
    // URL of the institution-getting script
    static let institScript = "http://ecocollector.azurewebsites.net/get_institutions.php"
    
    //URL of the list-getting script
    static let listScript = "http://ecocollector.azurewebsites.net/get_lists.php"
}