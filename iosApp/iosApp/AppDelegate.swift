//
//  AppDelegate.swift
//
//

import Foundation
import UIKit
import core
import netfox

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        CoreClient().debugLogger()
        NFX.sharedInstance().start()
        NFX.sharedInstance().ignoreURL("https://tmpfiles.org/api/v1/upload")
        TasksExamples().testVerify(coreClient: CoreClient())
        print(CoreClient().isAppInstalledWithScheme(scheme: "sms"))
        return true
    }
}
