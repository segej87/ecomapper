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
    
    var uuid: String?
    
    // MARK: Types
    
    struct PropertyKey {
        static let uuidKey = "uuid"
    }
    
    // MARK: Initialization
    
    init?(uuid: String?){
        self.uuid = uuid
        
        super.init()
    }
    
    // MARK: NSCoding
    
    func encode(with aCoder: NSCoder) {
        aCoder.encode(uuid, forKey: PropertyKey.uuidKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let uuid = aDecoder.decodeObject(forKey: PropertyKey.uuidKey) as? String
        
        // Must call designated initializer
        self.init(uuid: uuid)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = FileManager().urls(for: .documentDirectory, in: .userDomainMask).first!
    static let ArchiveURL = DocumentsDirectory.appendingPathComponent("EcoLogin")
    
}
