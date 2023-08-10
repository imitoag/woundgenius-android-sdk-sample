# WoundGeniusSDK Sample App

This repository contains the WoundGenius SDK Sample App to demonstrate the available features of WoundGenius SDK.
And to be used as a reference app for Android team to integrate the WoundGenius SDK to their project.

## Initial Setup
To launch the Sample App perform the following actions:
1. Request the WoundGenius SDK license, request access to the WoundGenius SDK repository for your Android Developers. Follow the instructions listed here to do that: https://support.imito.io/portal/en/kb/articles/licence-key (You'll need to Sign Up, provide the Bundle Ids you are planning to use, GitHub username of the developers).
2. Download/Pull this Sample app to your machine. Open project in Android Studio.
![android_sample_screenshot_1](https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/6385965a-14bf-46f7-985e-e5ce9e2f0a66)
3. There are two ways to implement the SDK.
   Option 1 
• In Github account you provided in Step 1 open Settings -> Developer Settings -> Personal Access Tokens -> Tokens(classic) -> Generate new token. Make sure you select the following scopes (“read:packages”) and Generate a token. After Generating make sure to copy your new personal access token. You cannot see it again! The only option is to generate a new key.
• Create a github.properties file within your root Android project
• Add properties username=GITHUB_USERID and token=PERSONAL_ACCESS_TOKEN. Replace GITHUB_USERID with personal / organisation Github User ID and PERSONAL_ACCESS_TOKEN with the token generated in Step 3.
• Add the following code to build.gradle inside the module that will be using the library 
```ruby   
 repositories {
        maven {
                name = "GitHubPackages"
                url = uri("https://github.com/imitoag/woundgenius-android-sdk")
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
• Add sdk as local maven repo by adding the following code to build.gradle inside the app module
```ruby   
 repositories {
      maven {
            url "SDK"
        }
    }
```

6. Run the application.
7. After you'll launch the application - go to Settings or press "License Key" button. And paste the license key you've received at Step 1.

<img width="300" alt="image" src="https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/2ff90976-d4de-49f7-b8bd-c77a467244bb">
<img width="300" alt="image" src="https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/4194ea6a-8998-417d-9988-15f2d024b14e">

8. Navigate back from Settings Screen. Click Start Capturing. Grant the permission for the app to access the Camera.
 <img width="300" alt="image" src="https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/12295c8e-719c-480d-b26d-a2d12dca3d49">
