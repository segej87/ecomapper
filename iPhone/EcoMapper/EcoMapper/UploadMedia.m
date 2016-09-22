//
//  UploadMedia.m
//  EcoMapper
//
//  Created by Jon on 6/28/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

#import "UploadMedia.h"

@implementation UploadMedia : NSObject

-(void)uploadBlobToContainer: ( NSString *) guid :( UIImage *) media :( NSString *) mediaName {
    NSError *error = nil;
    
    // Create a storage account object from a connection string.
    AZSCloudStorageAccount *account = [AZSCloudStorageAccount accountFromConnectionString:@"DefaultEndpointsProtocol=https;AccountName=ecomapper;AccountKey=c0h6WIRF2ObRNWwAkp9arNRLb1KUa0/fZwnKohRwgZfrbVca5WXPxIqJKPeSVyK1oPdAgbIghCpPJNayrId1tw==" error: &error];
    
    // Create a blob service client object.
    AZSCloudBlobClient *blobClient = [account getBlobClient];
    
    // Create a local container object.
    AZSCloudBlobContainer *blobContainer = [blobClient containerReferenceFromName:guid];
    
    [blobContainer createContainerIfNotExistsWithAccessType:AZSContainerPublicAccessTypeContainer requestOptions:nil operationContext:nil completionHandler:^(NSError *error, BOOL exists){
        if (error){
            NSLog(@"Error in creating container.");
        }
        else{
            // Get the image data
            if (media != nil) {
                NSData *data = UIImageJPEGRepresentation(media, 1.0f);
                
                // Create a local blob object
                AZSCloudBlockBlob *blockBlob = [blobContainer blockBlobReferenceFromName:mediaName];
                
                if (data != nil) {
                    // Upload blob to storage
                    [blockBlob uploadFromData: data completionHandler: ^(NSError *_Nullable uploadError) {
                        if (uploadError != NULL) {
                            NSLog(@"Error uploading blob");
                        } else {
                            NSLog(@"Success!");
                        }
                    }];
                } else {
                    NSLog(@"Empty content");
                }
            }
        }
    }];
}

@end