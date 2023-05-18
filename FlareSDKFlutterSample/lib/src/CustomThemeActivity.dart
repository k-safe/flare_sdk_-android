import 'dart:collection';
import 'dart:convert';

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

class _CustomThemeActivity extends State<CustomThemeActivity> with WidgetsBindingObserver{
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
    final args = ModalRoute.of(context)!.settings.arguments as Map<String, dynamic>;
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
  @override
  Widget build(BuildContext context) {
    return
      WillPopScope(
          onWillPop: () async =>false,
          child:Scaffold(
              resizeToAvoidBottomInset: false,
              body: Column(children: [
                Container(
                    margin: const EdgeInsets.only(left: 8, top: 50),
                    alignment: Alignment.topLeft,
                    child: IconButton(
                      onPressed: () {
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
                    child:const Text("Custom Theme Demo",style: TextStyle(fontSize: 20,color: Colors.black),)
                ),
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
                            inputFormatters: [FilteringTextInputFormatter.digitsOnly],
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
                            inputFormatters: [FilteringTextInputFormatter.digitsOnly],
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
                            margin: const EdgeInsets.only(left: 20,right: 10),
                            child: const Text(
                                'Press button below to activate SIDE engine, then shake your phone repeatedly until an incident triggers',
                                style: TextStyle(fontSize: 13,color: Colors.black),
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
                                    callStartML(cUserName.text, cEmail.text);
                                    // Navigator.pushNamed(context, '/IncidentTimer');
                                  } else if (isConfigure == "false") {
                                    _showToast(
                                        context, "Please enter valid license key");
                                  }
                                  // Navigator.pushNamed(context, '/StandardThemeActivity');
                                },
                                child: pressStart
                                    ? const Text('Start')
                                    : const Text('Stop'),
                              ),
                            ))
                      ],
                    ),
                  ),
                )
              ])));
  }

  Future<void> callStartML(String userName,String email) async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("startSideML", <String, Object>{
      "userName": userName,
      "email": email
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    if (result.entries.first.value != null &&
        result.entries.first.value == true) {
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
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("configure", <String, Object>{
      "isCustom": true,
      "mode": mode,
      "lic": lic
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    if (result.entries.first.value != null &&
        result.entries.first.value == true) {
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

  Future<void> callSideEngineCallback() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("incidentDetected");
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
      if (customTheme != null && customTheme == true) {
        if(keyRes.indexOf("isAppInBackground") >= 0) {
          var isAppInBackground = valueRes[keyRes.indexOf("isAppInBackground")];
          if(isAppInBackground){
            await channel.invokeMethod("customPartnerNotify", <String, Object>{
              "userName": cUserName.text,
              "email": cEmail.text
            });
          }else{
            Navigator.pushNamed(context,
                "/IncidentTimer", arguments: IncidentTimerScreenArguments(false, cUserName.text,cEmail.text));
          }
        }else{
          Navigator.pushNamed(context,
              "/IncidentTimer", arguments: IncidentTimerScreenArguments(false, cUserName.text,cEmail.text));
        }
        callSideEngineCallback();
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
