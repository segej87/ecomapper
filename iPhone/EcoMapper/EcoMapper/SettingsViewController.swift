//
//  SettingsViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/18/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class SettingsViewController: UIViewController, UIPickerViewDataSource, UIPickerViewDelegate {
    
    // MARK: Properties
    
    /*
     UI Properties
     */
    @IBOutlet weak var minAccLabel: UILabel!
    @IBOutlet weak var minAccSlider: UISlider!
    @IBOutlet weak var minStabLabel: UILabel!
    @IBOutlet weak var minStabSlider: UISlider!
    @IBOutlet weak var maxTimeLabel: UILabel!
    @IBOutlet weak var maxTimeSlider: UISlider!
    @IBOutlet weak var syncStartupPicker: UIPickerView!
    @IBOutlet weak var syncFrequencyPicker: UIPickerView!
    @IBOutlet weak var uploadWiFiSwitch: UISwitch!
    
    /*
     The user defaults object
     */
    var defaults: UserDefaults?
    
    /*
     Options for the startup sync picker
     */
    let syncPickerData = ["Always","WiFi Only","Never"]
    let freqPickerData = ["Manual"]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        defaults = UserDefaults.standard

        setUpSliders()
        
        setUpPickers()
        
        setUpSwitches()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // Picker delegate methods
    
    @available(iOS 2.0, *)
    public func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) ->Int {
        switch pickerView {
        case syncStartupPicker:
            return syncPickerData.count
        case syncFrequencyPicker:
            return freqPickerData.count
        default:
            return 0
        }
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        switch pickerView {
        case syncStartupPicker:
            return syncPickerData[row]
        case syncFrequencyPicker:
            return freqPickerData[row]
        default:
            return nil
        }
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        switch pickerView {
        case syncStartupPicker:
            defaults?.set(syncPickerData[row], forKey: "DefSyncStart")
            break
        case syncFrequencyPicker:
            defaults?.set(freqPickerData[row], forKey: "DefSyncFreq")
            break
        default:
            break
        }
    }
    
    // MARK: UI Methods
    
    func setUpSliders() {
        minAccSlider.minimumValue = 20
        minAccSlider.maximumValue = 100
        minAccSlider.value = (defaults?.float(forKey: "DefMinAcc"))!
        minAccLabel.text = "\(Int(minAccSlider.value)) m"
        
        minStabSlider.minimumValue = 20
        minStabSlider.maximumValue = 100
        minStabSlider.value = (defaults?.float(forKey: "DefMinStab"))!
        minStabLabel.text = "\(Int(minStabSlider.value)) m"
        
        maxTimeSlider.minimumValue = 1
        maxTimeSlider.maximumValue = 3
        maxTimeSlider.value = (defaults?.float(forKey: "DefMaxTime"))!
        maxTimeLabel.text = "\(Double(maxTimeSlider.value)) minutes"
    }
    
    func setUpPickers() {
        syncStartupPicker.dataSource = self
        syncStartupPicker.delegate = self
        
        syncFrequencyPicker.dataSource = self
        syncFrequencyPicker.delegate = self
        
        let startupValue = syncPickerData.index(of: (defaults?.string(forKey: "DefSyncStart"))!)
        syncStartupPicker.selectRow(startupValue!, inComponent: 0, animated: false)
        
        let freqValue = freqPickerData.index(of: (defaults?.string(forKey: "DefSyncFreq"))!)
        syncFrequencyPicker.selectRow(freqValue!, inComponent: 0, animated: false)
    }
    
    func setUpSwitches() {
        uploadWiFiSwitch.isOn = (defaults?.bool(forKey: "PhotoWiFi"))!
    }
    
    // MARK: Actions
    
    @IBAction func sliderValueChanged(_ sender: UISlider) {
        let currentValue = sender.value
        
        switch sender {
        case minAccSlider:
            let roundedVal = 5 * Int(round(currentValue / 5))
            minAccSlider.value = Float(roundedVal)
            defaults?.set(roundedVal, forKey: "DefMinAcc")
            minAccLabel.text = "\(roundedVal) m"
            break
        case minStabSlider:
            let roundedVal = 5 * Int(round(currentValue / 5))
            minStabSlider.value = Float(roundedVal)
            defaults?.set(roundedVal, forKey: "DefMinStab")
            minStabLabel.text = "\(roundedVal) m"
            break
        case maxTimeSlider:
            let roundedVal = 0.5 * Double(round(currentValue / 0.5))
            maxTimeSlider.value = Float(roundedVal)
            defaults?.set(roundedVal, forKey: "DefMaxTime")
            maxTimeLabel.text = "\(roundedVal) minutes"
        default:
            break
        }
    }
    
    @IBAction func switchValueChanged(_ sender: UISwitch) {
        print("Switch state: \(sender.isOn)")
        defaults?.set(sender.isOn, forKey: "PhotoWiFi")
    }
}
