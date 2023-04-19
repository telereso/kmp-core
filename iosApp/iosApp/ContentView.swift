
import SwiftUI
import core

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel

    var body: some View {
        VStack {
            Text("🚀 Total Rockets Launched: " + String("test"))
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

        init() {
            loadRockets()
        }

        func loadRockets() {

        }

    }

}