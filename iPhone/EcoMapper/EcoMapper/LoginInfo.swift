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
    var species: [String:[AnyObject]]?
    var units: [String:[AnyObject]]?
    
    // MARK: Types
    
    struct PropertyKey {
        static let uuidKey = "uuid"
        static let accessKey = "access"
        static let tagsKey = "tags"
        static let speciesKey = "species"
        static let unitsKey = "units"
    }
    
    // MARK: Initialization
    
    init?(uuid: String?, accessLevels: [String]?, tags: [String:[AnyObject]]?, species: [String:[AnyObject]]?, units: [String:[AnyObject]]?){
        self.uuid = uuid
        self.accessLevels = accessLevels
        self.tags = tags
        self.species = species
        self.units = units
        
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
        aCoder.encodeObject(units, forKey: PropertyKey.unitsKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let uuid = aDecoder.decodeObjectForKey(PropertyKey.uuidKey) as! String
        let accessLevels = aDecoder.decodeObjectForKey(PropertyKey.accessKey) as? [String]
        let tags = aDecoder.decodeObjectForKey(PropertyKey.tagsKey) as? [String:[AnyObject]]
        let species = aDecoder.decodeObjectForKey(PropertyKey.speciesKey) as? [String:[AnyObject]]
        let units = aDecoder.decodeObjectForKey(PropertyKey.unitsKey) as? [String:[AnyObject]]
        
        // Must call designated initializer
        self.init(uuid: uuid, accessLevels: accessLevels, tags: tags, species: species, units: units)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = NSFileManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first!
    static let ArchiveURL = DocumentsDirectory.URLByAppendingPathComponent("EcoLogin")
    
}
