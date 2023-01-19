
import SwiftUI
import shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel

    var body: some View {
        VStack {
            Text("🚀 Total Rockets Launched: " + String(viewModel.rocketsLaunched?.size ?? 0))
            Text("First Rocket Mission: " + String(viewModel.rocketsLaunched?.get(index: 0)?.mission_name ?? "loading"))
            Text("First Rocket name: " + String(viewModel.rocketsLaunched?.get(index: 0)?.rocket?.name ?? "loading"))
        }
        .padding()
    }

}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(viewModel: .init())
    }
}

extension ContentView {

    class ViewModel: ObservableObject {
        @Published var rocketsLaunched: KotlinArray<RocketLaunch>?
        let coreClientManager = CoreClientManager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()

        init() {
            loadRockets()
        }

        func loadRockets() {
            coreClientManager.fetchLaunchRockets(forceReload: true)
                .onSuccess  { result in
                    guard let rocketsResult = result else { return }
                    print("Total Rockect Missions:, \(rocketsResult.size)!")
                    print("First Rockect Mission:, \(rocketsResult.get(index: 0)?.mission_name ?? "")!")
                }.onSuccessUI  { [self] result in
                guard let rocketsResult = result else { return }
                rocketsLaunched = rocketsResult
            }
        }

    }

}