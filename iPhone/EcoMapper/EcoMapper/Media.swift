//
//  Media.swift
//  EcoMapper
//
//  Created by Jon on 6/27/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class Media: NSObject, NSCoding {
    
    // MARK: Properties
    
    var mediaName: String?
    var mediaPath: NSURL?
    
    // MARK: Types
    
    struct PropertyKey {
        static let nameKey = "name"
        static let pathKey = "path"
    }
    
    // MARK: Initialization
    
    init?(name: String, path: NSURL?){
        self.mediaName = name
        self.mediaPath = path
        
        super.init()
        
        // Make initializer failable
        if name == "" || path == nil {
            return nil
        }
    }
    
    // MARK: NSCoding
    
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(mediaName, forKey: PropertyKey.nameKey)
        aCoder.encodeObject(mediaPath, forKey: PropertyKey.pathKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let name = aDecoder.decodeObjectForKey(PropertyKey.nameKey) as! String
        let path = aDecoder.decodeObjectForKey(PropertyKey.pathKey) as? NSURL
        
        // Must call designated initializer
        self.init(name: name, path: path)
    }
    
}
