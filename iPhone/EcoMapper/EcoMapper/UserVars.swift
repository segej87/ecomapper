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
            RecordsURL = DocumentsDirectory.appendingPathComponent("Records-\(uuid!)")
            MediasURL = DocumentsDirectory.appendingPathComponent("Media-\(uuid!)")
        }
    }
    
    static let DocumentsDirectory = FileManager().urls(for: .documentDirectory, in: .userDomainMask).first!
    
    static var RecordsURL: URL?
    
    static var MediasURL: URL?
    
    static let PhotosURL = DocumentsDirectory.appendingPathComponent("Photos", isDirectory: true)
    
    static var AccessLevels = ["Public", "Private"]
    
    static var Tags = [String:[AnyObject]]()
    
    static var Species = [String:[AnyObject]]()
    
    static var Units = [String:[AnyObject]]()
    
    // URL of the authorization script
    static let authScript = "http://ecocollector.azurewebsites.net/get_login.php"
    
    //URL of the list-getting script
    static let listScript = "http://ecocollector.azurewebsites.net/get_lists.php"
    
    // URL to PHP script for uploading new records via POST.
    static let recordAddScript = "http://ecocollector.azurewebsites.net/add_records.php"
    
    // URL to blob storage Account.
    static let blobRootURLString = "https://ecomapper.blob.core.windows.net/"
}
