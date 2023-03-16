//
//  AppDelegate.swift
//
//

import Foundation
import UIKit
import shared
import netfox

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        CoreClient().debugLogger()
        NFX.sharedInstance().start()
        return true
    }
}
