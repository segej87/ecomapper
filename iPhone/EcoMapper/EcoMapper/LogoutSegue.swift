//
//  LogoutSegue.swift
//  EcoMapper
//
//  Created by Jon on 11/8/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import Foundation
import UIKit

class LogoutSegue: UIStoryboardSegue {
    
    override func perform() {
        source.present(destination, animated: true) {
            self.source.navigationController?.popToRootViewController(animated: false)
            UIApplication.shared.delegate?.window??.rootViewController = self.destination
        }
    }
}
