import 'dart:collection';
import 'dart:convert';

import 'package:flutersideml/src/IncidentTimer.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class TestIncident extends StatefulWidget {
  const TestIncident({Key? key}) : super(key: key);
  @override
  State<TestIncident> createState() => _TestIncident();
}

class _TestIncident extends State<TestIncident> {

  static const channel = MethodChannel("com.sideml.flutersideml");
  late BuildContext mContext;
  String email = '';
  String userName = '';
  bool isTestMode = false;

  @override
  void initState() {
    Future.delayed(const Duration(seconds: 1), () {
      final arg = ModalRoute
          .of(mContext)!
          .settings
          .arguments as TestIncidentScreenArguments;
      isTestMode = arg.isTestMode;
      email = arg.email;
      userName = arg.userName;
      callStartML(userName, email, true);
    });
    super.initState();
  }
  @override
  Widget build(BuildContext context) {
    mContext = context;
    return Scaffold(
      resizeToAvoidBottomInset: false,
      backgroundColor: const Color(0xff0a1f34),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[

          Container(
              margin: const EdgeInsets.only(top: 50, left: 8),
              alignment: Alignment.topRight,
              child: IconButton(
                onPressed: () {
                  //callForResumeSideEngine();
                  Navigator.pop(context);

                },
                icon: const Icon(Icons.close, color: Colors.white),
              )),


          Expanded(child:  Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                decoration: const BoxDecoration(color: Color(0x00000d05)),
                height: 80,
                width: 80,
                child: Image.asset('./assets/images/ic_shake_mobile.png',
                    color: Colors.white),
              ),
              Container(
                padding:  const EdgeInsets.all(14),

                  child: const Text(
                    'Shake your phone repeatedly to simulate an incident',
                    // style: Theme.of(context).textTheme.labelLarge
                    style: TextStyle(
                        fontSize: 24,
                        color: CupertinoColors.white,
                        fontWeight: FontWeight.bold,
                        fontFamily: AutofillHints.creditCardName),
                      textAlign: TextAlign.center )),
              Container(
                  padding:  const EdgeInsets.all(14),
                  child: const Text(
                    'This is in test mode don\'t worry it will not go off if you shake your phone in real life',
                    // style: Theme.of(context).textTheme.labelLarge
                    style:
                    TextStyle(fontSize: 16, color: CupertinoColors.white),
                      textAlign: TextAlign.center
                  )),
            ],
          ),

          ),
        ],
      ),
    );
  }

  Future<void> callStartML(String userName,String email, bool isTestMode) async {
    final LinkedHashMap<Object?,Object?> result = await channel.invokeMethod("startSideML",<String,Object>{
      "userName": userName,
      "email": email,
      "isTestMode": isTestMode,
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    if(result.entries.first.value != null &&
        result.entries.first.value == true){
      callSideEngineCallback();
    }
  }

  Future<void> callSideEngineCallback() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("incidentDetected");
    List keys = result.keys.toList();
    // List values = result.values.toList();
    if (keys.contains("response")) {
      var last = keys[keys.indexOf("response")];
      var encodedString = jsonEncode(result[last]);
      Map<String, dynamic> responseValue =
      json.decode(json.decode(encodedString));
      var customTheme = responseValue.entries.first.value;
      // developer.log('log me', name: '${result}');
      Navigator.pop(mContext);
      if (customTheme != null && customTheme == true) {
        callSideEngineCallback();
        Navigator.pushNamed(mContext,
            "/IncidentTimer", arguments: IncidentTimerScreenArguments(
                true,
                userName,
                email
            ));
      }
    }
  }

  Future<void> callForResumeSideEngine() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("resumeSideEngine");
    if (kDebugMode) {
      print(result.entries.first.value);
    }
  }
}

class TestIncidentScreenArguments {
  bool isTestMode;
  String userName;
  String email;

  TestIncidentScreenArguments(this.isTestMode,this.userName,this.email);
}

