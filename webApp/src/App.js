import logo from './logo.svg';
import './App.css';
import React, {useState, useEffect} from 'react';

const CoreClient = require("core-client").io.telereso.kmp.core

CoreClient.debugLogger()


function createPromise(): Promise<string> {
    return new Promise((resolve, reject) => {
        console.log("Promise to Task Converted ! ")
        resolve("Promise resul!")
    });
}

function App(block) {
    const [text, setText] = useState("Loading....");

    useEffect(async block => {
        // CoreClient.async(CoreClient.TasksExamples.hi()).then((data)=>{
        //   console.log("onSuccess",data)
        //  }).catch((e)=>{
        //   console.log("catch",e)
        // })

        CoreClient.TasksExamples.testVerify(new CoreClient.CoreClient())

        // #1 watching flow in JS
        let job1 = CoreClient.watch(CoreClient.TasksExamples.getFlowPayload(), (data) => {
            console.log(data);
        }, (error) => {
            console.log(error)
        })

        // #2 watching flow in JS
        // let job2 = CoreClient.TasksExamples.getFlowPayload().watch((data, error) => {
        //     if (error != null) {
        //         console.log(error)
        //     } else {
        //         console.log(data);
        //     }
        // })

        let job3 = CoreClient.watch(CoreClient.TasksExamples.getDelayedFlowString(), (data) => {
            console.log(data);
        }, (error) => {
            console.log(error)
        })

        setTimeout(function () {
            job1.cancel()
            // job2.cancel()
            job3.cancel()
        }, 2000);


        CoreClient.TasksExamples.exception()
            .onSuccess((e) => {
                console.log("onSuccess", e)
            })
            .onSuccessUI((e) => {
                const randomRocketLaunch = e[0]
                setText(`\uD83D\uDE80 Total Rockets Launched: ${e.length} ` +
                    `\n\nLast Rocket Mission: ${randomRocketLaunch.mission_name} ` +
                    `\n\nRocket Name: ${randomRocketLaunch.rocket?.name} \n` +
                    `\n` +
                    `Rocket Type: ${randomRocketLaunch.rocket?.type} `)
            })
            .onFailure((e) => {
                console.log(e)
            })
            .onFailureUI((e) => {
                setText(e.message)
            })


        // Convert Promise to Task
        console.log("Converting Promise to Task ")

        CoreClient.Tasks.fromString(new Promise((resolve, reject) => {
            console.log("Promise String to Task Converted ! ")
            resolve("Promise String resul!")
        })).onSuccess((res) => {
            console.log(res)
        })


        CoreClient.Tasks.from(createPromise())
            .onSuccess((res) => {
                console.log(res)
            })

        CoreClient.TasksExamples.apiCall()
            .onSuccess((res) => {
                console.log(res)
            })

        CoreClient.TasksExamples.testRetry3(CoreClient.TaskConfig.Companion
            .builder()
            .retry(5)
            .backOffDelay(1000)
            .startDelay(5000)
            .build())
            .onSuccess((res) => {
                console.log(res)
            })

        CoreClient.Tasks.create(() => {
            console.log("hi from created task")
        })

        CoreClient.Tasks.createWithConfig(CoreClient.TaskConfig.Companion
            .builder()
            .startDelay(4000)
            .build(), () => {
            console.log("hi from created task after 4 seconds")
        })
    },[]);

    return (
        <div className="App">
            <header className="App-header">
                <img src={logo} className="App-logo" alt="logo"/>
                <p>
                    {text}
                </p>
                <a
                    className="App-link"
                    href="https://reactjs.org"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Learn React
                </a>
            </header>
        </div>
    );
}

export default App;
