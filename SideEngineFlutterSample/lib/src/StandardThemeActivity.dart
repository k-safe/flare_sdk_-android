import 'dart:collection';
import 'package:flutersideml/src/TestIncident.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';


late TextEditingController cCode = TextEditingController();
late TextEditingController cMobile = TextEditingController();
late TextEditingController cUserName = TextEditingController();
late TextEditingController cEmail = TextEditingController();
class StandardThemeActivity extends StatefulWidget {
  const StandardThemeActivity({Key? key}) : super(key: key);
  @override
  State<StandardThemeActivity> createState() => _StandardThemeActivity();
}

class _StandardThemeActivity extends State<StandardThemeActivity> {
  static const channel = MethodChannel("com.sideml.flutersideml");
  bool pressStart = true;
  String isConfigure = "";
  @override
  void initState() {
    callConfigure();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {

    return WillPopScope(
        onWillPop: () async =>false,
    child:Scaffold(
          resizeToAvoidBottomInset: false,
          body:Column(
            children: [
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
                  child:const Text("Standard theme",style: TextStyle(fontSize: 20,color: Colors.black),)
              ),
              Center(
                child:

                Padding(
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
                          onChanged: (code) {
                          },
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
                          onChanged: (mobile) {
                          },
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
                          onChanged: (mobile) {
                          },
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
                          onChanged: (mobile) {
                          },
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

                          child: const Text('Press start to activate Incident detection in live mode',
                              style: TextStyle(fontSize: 13,color: Colors.black))
                      ),
                      Padding(
                          padding: const EdgeInsets.all(10),
                          child:SizedBox(
                            width: 200.0,
                            height: 50.0,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                primary: Colors.white, //background color of button
                                side: const BorderSide(width:2, color:Colors.redAccent), //border width and color
                                elevation: 3, //elevation of button
                                shape: RoundedRectangleBorder( //to set border radius to button
                                    borderRadius: BorderRadius.circular(30)
                                ),
                                // padding: const EdgeInsets.fromLTRB(100,18,100,18), //content padding inside button
                                foregroundColor: Colors.black,
                              ),
                              onPressed: () {
                                callStartML(cUserName.text,cEmail.text, false);
                              },
                              child:pressStart? const Text('Start') : const Text('Stop'),
                            ),
                          )
                      ),
                       const Text('Press button below to activate test mode, then shake your phone repeatedly until a test incident triggers',
                              style: TextStyle(fontSize: 13,color: Colors.black) ,textAlign: TextAlign.center),
                      Padding(
                          padding: const EdgeInsets.all(10),
                          child:SizedBox(
                              width: 200.0,
                              height: 50.0,
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
                                  if(isConfigure.isEmpty){
                                    _showToast(context, "Please wait...");
                                  }else if(isConfigure == "true"){
                                    if(!pressStart){
                                      callStartML(cUserName.text, cEmail.text, false);
                                    }
                                    Navigator.pushNamed(
                                        context,
                                        '/TestIncident',
                                        arguments: TestIncidentScreenArguments(
                                            true,
                                            cUserName.text,
                                            cEmail.text
                                        ));
                                  }else if(isConfigure == "false"){
                                    _showToast(context, "Please enter valid license key");
                                  }
                                }, child: const Text('Test Incident'),
                              )
                          )
                      )
                    ],
                  ),
                ),
              )
            ],
          )

      )
    );
}

  void _onBackPressed() async {
    cCode.clear();
    cMobile.clear();
    cUserName.clear();
    cEmail.clear();
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
      if(!isTestMode){
        setState(() {
          pressStart = false;
        });
      }

    }else{
      if(!isTestMode) {
        setState(() {
          pressStart = true;
        });
      }
    }
  }

  Future<void> callConfigure() async {
    final LinkedHashMap<Object?,Object?> result =
    await channel.invokeMethod("configure",<String,Object>{
      "isCustom": false,
    });
    if (kDebugMode) {
      print(result.entries.first.value);
    }
    if(result.entries.first.value != null &&
        result.entries.first.value == true){
      setState(() {
        isConfigure = "true";
      });
    }else{
      setState(() {
        isConfigure = "false";
      });
    }
  }
  void _showToast(BuildContext context,String text) {
    Fluttertoast.showToast(
      msg: text,
      toastLength: Toast.LENGTH_SHORT,
    );
  }
}