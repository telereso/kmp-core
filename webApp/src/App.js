import logo from './logo.svg';
import './App.css';
import React, { useState ,useEffect} from 'react';
import {CoreClientManager} from "core-client"

const manger = new CoreClientManager.Builder().build()
window.manger = manger


function App() {
    const [text, setText] = useState("Loading....");

    useEffect(() => {
        manger.fetchLaunchRockets(true)
            .onSuccess((e) => {
                console.log("onSuccess",e)
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
