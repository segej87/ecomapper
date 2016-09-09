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
    var accessLevels: [String]?
    var tags: [String:[AnyObject]]?
    var species: [String]?
    
    // MARK: Types
    
    struct PropertyKey {
        static let uuidKey = "uuid"
        static let accessKey = "access"
        static let tagsKey = "tags"
        static let speciesKey = "species"
    }
    
    // MARK: Initialization
    
    init?(uuid: String?, accessLevels: [String]?, tags: [String:[AnyObject]]?, species: [String]?){
        self.uuid = uuid
        self.accessLevels = accessLevels
        self.tags = tags
        self.species = species
        
        super.init()
        
        // Make initializer failable
        if uuid == nil {
            return nil
        }
    }
    
    // MARK: NSCoding
    
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(uuid, forKey: PropertyKey.uuidKey)
        aCoder.encodeObject(accessLevels, forKey: PropertyKey.accessKey)
        aCoder.encodeObject(tags, forKey: PropertyKey.tagsKey)
        aCoder.encodeObject(species, forKey: PropertyKey.speciesKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let uuid = aDecoder.decodeObjectForKey(PropertyKey.uuidKey) as! String
        let accessLevels = aDecoder.decodeObjectForKey(PropertyKey.accessKey) as? [String]
        let tags = aDecoder.decodeObjectForKey(PropertyKey.tagsKey) as? [String:[AnyObject]]
        let species = aDecoder.decodeObjectForKey(PropertyKey.speciesKey) as? [String]
        
        // Must call designated initializer
        self.init(uuid: uuid, accessLevels: accessLevels, tags: tags, species: species)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = NSFileManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first!
    static let ArchiveURL = DocumentsDirectory.URLByAppendingPathComponent("EcoLogin")
    
}
