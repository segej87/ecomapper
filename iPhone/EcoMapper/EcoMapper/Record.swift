//
//  Record.swift
//  EcoMapper
//
//  Created by Jon on 6/20/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class Record: NSObject, NSCoding {
    
    // MARK: Properties
    
    var coords: [Double]
    var photo: UIImage?
    var props: [String:AnyObject]
    
    // MARK: Types
    
    struct PropertyKey {
        static let coordKey = "coordinates"
        static let photoKey = "photo"
        static let propsKey = "properties"
    }
    
    // MARK: Initialization
    
    init?(coords: [Double], photo: UIImage?, props: [String:AnyObject]){
        self.coords = coords
        self.photo = photo
        self.props = props
        
        super.init()
        
        // Make initializer failable
        if coords.count < 2 || !props.keys.contains("name") {
            return nil
        }
    }
    
    // MARK: JSON formatting
    
    func prepareForJSON() -> [String:AnyObject] {
        let geomDict = ["type":"Point", "coordinates":self.coords]
        let typeDict = "Feature"
        var propDict = self.props
        if propDict.keys.contains("value") {
            let val = (propDict["value"] as! NSString).floatValue
            propDict.updateValue(val, forKey: "value")
        }
        let bigDict = ["geometry":geomDict, "type":typeDict, "properties":propDict]
        return bigDict
    }
    
    // MARK: NSCoding
    
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(coords, forKey: PropertyKey.coordKey)
        aCoder.encodeObject(photo, forKey: PropertyKey.photoKey)
        aCoder.encodeObject(props, forKey: PropertyKey.propsKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let coords = aDecoder.decodeObjectForKey(PropertyKey.coordKey) as! [Double]
        let photo = aDecoder.decodeObjectForKey(PropertyKey.photoKey) as? UIImage
        let props = aDecoder.decodeObjectForKey(PropertyKey.propsKey) as! [String:AnyObject]
        
        // Must call designated initializer
        self.init(coords: coords, photo: photo, props: props)
    }
    
    // MARK: Archiving Paths
    
    static let DocumentsDirectory = NSFileManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first!
    //static let ArchiveURL = DocumentsDirectory.URLByAppendingPathComponent("ecoRecords")
    static let ArchiveURL = DocumentsDirectory.URLByAppendingPathComponent("Records-\(UserVars.guid!)")
    
}