import SwiftUI
import Shared

@main
struct McAffenApp: App {
    @StateObject private var store = AppStore()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
                .task { await store.refresh() }
        }
    }
}
