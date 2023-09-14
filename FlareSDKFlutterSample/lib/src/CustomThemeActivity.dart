import 'dart:collection';
import 'dart:convert';
import 'dart:io' show Platform;

import 'package:flutersideml/src/IncidentTimer.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:permission_handler/permission_handler.dart';

late TextEditingController cCode = TextEditingController();
late TextEditingController cMobile = TextEditingController();
late TextEditingController cUserName = TextEditingController();
late TextEditingController cEmail = TextEditingController();

class CustomThemeActivity extends StatefulWidget {
  const CustomThemeActivity({Key? key}) : super(key: key);

  @override
  State<CustomThemeActivity> createState() => _CustomThemeActivity();
}

class _CustomThemeActivity extends State<CustomThemeActivity>
    with WidgetsBindingObserver {
  static const channel = MethodChannel("com.sideml.flutersideml");
  bool pressStart = true;
  String isConfigure = "";
  String mode = '';
  String lic = '';

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    retrieveArguments();
  }

  void retrieveArguments() {
    final args =
        ModalRoute.of(context)!.settings.arguments as Map<String, dynamic>;
    mode = args['mode'];
    lic = args['lic'];
    callConfigure();
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance!.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance!.removeObserver(this);
    super.dispose();
  }

  void _showActivityDialog(BuildContext context) {
    if (pressStart) {
      showModalBottomSheet(
        context: context,
        builder: (BuildContext context) {
          return Container(
            margin: EdgeInsets.only(top: 20.0, bottom: 16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
                children: [
                const Text('Select Activity',
                    style: TextStyle(
                      color: Colors.black54,
                      // Change the color to your desired color
                      fontSize: 20,
                      // Change the font size to your desired size
                      fontWeight: FontWeight
                          .bold, // Change the font weight to your desired style
                      // You can add more style properties as needed
                    )),
                Divider(),
                ListTile(
                    // contentPadding:EdgeInsets.fromLTRB(0, 10, 0, 0),
                 title: Center(
                    child: Text('Bike',
                        style: TextStyle(
                          color: Colors.blue,
                          // Change the color to your desired color
                          fontSize: 18,
                          // Change the font size to your desired size
                          fontWeight: FontWeight
                              .bold, // Change the font weight to your desired style
                          // You can add more style properties as needed
                        )),
                  ),
                  onTap: () {
                    callStartML(cUserName.text, cEmail.text, 'Bike');
                    Navigator.pop(context);
                  },
                ),
                Divider(),
                ListTile(
                  title: Center(
                    child: Text('Scooter',
                        style: TextStyle(
                          color: Colors.blue,
                          // Change the color to your desired color
                          fontSize: 18,
                          // Change the font size to your desired size
                          fontWeight: FontWeight
                              .bold, // Change the font weight to your desired style
                          // You can add more style properties as needed
                        )),
                  ),
                  onTap: () {
                    callStartML(cUserName.text, cEmail.text, 'Scooter');
                    Navigator.pop(context);
                  },
                ),
                Divider(),
                ListTile(
                  title: Center(
                    child: Text('Cycling',
                        style: TextStyle(
                          color: Colors.blue,
                          // Change the color to your desired color
                          fontSize: 18,
                          // Change the font size to your desired size
                          fontWeight: FontWeight
                              .bold, // Change the font weight to your desired style
                          // You can add more style properties as needed
                        )),
                  ),
                  onTap: () {
                    callStartML(cUserName.text, cEmail.text, 'Cycling');
                    Navigator.pop(context);
                  },
                ),
                Divider(),
                ListTile(
                  title: Center(
                    child: Text('Cancel',
                        style: TextStyle(
                          color: Colors.blue,
                          // Change the color to your desired color
                          fontSize: 20,
                          // Change the font size to your desired size
                          fontWeight: FontWeight
                              .bold, // Change the font weight to your desired style
                          // You can add more style properties as needed
                        )),
                  ),
                  onTap: () {
                    Navigator.pop(context);
                  },
                ),
              ],
            ),
          );
        },
      );
    } else {
      stopSideEngine();
    }
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
        onWillPop: () async => false,
        child: Scaffold(
            resizeToAvoidBottomInset: false,
            body: Column(children: [
              Container(
                  margin: const EdgeInsets.only(left: 8, top: 50),
                  alignment: Alignment.topLeft,
                  child: IconButton(
                    onPressed: () {
                      if (!pressStart) {
                        stopSideEngine();
                      }
                      Navigator.pop(context);
                      cCode.clear();
                      cMobile.clear();
                      cUserName.clear();
                      cEmail.clear();
                    },
                    icon: const Icon(Icons.close, color: Colors.black),
                  )),
              Container(
                  margin: const EdgeInsets.only(top: 20),
                  alignment: Alignment.center,
                  child: const Text(
                    "Custom Theme Demo",
                    style: TextStyle(fontSize: 20, color: Colors.black),
                  )),
              Center(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(10),
                        child: TextField(
                          keyboardType: TextInputType.phone,
                          inputFormatters: [
                            FilteringTextInputFormatter.digitsOnly
                          ],
                          controller: cCode,
                          autofocus: true,
                          textCapitalization: TextCapitalization.words,
                          onChanged: (code) {},
                          decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                              hintText: 'Country Calling Code',
                              contentPadding: EdgeInsets.all(20.0)),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(10),
                        child: TextField(
                          keyboardType: TextInputType.phone,
                          inputFormatters: [
                            FilteringTextInputFormatter.digitsOnly
                          ],
                          controller: cMobile,
                          textCapitalization: TextCapitalization.words,
                          onChanged: (mobile) {},
                          decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                              hintText: 'Emergency Contact Number',
                              contentPadding: EdgeInsets.all(20.0)),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(10),
                        child: TextField(
                          keyboardType: TextInputType.text,
                          // inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                          controller: cUserName,
                          textCapitalization: TextCapitalization.words,
                          onChanged: (mobile) {},
                          decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                              hintText: 'Rider Name',
                              contentPadding: EdgeInsets.all(20.0)),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(10),
                        child: TextField(
                          keyboardType: TextInputType.text,
                          // inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                          controller: cEmail,
                          textCapitalization: TextCapitalization.words,
                          onChanged: (mobile) {},
                          decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                              hintText: 'Emergency Email',
                              contentPadding: EdgeInsets.all(20.0)),
                        ),
                      ),
                      Container(
                          alignment: Alignment.center,
                          transformAlignment: Alignment.center,
                          margin: const EdgeInsets.only(left: 20, right: 10),
                          child: const Text(
                              'Press button below to activate SIDE engine, then shake your phone repeatedly until an incident triggers',
                              style:
                                  TextStyle(fontSize: 13, color: Colors.black),
                              textAlign: TextAlign.center)),
                      Padding(
                          padding: const EdgeInsets.all(10),
                          child: SizedBox(
                            width: 200.0,
                            height: 50.0,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                primary: Colors.white,
                                //background color of button
                                side: const BorderSide(
                                    width: 2, color: Colors.redAccent),
                                //border width and color
                                elevation: 3,
                                //elevation of button
                                shape: RoundedRectangleBorder(
                                    //to set border radius to button
                                    borderRadius: BorderRadius.circular(30)),
                                // padding: const EdgeInsets.fromLTRB(100,18,100,18), //content padding inside button
                                foregroundColor: Colors.black,
                              ),
                              onPressed: () {
                                if (isConfigure.isEmpty) {
                                  _showToast(context, "Please wait...");
                                } else if (isConfigure == "true") {
                                  if (kDebugMode) {
                                    print(cEmail.text);
                                  }
                                  // Show the bottom sheet dialog on 'Start' button press
                                  // Remove keyboard focus when the button is pressed
                                  FocusScope.of(context).unfocus();
                                  _showActivityDialog(context);

                                  // Navigator.pushNamed(context, '/IncidentTimer');
                                } else if (isConfigure == "false") {
                                  _showToast(context,
                                      "Please enter valid license key");
                                }
                                // Navigator.pushNamed(context, '/StandardThemeActivity');
                              },
                              child: pressStart
                                  ? const Text('Start')
                                  : const Text('Stop'),
                            ),
                          )),
                    ],
                  ),
                ),
              )
            ])));
  }

  Future<void> callStartML(
      String userName, String email, String activityType) async {
    final LinkedHashMap<Object?, Object?> result =
        await channel.invokeMethod("startSideML", <String, Object>{
      "userName": userName,
      "email": email,
      "isStarted": pressStart,
      "activityType": activityType
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    String key = "success";
    if (Platform.isAndroid) {
      key = "isServiceStart";
    }
    if (result[key] != null && result[key] == true && pressStart) {
      callSideEngineCallback();
      setState(() {
        pressStart = false;
      });
    } else {
      setState(() {
        pressStart = true;
      });
    }
  }

  Future<void> callConfigure() async {
    final LinkedHashMap<Object?, Object?> result = await channel.invokeMethod(
        "configure",
        <String, Object>{"isCustom": true, "mode": mode, "lic": lic});
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    String key = "success";
    if (Platform.isAndroid) {
      key = "isConfigure";
    }
    if (result[key] != null && result[key] == true) {
      permission();
      setState(() {
        isConfigure = "true";
      });
    } else {
      setState(() {
        isConfigure = "false";
      });
    }
  }

  Future<void> stopSideEngine() async {
    final LinkedHashMap<Object?, Object?> result = await channel.invokeMethod(
        "stopSideML",
        <String, Object>{"userName": "", "email": "", "isStarted": pressStart});
    if (kDebugMode) {
      print('stopEngine: ${result["type"]}');
    }
  }

  Future<void> callSideEngineCallback() async {
    final LinkedHashMap<Object?, Object?> result =
        await channel.invokeMethod("incidentDetected");
    if (kDebugMode) {
      print("ERROR⚠️|️" + "callSideEngineCallback : " + ": $result");
    }
    if (Platform.isAndroid) {
      List keys = result.keys.toList();
      // List values = result.values.toList();
      if (keys.indexOf("response") >= 0) {
        var last = keys[keys.indexOf("response")];
        var encodedString = jsonEncode(result[last]);
        Map<String, dynamic> responseValue =
            json.decode(json.decode(encodedString));
        var customTheme = responseValue.entries.first.value;
        // developer.log('log me', name: '${result}');
        List valueRes = responseValue.values.toList();
        List keyRes = responseValue.keys.toList();

        print("ERROR⚠️|️" +
            "callSideEngineCallback customTheme: " +
            ": $customTheme");
        if (customTheme != null && customTheme == true) {
          if (keyRes.indexOf("isAppInBackground") >= 0) {
            var isAppInBackground =
                valueRes[keyRes.indexOf("isAppInBackground")];
            print("ERROR⚠️|️" +
                "callSideEngineCallback isAppInBackground: " +
                ": $isAppInBackground");

            if (isAppInBackground) {
              await channel.invokeMethod(
                  "customPartnerNotify", <String, Object>{
                "userName": cUserName.text,
                "email": cEmail.text
              });
            } else {
              Navigator.pushNamed(context, "/IncidentTimer",
                  arguments: IncidentTimerScreenArguments(
                      false, cUserName.text, cEmail.text));
            }
          } else {
            Navigator.pushNamed(context, "/IncidentTimer",
                arguments: IncidentTimerScreenArguments(
                    false, cUserName.text, cEmail.text));
          }
          callSideEngineCallback();
        }
      }
    } else {
      if (result["success"] != null && result["success"] == true) {
        Navigator.pushNamed(context, "/IncidentTimer",
            arguments: IncidentTimerScreenArguments(
                false, cUserName.text, cEmail.text));
      }
    }
  }

  Future<void> permission() async {
    PermissionStatus status = await Permission.location.request();

    if (status.isGranted) {
      // Permission granted, proceed with location-related operations
    } else if (status.isDenied) {
      // Permission denied
    } else if (status.isPermanentlyDenied) {
      // Permission permanently denied, navigate to app settings
    } else if (status.isRestricted) {
      // Permission is restricted on this device
    }
  }

  void _showToast(BuildContext context, String text) {
    Fluttertoast.showToast(
      msg: text,
      toastLength: Toast.LENGTH_SHORT,
    );
  }
}
