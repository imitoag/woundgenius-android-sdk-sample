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

• Add sdk as local maven repo by adding the following code to build.gradle inside the project gradle
```ruby   
 repositories {
      maven {
            url "SDK"
        }
    }
```

6. Run the application.
7. After you launch the application, go to Settings or press "License Key" button, and paste the license key you've received at Step 1.

<img width="300" alt="image" src="https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/c405785e-e0c5-41ba-8116-6681f2d4a171">
<img width="300" alt="image" src="https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/1a2ef027-3c1d-4122-91c3-37d58c4ed7c9">

8. Navigate back from Settings Screen. Click Start Capturing. Grant the permission for the app to access the Camera.
 <img width="300" alt="image" src="https://github.com/imitoag/woundgenius-android-sdk-sample/assets/139133999/f8fd1408-19eb-46da-835b-7001d3ac8e31">
