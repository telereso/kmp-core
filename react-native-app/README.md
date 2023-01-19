We are using yarn to refresh and run the project.
Please keep in mind not to use npm as there are issues, 

Under package.json file we defined script named refresh.
running refresh will remove the current CoreClient dependecny and reinstall it.

To get started 
change the local dependecy absolute path to the one poiting to the CoreClient's production library..
This is the folder generatd when we run the jsBrowserProductionLibraryDistribution gradle command. 
"dependencies": {
"CoreClient-shared": "file:{your_absolute_path}",
},

Make sure to update the refresh script path as well witht the same path 

Run yarn refresh
IF all good

you can now run yarn to load the project on any target platform you choose.

In our case 
yarn android

There are samples in the App.js file of hwo we can are
access various classes and render on the UI.

We faced some issues like Kotlin object support, package namings but we sure to resoleve these during further development. 

on SDK
once changes are made run
./gradlew jsBrowserProductionLibraryDistribution
This will repacakge the productionLibrary referenced as a absolute path. 
Once the RN app refresh ti will auto pickup the changes. 
