import logo from './logo.svg';
import './App.css';
import React, { useState ,useEffect} from 'react';
const CoreClient =  require("core-client").io.telereso.kmp.core

CoreClient.debugLogger()

function App(block) {
    const [text, setText] = useState("Loading....");

    useEffect(async block => {
          // CoreClient.async(CoreClient.TasksExamples.hi()).then((data)=>{
          //   console.log("onSuccess",data)
          //  }).catch((e)=>{
          //   console.log("catch",e)
          // })

        // #1 watching flow in JS
        CoreClient.watch(CoreClient.TasksExamples.getFlowPayload(), (data) => {
            console.log(data);
        }, (error) => {
            console.log(error)
        })

        // #2 watching flow in JS
        // CoreClient.TasksExamples.getFlowPayload().watch((data, error) => {
        //     if (error != null) {
        //         console.log(error)
        //     } else {
        //         console.log(data);
        //     }
        // })
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
