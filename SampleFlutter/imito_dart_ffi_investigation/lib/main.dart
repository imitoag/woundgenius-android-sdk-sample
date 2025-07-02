import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  static const platform = const MethodChannel('your_sdk_channel');


  Future<void> openSdkScreen() async {
    try {
      await platform.invokeMethod('open');
    } on PlatformException catch (e) {
      print("Failed to open SDK screen: '${e.message}'.");
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
              ElevatedButton(
                onPressed: () {
                  openSdkScreen(); // Открытие первого экрана SDK
                },
                child: Text('Open SDK Screen'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
