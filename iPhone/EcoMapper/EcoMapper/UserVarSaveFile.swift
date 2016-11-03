//
//  UserVarSaveFile.swift
//  EcoMapper
//
//  Created by Jon on 11/3/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation

class UserVarsSaveFile: NSObject, NSCoding {
    
    // MARK: Properties

    var userName: String?
    var accessLevels: [String]?
    var tags: [String:[AnyObject]]?
    var species: [String:[AnyObject]]?
    var units: [String:[AnyObject]]?
    var accessDefaults: [String]?
    var tagDefaults: [String]?
    var speciesDefault: String?
    var unitsDefault: String?
    
    // MARK: Types
    
    struct PropertyKey {
        static let userNameKey = "username"
        static let accessKey = "access"
        static let tagsKey = "tags"
        static let speciesKey = "species"
        static let unitsKey = "units"
        static let accessDefaultsKey = "accessDefaults"
        static let tagDefaultsKey = "tagDefaultsKey"
        static let speciesDefaultKey = "speciesDefaultKey"
        static let unitsDefaultKey = "unitsDefaultKey"
    }
    
    // MARK: Initialization
    
    init(userName: String?, accessLevels: [String]?, tags: [String:[AnyObject]]?, species: [String:[AnyObject]]?, units: [String:[AnyObject]]?, accessDefaults: [String]?, tagDefaults: [String]?, speciesDefault: String?, unitsDefault: String?){
        self.userName = userName
        self.accessLevels = accessLevels
        self.tags = tags
        self.species = species
        self.units = units
        self.accessDefaults = accessDefaults
        self.tagDefaults = tagDefaults
        self.speciesDefault = speciesDefault
        self.unitsDefault = unitsDefault
        
        super.init()
    }
    
    // MARK: NSCoding
    
    func encode(with aCoder: NSCoder) {
        aCoder.encode(userName, forKey: PropertyKey.userNameKey)
        aCoder.encode(accessLevels, forKey: PropertyKey.accessKey)
        aCoder.encode(tags, forKey: PropertyKey.tagsKey)
        aCoder.encode(species, forKey: PropertyKey.speciesKey)
        aCoder.encode(units, forKey: PropertyKey.unitsKey)
        aCoder.encode(accessDefaults, forKey: PropertyKey.accessDefaultsKey)
        aCoder.encode(tagDefaults, forKey: PropertyKey.tagDefaultsKey)
        aCoder.encode(speciesDefault, forKey: PropertyKey.speciesDefaultKey)
        aCoder.encode(unitsDefault, forKey: PropertyKey.unitsDefaultKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let userName = aDecoder.decodeObject(forKey: PropertyKey.userNameKey) as? String
        let accessLevels = aDecoder.decodeObject(forKey: PropertyKey.accessKey) as? [String]
        let tags = aDecoder.decodeObject(forKey: PropertyKey.tagsKey) as? [String:[AnyObject]]
        let species = aDecoder.decodeObject(forKey: PropertyKey.speciesKey) as? [String:[AnyObject]]
        let units = aDecoder.decodeObject(forKey: PropertyKey.unitsKey) as? [String:[AnyObject]]
        let accessDefaults = aDecoder.decodeObject(forKey: PropertyKey.accessDefaultsKey) as? [String]
        let tagDefaults = aDecoder.decodeObject(forKey: PropertyKey.tagDefaultsKey) as? [String]
        let speciesDefault = aDecoder.decodeObject(forKey: PropertyKey.speciesDefaultKey) as? String
        let unitsDefault = aDecoder.decodeObject(forKey: PropertyKey.unitsDefaultKey) as? String
        
        // Must call designated initializer
        self.init(userName: userName, accessLevels: accessLevels, tags: tags, species: species, units: units, accessDefaults: accessDefaults, tagDefaults: tagDefaults, speciesDefault: speciesDefault, unitsDefault: unitsDefault)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = FileManager().urls(for: .documentDirectory, in: .userDomainMask).first!
    
}
