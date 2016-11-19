//
//  CropImageViewController.swift
//  EcoMapper
//
//  Created by Jon on 11/11/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

import UIKit

class CropImageViewController: UIViewController {
    
    
    //MARK: Class Variables
    
    @IBOutlet weak var cropImageView: CroppableImageView!
    @IBOutlet weak var saveButton: UIButton!
    @IBOutlet weak var cancelButton: UIButton!
    
    var inputImage : UIImage?
    var outputImage : UIImage?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        cropImageView.constrainSquare = true
        cropImageView.imageToCrop = inputImage
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    // MARK: Actions
    
    @IBAction func attemptSave(_ sender: UIButton) {
        if let croppedImage = cropImageView.croppedImage() {
            outputImage = croppedImage
            self.performSegue(withIdentifier: "saveExitSegue", sender: self)
        } else {
            if #available(iOS 9.0, *) {
                let alertVC = UIAlertController(title: "Can't save", message: "Please crop the image before saving it", preferredStyle: .alert)
                let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
                alertVC.addAction(okAction)
                self.present(alertVC, animated: true, completion: nil)
            } else {
                let alertVC = UIAlertView(title: "Can't save", message: "Please crop the image before saving it", delegate: self, cancelButtonTitle: "OK")
                alertVC.show()
            }
        }
    }
    
}
