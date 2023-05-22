import 'dart:collection';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:share/share.dart';
import 'package:permission_handler/permission_handler.dart';

late TextEditingController cUserName = TextEditingController();

class EmergencySOSActivity extends StatefulWidget {
  const EmergencySOSActivity({Key? key}) : super(key: key);

  @override
  State<EmergencySOSActivity> createState() => _EmergencySOSActivity();
}

class _EmergencySOSActivity extends State<EmergencySOSActivity> with WidgetsBindingObserver{
  static const channel = MethodChannel("com.sideml.flutersideml");
  bool pressStart = true;
  String isConfigure = "";
  String mode = '';
  String lic = '';
  String sosLiveTrackingUrl = '';

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
                        cUserName.clear();
                        if(pressStart == false){
                          callSOSML(cUserName.text, true);
                        }
                      },
                      icon: const Icon(Icons.close, color: Colors.black),
                    )),
                Container(
                    margin: const EdgeInsets.only(top: 20),
                    alignment: Alignment.center,
                    child:const Text("Emergency SOS",style: TextStyle(fontSize: 20,color: Colors.black),)
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
                                    callSOSML(cUserName.text, false);
                                    // Navigator.pushNamed(context, '/IncidentTimer');
                                  } else if (isConfigure == "false") {
                                    _showToast(
                                        context, "Please enter valid license key");
                                  }
                                  // Navigator.pushNamed(context, '/StandardThemeActivity');
                                },
                                child: pressStart
                                    ? const Text('Activate SOS')
                                    : const Text('Deactivate SOS'),
                              ),
                            )
                        ),
                  Opacity( opacity: pressStart ? 0.0: 1 ,
                      child:Padding(
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
                                  Share.share(sosLiveTrackingUrl);
                                },
                                child: const Text('Share sos link'),
                              ),
                            )
                        ))
                      ],
                    ),
                  ),
                )
              ])));
  }

  Future<void> callSOSML(String userName, bool isClose) async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("startSOSML", <String, Object>{
      "userName": userName,
      "isActive": pressStart
    });
    if (kDebugMode) {
      print(result.entries);
    }
    if(isClose){
      return;
    }
    List keys = result.keys.toList();
    print("ERROR⚠️|️" + "SOS: " + ": " + 'keys: $keys');
    if (Platform.isAndroid) {
      bool? sosActive = result["sosActive"] as bool?;
      print("ERROR⚠️|️" + "SOS: " + ": " + 'sosActive: $sosActive');
      if (keys.indexOf("response") >= 0) {
        var last = keys[keys.indexOf("response")];
        var encodedString = jsonEncode(result[last]);
        print("ERROR⚠️|️" + "SOS: " + ": " + 'encodedString: $encodedString');
        Map<String, dynamic> responseValue =
        json.decode(json.decode(encodedString));
        List valueRes = responseValue.values.toList();
        List keyRes = responseValue.keys.toList();
        if (keyRes.indexOf("sosLiveTrackingUrl") >= 0) {
          sosLiveTrackingUrl = valueRes[keyRes.indexOf("sosLiveTrackingUrl")];
        }
      }
      if (keys.indexOf("sosActive") >= 0) {
        if (sosActive == false) {
          setState(() {
            pressStart = true;
          });
        } else {
          setState(() {
            pressStart = false;
          });
        }
      }
    }else{
      if (result["success"] != null &&
          result["success"] == true) {
        print("ERROR⚠️|️" + "SOS: " + ": " + 'result["type"]: $result["type"]');
        var payload = result["payload"] as Map<Object?, Object?>;
        sosLiveTrackingUrl = payload["surveyVideoURL"] as String;
        print("ERROR⚠️|️" + "SOS: " + ": " + 'surveyVideoURL: $sosLiveTrackingUrl');
      }
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
    String key = "success";
    if (Platform.isAndroid) {
      key = "isConfigure";
    }

    List keys = result.keys.toList();

    if (keys.indexOf("sosActive") >= 0) {
      permission();
      setState(() {
        isConfigure = "true";
      });
      bool? sosActive;
      if (Platform.isAndroid) {
        sosActive = result["sosActive"] as bool?;
        print("ERROR⚠️|️" + "SOS: " + ": " + 'sosActive: $sosActive');
        if (keys.indexOf("response") >= 0) {
          var last = keys[keys.indexOf("response")];
          var encodedString = jsonEncode(result[last]);
          print("ERROR⚠️|️" + "SOS: " + ": " + 'encodedString: $encodedString');
          Map<String, dynamic> responseValue =
          json.decode(json.decode(encodedString));
          List valueRes = responseValue.values.toList();
          List keyRes = responseValue.keys.toList();
          if (keyRes.indexOf("sosLiveTrackingUrl") >= 0) {
            sosLiveTrackingUrl = valueRes[keyRes.indexOf("sosLiveTrackingUrl")];
          }
        }
      }else{
        if (result["success"] != null &&
            result["success"] == true) {
          print("ERROR⚠️|️" + "SOS: " + ": " + 'result["type"]: $result["type"]');
          var payload = result["payload"] as Map<Object?, Object?>;
          sosLiveTrackingUrl = payload["surveyVideoURL"] as String;
          sosActive = payload["sosActive"] as bool;
          print("ERROR⚠️|️" + "SOS: " + ": " + 'surveyVideoURL: $sosLiveTrackingUrl');
        }
      }
      if(sosActive == false){
        setState(() {
          pressStart = true;
        });
      }else{
        setState(() {
          pressStart = false;
        });
      }
    }else if (result[key] != null &&
        result[key] == true) {
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
