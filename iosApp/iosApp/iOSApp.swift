//
//  iOSApp.swift
//
//  KMMTEMPLATE-iOS
//

import ComposeApp
import SwiftUI

@main
struct IOSApp: App {
    init() {
        let environment = Bundle.main.object(forInfoDictionaryKey: "AppEnvironment") as? String ?? "prod"
        KoinInitializerKt.doInitKoinIos(environment: environment)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
