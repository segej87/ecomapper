//
//  NewPhoto.swift
//  EcoMapper
//
//  Created by Jon on 7/7/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class NewPhoto: NSObject, NSCoding {
    
    // MARK: Properties
    
    var photo: UIImage?
    
    // MARK: Types
    
    struct PropertyKey {
        static let photoKey = "photo"
    }
    
    // MARK: Initialization
    
    init?(photo: UIImage?){
        self.photo = photo
        
        super.init()
        
        // Make initializer failable
        if photo == nil {
            return nil
        }
    }
    
    // MARK: NSCoding
    
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(photo, forKey: PropertyKey.photoKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let photo = aDecoder.decodeObjectForKey(PropertyKey.photoKey) as? UIImage
        
        // Must call designated initializer
        self.init(photo: photo)
    }
    
}