import 'dart:collection';
import 'dart:convert';

import 'package:flutersideml/src/IncidentTimer.dart';
import 'package:flutersideml/src/TestIncident.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';

late TextEditingController cCode = TextEditingController();
late TextEditingController cMobile = TextEditingController();
late TextEditingController cUserName = TextEditingController();
late TextEditingController cEmail = TextEditingController();

class CustomThemeActivity extends StatefulWidget {
  const CustomThemeActivity({Key? key}) : super(key: key);

  @override
  State<CustomThemeActivity> createState() => _CustomThemeActivity();
}

class _CustomThemeActivity extends State<CustomThemeActivity> {
  static const channel = MethodChannel("com.sideml.flutersideml");
  bool pressStart = true;
  String isConfigure = "";
  bool isTestMode = false;

  @override
  void initState() {
    super.initState();
    callConfigure();
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
                    child:const Text("Custom theme",style: TextStyle(fontSize: 20,color: Colors.black),)
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
                                hintText: 'Country Code',
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
                                hintText: 'Mobile',
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
                                hintText: 'UserName',
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
                                hintText: 'Email',
                                contentPadding: EdgeInsets.all(20.0)),
                          ),
                        ),
                        Container(
                            alignment: Alignment.center,
                            transformAlignment: Alignment.center,
                            margin: const EdgeInsets.only(left: 20,right: 10),
                            child: const Text(
                                'Press start to activate Incident detection in live mode',
                                style: TextStyle(fontSize: 13,color: Colors.black))),
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
                                    isTestMode = false;
                                    callStartML(cUserName.text, cEmail.text, false);
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
                            )),
                        const Text(
                            'Press button below to activate test mode, then shake your phone repeatedly until a test incident triggers',
                            style: TextStyle(fontSize: 13,color: Colors.black),textAlign: TextAlign.center),
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
                                    padding:
                                    const EdgeInsets.fromLTRB(50, 18, 50, 18),
                                    //content padding inside button
                                    foregroundColor: Colors.black,
                                  ),
                                  onPressed: () {
                                    if (isConfigure.isEmpty) {
                                      _showToast(context, "Please wait...");
                                    } else if (isConfigure == "true") {
                                      if(!pressStart){
                                        if (kDebugMode) {
                                          print(cEmail.text);
                                        }
                                        isTestMode = true;
                                        callStartML(cUserName.text,cEmail.text, true);
                                      }
                                      Navigator.pushNamed(
                                          context,
                                          '/TestIncident',
                                          arguments: TestIncidentScreenArguments(
                                              true,
                                              cUserName.text,
                                              cEmail.text
                                          ));
                                    } else if (isConfigure == "false") {
                                      _showToast(
                                          context, "Please enter valid license key");
                                    }
                                  },
                                  child: const Text('Test Incident'),
                                )))
                      ],
                    ),
                  ),
                )
              ])));
  }

  Future<void> callStartML(String userName,String email, bool isTestMode) async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("startSideML", <String, Object>{
      "userName": userName,
      "email": email,
      "isTestMode": isTestMode,
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    if (result.entries.first.value != null &&
        result.entries.first.value == true) {
      callSideEngineCallback();
      if (!isTestMode) {
        setState(() {
          pressStart = false;
        });
      }
    } else {
      if (!isTestMode) {
        setState(() {
          pressStart = true;
        });
      }
    }
  }

  Future<void> callConfigure() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("configure", <String, Object>{
      "isCustom": true,
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    if (result.entries.first.value != null &&
        result.entries.first.value == true) {
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
              "email": cEmail.text,
              "isTestMode": isTestMode,
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

  void _showToast(BuildContext context, String text) {
    Fluttertoast.showToast(
      msg: text,
      toastLength: Toast.LENGTH_SHORT,
    );
  }
}
