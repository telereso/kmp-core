import shared

@objc(CoreClient)
class CoreClient: NSObject {
    let sdk = CoreClientManager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()

    @objc(hi:withRejecter:)
    func hi(_ resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
      resolve("hiii")
    }

    @objc(fetchLaunchRockets:withResolver:withRejecter:)
    func fetchLaunchRockets(force: Bool, resolve: @escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        sdk.fetchLaunchRockets(forceReload: true)
                        .onSuccess { result in
                            guard let rocketsResult = result else {return}
                            resolve(RocketLaunch.companion.toJson(array: rocketsResult))
                        }.onFailure { KotlinThrowable in
                            reject("empty", "empty", KotlinThrowable.asError())
                        }
    }
}
