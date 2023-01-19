#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(CoreClient, NSObject)

RCT_EXTERN_METHOD(hi:
                 (RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(fetchLaunchRockets:(BOOL)force
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
