import 'dart:collection';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:permission_handler/permission_handler.dart';

TextEditingController cCode = TextEditingController();
TextEditingController cMobile = TextEditingController();
TextEditingController cUserName = TextEditingController();
TextEditingController cEmail = TextEditingController();

class FlareAwareActivity extends StatefulWidget {
  const FlareAwareActivity({Key? key}) : super(key: key);

  @override
  State<FlareAwareActivity> createState() => _FlareAwareActivity();
}

class _FlareAwareActivity extends State<FlareAwareActivity> with WidgetsBindingObserver{
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
    WidgetsBinding.instance.addObserver(this);
  }
  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
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
                    child:const Text("Flare Aware Demo",style: TextStyle(fontSize: 20,color: Colors.black),)
                ),
                Center(
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Padding(
                            padding: const EdgeInsets.all(10),
                            child: SizedBox(
                              width: 200.0,
                              height: 50.0,
                              child: ElevatedButton(
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: Colors.white,
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
                                    callStartFlareAwareML();
                                  } else if (isConfigure == "false") {
                                    _showToast(
                                        context, "Please enter valid license key");
                                  }
                                },
                                child: pressStart
                                    ? const Text('START Flare Aware')
                                    : const Text('STOP Flare Aware'),
                              ),
                            )
                        )
                      ],
                    ),
                  ),
                )
              ])));
  }

  Future<void> callStartFlareAwareML() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("startFlareAwareML", <String, Object>{
      "isActive": pressStart
    });
    if (kDebugMode) {
      print(result.entries);
    }
    bool? isActive;
    if (Platform.isAndroid) {
      isActive = result["isActive"] as bool?;
    }else{
      if (result["success"] != null &&
          result["success"] == true) {
        var payload = result["payload"] as Map<Object?, Object?>;
        if (payload["isActive"]  != null) {
          isActive = payload["isActive"] as bool?;
        }
      }
    }

    if(isActive != null){
      if (isActive == true) {
        setState(() {
          pressStart = false;
        });
      } else {
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

    if (result[key] != null &&
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
