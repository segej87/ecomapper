//
//  PickerTableViewCell.swift
//  EcoMapper
//
//  Created by Jon on 11/8/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class PickerTableViewCell: UITableViewCell {
    
    
    // MARK: Properties
    
    @IBOutlet weak var itemLabel: UILabel!
    @IBOutlet weak var defaultButton: UIButton!
    
    var itemType = ""
    
    var value = "" {
        didSet {
            itemLabel.text = value
        }
    }
    
    var isDefault = false {
        didSet {
            if isDefault {
                defaultButton.setTitle("Remove default", for: .normal)
            } else {
                defaultButton.setTitle("Add default", for: .normal)
            }
        }
    }
    
    // MARK: Initialization
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    
    // MARK: UITableView Methods
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    
    // MARK: Defaults
    
    @IBAction func manageDefaultChange(_ sender: UIButton) {
        switch (itemType) {
        case "tags":
            if !isDefault {
                UserVars.TagsDefaults.append(value)
            } else {
                let ind = UserVars.TagsDefaults.index(of: value)
                if ind != -1 {
                    UserVars.TagsDefaults.remove(at: ind!)
                }
            }
            isDefault = !isDefault
            break
        case "species":
            if !isDefault {
                UserVars.SpecDefault = value
            } else {
                UserVars.SpecDefault = nil
            }
            isDefault = !isDefault
            break
        case "units":
            if !isDefault {
                UserVars.UnitsDefault = value
            } else {
                UserVars.UnitsDefault = nil
            }
            isDefault = !isDefault
            break
        case "access":
            if !isDefault {
                print("Adding \(value) to Access Defaults")
                UserVars.AccessDefaults.append(value)
            } else {
                print("Removing \(value) from Access Defaults")
                let ind = UserVars.AccessDefaults.index(of: value)
                if ind != -1 {
                    UserVars.AccessDefaults.remove(at: ind!)
                }
            }
            isDefault = !isDefault
            break
        default:
            break
        }
    }
    
    
}
