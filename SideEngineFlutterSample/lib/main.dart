import 'package:flutersideml/src/TestIncident.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'src/StandardThemeActivity.dart';
import 'src/CustomThemeActivity.dart';
import 'src/CustoMapScreen.dart';
import 'src/IncidentTimer.dart';
void main() {
  runApp(const MyApp());
}

late TextEditingController cCode = TextEditingController();
late TextEditingController cMobile = TextEditingController();
late TextEditingController cUserName = TextEditingController();

class MyApp extends StatelessWidget {
  static const channel = MethodChannel("com.sideml.flutersideml");
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter SideEngine Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      routes: {
        '/StandardThemeActivity': (context) => const StandardThemeActivity(),
        '/CustomThemeActivity': (context) => const CustomThemeActivity(),
        '/CustomMapScreen': (context) => const CustomMapScreen(),
        '/TestIncident':(context)=> const TestIncident(),
        '/IncidentTimer':(context)=> const IncidentTimer(),
      },
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  static const channel = MethodChannel("com.sideml.flutersideml");

  const HomePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          // Here we take the value from the MyHomePage object that was created by
          // the App.build method, and use it to set our appbar title.
          title: const Text('Flutter SideEngine Demo'),
        ),
        body: Center(
          child:
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Padding(
                  padding: const EdgeInsets.all(10),
                  child: Text('Welcome to Side Engine',
                      style: Theme.of(context).textTheme.titleLarge)
                ),
                Padding(
                  padding: const EdgeInsets.all(10),
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      primary: Colors.white, //background color of button
                      side: const BorderSide(width:2, color:Colors.redAccent), //border width and color
                      elevation: 3, //elevation of button
                      shape: RoundedRectangleBorder( //to set border radius to button
                          borderRadius: BorderRadius.circular(30)
                      ),
                      padding: const EdgeInsets.fromLTRB(50,18,50,18), //content padding inside button
                      foregroundColor: Colors.black,
                    ),
                    onPressed: () {
                      Navigator.pushNamed(context, '/StandardThemeActivity');
                      }, child: const Text('Standard Theme'),
                  ),
                ),
                Padding(
                    padding: const EdgeInsets.all(10),
                    child:
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                      primary: Colors.white, //background color of button
                      side: const BorderSide(width:2, color:Colors.redAccent), //border width and color
                      elevation: 3, //elevation of button
                      shape: RoundedRectangleBorder( //to set border radius to button
                          borderRadius: BorderRadius.circular(30)
                      ),
                        padding: const EdgeInsets.fromLTRB(50,18,50,18), //content padding inside button
                      foregroundColor: Colors.black,
                      ),
                      onPressed: () {
                        // callConfigure(context, true);
                        Navigator.pushNamed(context, '/CustomThemeActivity');
                        }, child: const Text('Custom Theme'),
                    )
                ),
              ],
            ),
          ),
        )
    );
  }
}