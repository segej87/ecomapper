//
//  NetworkTests.swift
//  EcoMapper
//
//  Created by Jon on 11/10/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation

struct NetworkTests {
    
    /*
     A Reachability object
     */
    static var reachability : Reachability?
    
    // MARK: Reachability functions
    
    static func setupReachability(_ hostName: String?) {
        do {
            let reachability = try hostName == nil ? Reachability.reachabilityForInternetConnection() : Reachability(hostname: hostName!)
            self.reachability = reachability
        } catch ReachabilityError.failedToCreateWithAddress(let address) {
            NSLog("Unable to create Reachability with address: \(address)")
            return
        } catch {}
    }
}
