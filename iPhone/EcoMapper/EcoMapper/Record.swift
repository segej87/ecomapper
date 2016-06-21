//
//  Record.swift
//  EcoMapper
//
//  Created by Jon on 6/20/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class Record {
    
    // MARK: Properties
    
    var coords: [Float]
    var photo: UIImage?
    var props: [String:AnyObject]
    
    // MARK: Initialization
    
    init?(coords: [Float], photo: UIImage?, props: [String:AnyObject]){
        self.coords = coords
        self.photo = photo
        self.props = props
        
        // Make initializer failable
        if coords.count < 2 || !props.keys.contains("name") {
            return nil
        }
    }
}