//
//  EcoMapperTests.swift
//  EcoMapperTests
//
//  Created by Jon on 6/20/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit
import XCTest
@testable import EcoMapper

class EcoMapperTests: XCTestCase {
    
    // MARK: Record tests
    
    // Tests to confirm that the Record initializer returns when no name is provided or there are less than 2 coordinates
    func testRecordInitialization() {
        // Success case.
        let potentialItem = Record(coords: [100, 200, 233], photo: nil, props: ["name":"test record", "value":123 as Float, "units":"mg/l"])
        XCTAssertNotNil(potentialItem)
        
        // Failure cases.
        let noName = Record(coords: [100, 200, 233], photo: nil, props: ["value":123 as Float, "units":"mg/l"])
        XCTAssertNil(noName, "Empty name is invalid")
    }
    
}
