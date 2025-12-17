import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  
  static const platform = const MethodChannel('your_sdk_channel');

  Future<void> openSdkCameraScreen() async {
    try {
    
      await platform.invokeMethod('openCamera');
    } on PlatformException catch (e) {
      print("Failed to open SDK Camera screen: '${e.message}'.");
    }
  }

  Future<void> openSdkBodyPickerScreen() async {
    try {

      await platform.invokeMethod('openBodyPicker');
    } on PlatformException catch (e) {
      print("Failed to open SDK Body Picker screen: '${e.message}'.");
    }
  }

  Future<void> openSdkHelpScreen() async {
    try {
    
      await platform.invokeMethod('openHelpScreen');
    } on PlatformException catch (e) {
      print("Failed to open SDK Help screen: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Flutter App with SDK Integration'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
        
            children: <Widget>[
           
              const Padding(
                padding: EdgeInsets.only(bottom: 24.0, left: 16.0, right: 16.0),
                child: Text(
                  'Press on one of the options to launch the SDK screens',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 16.0, fontWeight: FontWeight.w500),
                ),
              ),

           
              ElevatedButton(
                onPressed: openSdkCameraScreen,
                child: const Text('Open SDK Camera'),
              ),

             
              const SizedBox(height: 16.0),

           
              ElevatedButton(
                onPressed: openSdkBodyPickerScreen,
                child: const Text('Open SDK Body Picker'),
              ),

            
              const SizedBox(height: 16.0),

             
              ElevatedButton(
                onPressed: openSdkHelpScreen,
                child: const Text('Open SDK Help screen'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}