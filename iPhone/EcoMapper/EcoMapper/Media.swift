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
    var mediaPath: URL?
    var marked: Bool?
    
    
    // MARK: Types
    
    struct PropertyKey {
        static let nameKey = "name"
        static let pathKey = "path"
        static let markKey = "mark"
    }
    
    
    // MARK: Initialization
    
    init?(name: String, path: URL?, marked: Bool){
        self.mediaName = name
        self.mediaPath = path
        self.marked = marked
        
        super.init()
        
        // Make initializer failable
        if name == "" || path == nil {
            return nil
        }
    }
    
    
    // MARK: NSCoding
    
    func encode(with aCoder: NSCoder) {
        aCoder.encode(mediaName, forKey: PropertyKey.nameKey)
        aCoder.encode(mediaPath, forKey: PropertyKey.pathKey)
        aCoder.encode(marked, forKey: PropertyKey.markKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let name = aDecoder.decodeObject(forKey: PropertyKey.nameKey) as! String
        let path = aDecoder.decodeObject(forKey: PropertyKey.pathKey) as? URL
        let marked = aDecoder.decodeObject(forKey: PropertyKey.markKey) as! Bool
        
        // Must call designated initializer
        self.init(name: name, path: path, marked: marked)
    }
    
}
