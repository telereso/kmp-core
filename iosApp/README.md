FOr iOS, the pos init is arleady defined.
We target 16

Make sure the SDK has generated a podspec. 

OPen terminal in the ios project and run 

```shell
pod install
```

if you faced an issue with ./gradle permission denied , run the following

```shell
chmod +x ../gradlew
```

there is a script in the gradle file that helps iOS refresh everytime a build happens so no need to run pod install all the time. 

Sample usage is in the ContentView Swift UI file. 

