//
//  UserVars.swift
//  EcoMapper
//
//  Created by Jon on 6/30/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation
import UIKit

struct UserVars {
    static var uuid: String? {
        didSet {
            RecordsURL = DocumentsDirectory.URLByAppendingPathComponent("Records-\(uuid)")
            MediasURL = DocumentsDirectory.URLByAppendingPathComponent("Media-\(uuid)")
        }
    }
    
    static let DocumentsDirectory = NSFileManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first!
    
    static var RecordsURL: NSURL?
    
    static var MediasURL: NSURL?
}