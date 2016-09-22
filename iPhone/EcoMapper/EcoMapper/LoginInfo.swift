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
    
    func encode(with aCoder: NSCoder) {
        aCoder.encode(uuid, forKey: PropertyKey.uuidKey)
        aCoder.encode(accessLevels, forKey: PropertyKey.accessKey)
        aCoder.encode(tags, forKey: PropertyKey.tagsKey)
        aCoder.encode(species, forKey: PropertyKey.speciesKey)
        aCoder.encode(units, forKey: PropertyKey.unitsKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let uuid = aDecoder.decodeObject(forKey: PropertyKey.uuidKey) as! String
        let accessLevels = aDecoder.decodeObject(forKey: PropertyKey.accessKey) as? [String]
        let tags = aDecoder.decodeObject(forKey: PropertyKey.tagsKey) as? [String:[AnyObject]]
        let species = aDecoder.decodeObject(forKey: PropertyKey.speciesKey) as? [String:[AnyObject]]
        let units = aDecoder.decodeObject(forKey: PropertyKey.unitsKey) as? [String:[AnyObject]]
        
        // Must call designated initializer
        self.init(uuid: uuid, accessLevels: accessLevels, tags: tags, species: species, units: units)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = FileManager().urls(for: .documentDirectory, in: .userDomainMask).first!
    static let ArchiveURL = DocumentsDirectory.appendingPathComponent("EcoLogin")
    
}
