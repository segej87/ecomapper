//
//  LoginInfo.swift
//  EcoMapper
//
//  Created by Jon on 6/30/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation

class LoginInfo: NSObject, NSCoding {
    
    // MARK: Properties
    
    var guid: String?
    
    // MARK: Types
    
    struct PropertyKey {
        static let guidKey = "guid"
    }
    
    // MARK: Initialization
    
    init?(guid: String?){
        self.guid = guid
        
        super.init()
        
        // Make initializer failable
        if guid == nil {
            return nil
        }
    }
    
    // MARK: NSCoding
    
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(guid, forKey: PropertyKey.guidKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let guid = aDecoder.decodeObjectForKey(PropertyKey.guidKey) as! String
        
        // Must call designated initializer
        self.init(guid: guid)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = NSFileManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first!
    static let ArchiveURL = DocumentsDirectory.URLByAppendingPathComponent("EcoLogin")
    
}
