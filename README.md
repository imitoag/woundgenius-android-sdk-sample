# WoundGeniusSDK Sample App

This repository contains the WoundGenius SDK Sample App for demonstrating the available features of WoundGenius SDK. It can be used as a reference app for Android team to integrate the WoundGenius SDK into their project.


## Initial Setup
To launch the Sample App, perform the following actions:
1. Request the WoundGenius SDK license, and request access to the WoundGenius SDK repository for your Android Developers. Follow the instructions listed here to do that: https://support.imito.io/portal/en/kb/articles/licence-key (You'll need to Sign Up and provide the Bundle Ids you are planning to use, and the developer GitHub usernames).
2. Download/Pull this Sample app to your machine. Open project in Android Studio.
![android_sample_screenshot_1](https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/2c12b8f9-bfab-4248-8223-94559e71cb59)

3. There are two ways to implement the SDK:
   
 Option 1 

• In the Github account you provided in Step 1, open Settings -> Developer Settings -> Personal Access Tokens -> Tokens(classic) -> Generate new token. Make sure you select the following scopes (“read:packages”) and Generate a token. After generating, make sure to copy your new personal access token. You won't see it again! The only option is to generate a new key. 

• Create a github.properties file in the root of your Android project

• Add the properties username=GITHUB_USERID and token=PERSONAL_ACCESS_TOKEN. Replace GITHUB_USERID with personal / organisation Github User ID and PERSONAL_ACCESS_TOKEN with the token generated in Step 3.

• Add the following code to build.gradle inside the project gradle 

```ruby   
 repositories {
    google()
    mavenCentral()
    jcenter()
    maven { url 'https://maven.google.com' }
    maven { url "https://jitpack.io" }
        maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/imitoag/woundgenius-android-sdk")
                def propsFile = rootProject.file('github.properties')
                def props = new Properties()
                props.load(new FileInputStream(propsFile))
                credentials {
                    username props['username']
                    password props['token']
                }
        }
    }
```

 Option 2 

• Add sdk as local maven repo and repositories urls by adding the following code to build.gradle inside the project gradle
3.  into your project build.gradle file
```ruby   
 repositories {
   google()
    mavenCentral()
    maven { url 'SDK' }
    jcenter()
    maven { url 'https://maven.google.com' }
    maven { url "https://jitpack.io" }
}
```

6. Run the application.
7. After you launch the application, go to Settings or press "License Key" button, and paste the license key you've received at Step 1.

<img width="300"  alt="image" src="https://github.com/user-attachments/assets/5ad8f1fc-f7ee-46e0-a220-15a2e2882352" />
<img width="300" alt="image" src="https://github.com/user-attachments/assets/0d3a60ce-3550-4e8a-b0ec-1ee841d642bb" />

8. Navigate back from Settings Screen. Click Start Capturing. Grant the permission for the app to access the Camera.
<img width="300"  alt="image" src="https://github.com/user-attachments/assets/5df9a748-0ce7-4f93-8730-cae3d4704012" />

# WoundGenius SDK React Native Sample App

This repository contains the React Native Sample (SampleReactNative folder).

## Initial Setup
To launch the SampleReactNative App, perform the following actions:

1. Pull the repository.
2. Navigate to the SampleReactNative.
3. In terminal run a command npm install. That should setup the node_modules.
4. Open the android folder using AndroidStudio.
5. Follow one of the options for integrating WoundGenius SDk into the Sample app.
6. Update the license key in AndroidStudio in the MainApplication class file.
7. Navigate out of android folder in terminal, to the SampleReactNative folder. Run the ```npx react-native run-android``` command in terminal. This should start the dev server usually on port 8081.
8. Switch back to AndroidStudio to run the Android app on your device/emulator or press a keybutton in terminal to run app on Android from React Native.
9. Click "Start Capturing". Allow access to Camera.

# WoundGenius SDK Flutter Sample App

This repository contains the Flutter Sample (SampleFlutter folder).

## Initial Setup
To launch the Sample Flutter App, perform the following actions:

1. Pull the repository.
2. Navigate to the SampleFlutter -> imito_dart_ffi_investigation
3. Open terminal in this folder using VS code on native terminal. 
4. Run ```flutter doctor``` to make sure your environment is ready. If you see any red 'X' marks, follow the suggested commands to fix them
5. Connect device to your machine using cable or use ```flutter devices``` command to selected prefered device if it's already connected
6. Open the android folder and update the license key in the MainApplication class file, if necessary.
7. Execute ```flutter run``` commad if you do not have Android Studio installed. Otherwise, run it through Android Studio.
8. Click "Start Capturing". Allow access to Camera.


# WoundGenius SDK Ionic Angular Sample App

This repository contains the Ionic Angular Sample (SampleIonicAngular folder).

## Initial Setup
To launch the SampleIonicAngular App, perform the following actions:

1. Pull the repository.
2. Navigate to the SampleIonicAngular -> woundGeniusSampleIA.
3. In terminal run a command ```npm install```. That should setup the angular_modules.
4. Execute ``npm install -g @ionic/cli`` commad if you do not have ionic installed.
5. Follow one of the options for integrating WoundGenius SDK into the Sample app.
6. Update the license key in AndroidStudio in the ```MainApplication``` class file.
7. run a command ```ionic capacitor run android``` and select the desired device on which you want to install the android application. If the options are empty, launch an android emulator or connect your device via cable and open Android Studio.
8. Click "Open SDK". Allow access to Camera.
