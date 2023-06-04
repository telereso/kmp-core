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

            // Create Task
            (Tasks.shared.create {
                JwtPayload(iss: "123123", exp: 6666, iat: 123, sub: "")
            } as! Task<JwtPayload>)
                    .onSuccess { object in
                        print(object?.iss ?? "")
                        print(6666)
                    }

            // Create Task with config
            (Tasks.shared.create(config: TaskConfig(retry: 3, backOffDelay: 1000, startDelay: 4000)) {
                "hi after 4 seconds"
            } as! Task<NSString>)
                    .onSuccess { srt in
                        print(srt ?? "")
                    }

            // Create Flow with primitive type

            (Flows.shared.from(list: ["123", "1234"]) as! CommonFlow<NSString>)
                    .watch { payload, exception in
                        print(payload)
                    }

            let job = TasksExamples.shared.getDelayedFlowString().watch { (str: NSString?, err: ClientException?) in
                print(str ?? "")
                print(err ?? "")
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                job.cancel()
            }




            // Create Flow with object

            (Flows.shared.from(list: [JwtPayload(iss: "33333", exp: 6666, iat: 123, sub: ""),
                                      JwtPayload(iss: "123123", exp: 6666, iat: 123, sub: "")]) as! CommonFlow<JwtPayload>)
                    .watch { payload, exception in
                        print(payload)
                    }

            TasksExamples.shared.testRetry3(config: TaskConfig(retry: 5, backOffDelay: 1000, startDelay: 3000)).onSuccess { res in
                print(res ?? "")
            }

        }

        func loadRockets() {

        }

    }

}
