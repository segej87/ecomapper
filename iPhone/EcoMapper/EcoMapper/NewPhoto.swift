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
    
    func encode(with aCoder: NSCoder) {
        aCoder.encode(photo, forKey: PropertyKey.photoKey)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        let photo = aDecoder.decodeObject(forKey: PropertyKey.photoKey) as? UIImage
        
        // Must call designated initializer
        self.init(photo: photo)
    }
    
}
