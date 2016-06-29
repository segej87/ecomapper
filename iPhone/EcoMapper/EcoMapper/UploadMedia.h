//
//  UploadMedia.h
//  EcoMapper
//
//  Created by Jon on 6/28/16.
//  Copyright © 2016 Sege Industries. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AZSClient/AZSClient.h>
#import <AssetsLibrary/AssetsLibrary.h>

@interface UploadMedia : NSObject

-(void)uploadBlobToContainer: ( NSString *) guid :( UIImage *) media :( NSString *) mediaName;

@end