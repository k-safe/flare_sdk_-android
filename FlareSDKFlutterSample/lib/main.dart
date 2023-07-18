import 'package:flutersideml/src/EmergencySOSActivity.dart';
import 'package:flutersideml/src/FlareAwareActivity.dart';
import 'package:flutersideml/src/SurveyVideoScreen.dart';
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
String selectedMode = Constants.ENVIRONMENT_SANDBOX;
String productionLicense = "8b53824f-ed7a-4829-860b-f6161c568fad";
String sandboxLicense = "9518a8f7-a55f-41f4-9eaa-963bdb1fce5f";
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
        '/EmergencySOSActivity': (context) => const EmergencySOSActivity(),
        '/FlareAwareActivity': (context) => const FlareAwareActivity(),
        '/TestIncident':(context)=> const TestIncident(),
        '/IncidentTimer':(context)=> const IncidentTimer(),
        '/SurveyVideoScreen':(context)=> const SurveyVideoScreen(),
      },
      home: const HomePage(),
    );
  }
}
class Constants {
  static const String ENVIRONMENT_PRODUCTION = 'production';
  static const String ENVIRONMENT_SANDBOX = 'sandbox';
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
                RadioButtonGroup(),
                Padding(
                  padding: const EdgeInsets.all(10),
                  child:

                  SizedBox(
                    width: 230, // Set the desired fixed width
                    child:
                      ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          primary: Colors.white, //background color of button
                          side: const BorderSide(width:2, color:Colors.redAccent), //border width and color
                          elevation: 3, //elevation of button
                          shape: RoundedRectangleBorder( //to set border radius to button
                              borderRadius: BorderRadius.circular(30)
                          ),
                          padding: const EdgeInsets.fromLTRB(0,18,0,18), //content padding inside button
                          foregroundColor: Colors.black,
                        ),
                        onPressed: () {
                          Navigator.pushNamed(context, '/StandardThemeActivity',arguments: {
                            "mode": selectedMode,
                            "lic":
                            (selectedMode == Constants.ENVIRONMENT_SANDBOX)
                                ? sandboxLicense : productionLicense
                          });
                          }, child: const Text('Standard Theme'),
                      ),
                  )
                ),
                Padding(
                    padding: const EdgeInsets.all(10),
                    child:
                    SizedBox(
                        width: 230, // Set the desired fixed width
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
                              Navigator.pushNamed(context, '/CustomThemeActivity',arguments: {
                                "mode": selectedMode,
                                "lic":
                                (selectedMode == Constants.ENVIRONMENT_SANDBOX)
                                    ? sandboxLicense : productionLicense
                              });
                              }, child: const Text('Custom Theme'),
                          )
                    )
                ),
                Padding(
                    padding: const EdgeInsets.all(10),
                    child:
                    SizedBox(
                        width: 230, // Set the desired fixed width
                        child:
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              primary: Colors.white, //background color of button
                              side: const BorderSide(width:2, color:Colors.redAccent), //border width and color
                              elevation: 3, //elevation of button
                              shape: RoundedRectangleBorder( //to set border radius to button
                                  borderRadius: BorderRadius.circular(30)
                              ),
                              padding: const EdgeInsets.fromLTRB(0,18,0,18), //content padding inside button
                              foregroundColor: Colors.black,
                            ),
                            onPressed: () {
                              // callConfigure(context, true);
                              Navigator.pushNamed(context, '/EmergencySOSActivity',arguments: {
                                "mode": Constants.ENVIRONMENT_PRODUCTION,
                                "lic": productionLicense
                              });
                            }, child: const Text('Emergency SOS'),
                          )
                    )
                ),
                Padding(
                    padding: const EdgeInsets.all(10),
                    child:
                    SizedBox(
                        width: 230, // Set the desired fixed width
                        child:
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              primary: Colors.white, //background color of button
                              side: const BorderSide(width:2, color:Colors.redAccent), //border width and color
                              elevation: 3, //elevation of button
                              shape: RoundedRectangleBorder( //to set border radius to button
                                  borderRadius: BorderRadius.circular(30)
                              ),
                              padding: const EdgeInsets.fromLTRB(0,18,0,18), //content padding inside button
                              foregroundColor: Colors.black,
                            ),
                            onPressed: () {
                              // callConfigure(context, true);
                              Navigator.pushNamed(context, '/FlareAwareActivity',arguments: {
                                "mode": Constants.ENVIRONMENT_PRODUCTION,
                                "lic": productionLicense
                              });
                            }, child: const Text('Enable Flare Aware'),
                          )
                    )
                )
              ],
            ),
          ),
        )
    );
  }
}
class RadioButtonGroup extends StatefulWidget {
  @override
  _RadioButtonGroupState createState() => _RadioButtonGroupState();
}

class _RadioButtonGroupState extends State<RadioButtonGroup> {
  @override
  Widget build(BuildContext context) {
    return
    Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Padding(
            padding: const EdgeInsets.fromLTRB(50,0,0,0),
            child:
              RadioListTile<String>(
                title: const Text('Sandbox Mode'),
                value: Constants.ENVIRONMENT_SANDBOX,
                groupValue: selectedMode,
                onChanged: (value) {
                  setState(() {
                    selectedMode = value!;
                  });
                },
              )
        ),
        Padding(
            padding: const EdgeInsets.fromLTRB(50,0,0,0),
            child:
              RadioListTile<String>(
                title: const Text('Production Mode'),
                value: Constants.ENVIRONMENT_PRODUCTION,
                groupValue: selectedMode,
                onChanged: (value) {
                  setState(() {
                    selectedMode = value!;
                  });
                },
              )
        ),
      ]);
  }
}