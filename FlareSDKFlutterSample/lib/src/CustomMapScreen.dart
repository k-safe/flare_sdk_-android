import 'dart:collection';
import 'dart:convert';
import 'dart:io';

import 'package:flutersideml/src/SurveyVideoScreen.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:url_launcher/url_launcher.dart';

class CustomMapScreen extends StatefulWidget {
  const CustomMapScreen({Key? key}) : super(key: key);

  @override
  State<CustomMapScreen> createState() => _CustomMapScreen();
}


class _CustomMapScreen extends State<CustomMapScreen> {
  static const channel = MethodChannel("com.sideml.flutersideml");
  late GoogleMapController googleMapController;


  static const CameraPosition initialCameraPosition =
      CameraPosition(target: LatLng(23.012249, 72.514431), zoom: 10);

  Set<Marker> markers = {};
  String words = "";
  String map = "";
  double? latitude = 0.0;
  double? longitude = 0.0;
  String email = '';
  String userName = '';
  bool isTestMode = false;
  late BuildContext mContext;
  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    Future.delayed(const Duration(seconds: 1), () {
      final arg = ModalRoute
          .of(mContext)!
          .settings
          .arguments as CustomMapScreenArguments;
      isTestMode = arg.isTestMode;
      email = arg.email;
      userName = arg.userName;
      callForTimerFinishSideEngine(userName, email, isTestMode);
    });

    getUserCurrentLocation().then((value) async {
      markers.clear();
      markers.add(Marker(
          markerId: const MarkerId('currentLocation'),
          position: LatLng(value.latitude, value.longitude)));
      setState(() {});
    });

  }
// created method for getting user current location
  Future<Position> getUserCurrentLocation() async {
    // await Geolocator.requestPermission().then((value){
    //
    // }).onError((error, stackTrace) async {
    //   await Geolocator.requestPermission();
    //   if (kDebugMode) {
    //     print("ERROR"+error.toString());
    //   }
    // });
    return await Geolocator.getCurrentPosition();
  }
  @override
  Widget build(BuildContext context) {
    mContext = context;
    return Scaffold(
        resizeToAvoidBottomInset: false,
        body:
        SingleChildScrollView(
        child:
        Column(children: [
          Container(
              margin: const EdgeInsets.only(left: 8, top: 50),
              alignment: Alignment.topRight,
              child: IconButton(
                onPressed: () {
                  callForCheckSurveyUrl(context);
                },
                icon: const Icon(Icons.close, color: Colors.black),
              )),
          SizedBox(
            width: double.infinity,
            height: 500,
            child: GoogleMap(
              initialCameraPosition: initialCameraPosition,
              markers: markers,
              zoomControlsEnabled: false,
              mapType: MapType.normal,
              onMapCreated: (GoogleMapController controller) {
                googleMapController = controller;
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.only(bottom: 10,top: 20,left: 20),
            alignment: Alignment.topLeft,
            child: const Text(
              'Your Location :',
              style: TextStyle(
              fontSize: 15,
                color: Colors.black,
                  fontWeight: FontWeight.bold,
              ),
            ),
          ),
          Container(
            padding: const EdgeInsets.only(bottom: 10,top: 10,left: 20,right: 10),
            alignment: Alignment.topLeft,
            child: const Text(
              'This location of your incident is displayed here for your reference',style: TextStyle(fontSize: 14,color: Colors.black)
              ),
            ),
          Container(
            padding: const EdgeInsets.only(bottom: 10,top: 10,left: 20),
            alignment: Alignment.topLeft,
            child: InkWell(
                child: Text(words,style: const TextStyle(fontSize: 20,color: Colors.red)),

                onTap: () => {
                  if(map != ""){
                    launch(map)
                  }
                }),
          ),
          Container(
            padding: const EdgeInsets.only(bottom: 10,top: 10,left: 20),
            alignment: Alignment.topLeft,
            child: Text(
              'Latitude: $latitude Longitude: $longitude',style: const TextStyle(fontSize: 14,color: Colors.black),
            ),

          ),
        ])
        )
    );
  }

  Future<void> callForResumeSideEngine() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("resumeSideEngine");
    if (kDebugMode) {
      print("resume sideengine ${result["success"]}");
    }
  }
  Future<void> callForOpenSurveyUrl() async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("openSurveyUrl");
    if (kDebugMode) {
      print("callForOpenSurveyUrl $result");
    }

    if (result["success"] != null &&
        result["success"] == true) {

      var payload = result["payload"] as Map<Object?, Object?>;
      var surveyVideoURL = payload["surveyVideoURL"] as String;

      Navigator.pushReplacementNamed(context,
          "/SurveyVideoScreen", arguments: SurveyVideoScreenArguments(
          surveyVideoURL));

    } else {
      Navigator.pop(context);
    }
  }
  Future<void> callForCheckSurveyUrl(context) async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("checkSurveyUrl");
    if (kDebugMode) {
      print("ERROR⚠️|️callForCheckSurveyUrl: : $result" );
    }
    if (Platform.isAndroid) {
      bool? isSurveyUrl = result["isSurveyUrl"] as bool?;
      if(isSurveyUrl == true){
        callForOpenSurveyUrl();
        Navigator.pop(context);
      }else{
        callForResumeSideEngine();
        Navigator.pop(context);
      }
    }else{
      if (result["success"] != null &&
          result["success"] == true) {
        callForOpenSurveyUrl();
      } else {
        callForResumeSideEngine();
        Navigator.pop(context);
      }
    }
  }
  Future<void> callForTimerFinishSideEngine(String userName, String email,bool isTestMode) async {
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("timerFinish", <String, Object>{
      "userName": userName,
      "email": email,
      "isTestMode": isTestMode,
    });
    if (kDebugMode) {
      print(result);
    }
    print("ERROR⚠️|️ CustomMap:  :  result: $result");
    List keys = result.keys.toList();
    // print("ERROR⚠️|️" + "CustomMap: " + ": " + 'keys: $keys');
    // List values = result.values.toList();
    if(keys.contains("response")) {
      var last = keys[keys.indexOf("response")];
      // print("ERROR⚠️|️" + "CustomMap: " + ": " + 'last: $last');
      print("ERROR⚠️|️ CustomMap: : response cas: $result");
      if(Platform.isAndroid) {
        var encodedStringNm = jsonEncode(result[last]);
        Map<String, dynamic> responseValue =
        json.decode(json.decode(encodedStringNm));
        List keyRes = responseValue.keys.toList();
        List valueRes = responseValue.values.toList();
        if (valueRes.contains("W3W")) {
          var resW3W = valueRes[keyRes.indexOf("result")];
          List keyW3W = resW3W.keys.toList();
          List valueW3W = resW3W.values.toList();

          map = valueW3W[keyW3W.indexOf("map")];
          words = "//" + valueW3W[keyW3W.indexOf("words")];
          latitude = valueW3W[keyW3W.indexOf("latitude")];
          longitude = valueW3W[keyW3W.indexOf("longitude")];
          setState(() {});
        }
      } else {
        if (result["success"] != null &&
            result["success"] == true) {
          var response = result["response"] as Map<Object?, Object?>;
          if (response["coordinates"]  != null) {
            var coordinates = response["coordinates"] as Map<Object?, Object?>;
            latitude = coordinates["lat"] as double?;
            longitude = coordinates["lng"] as double?;
          }
          if (response["map"]  != null) {
            map = response["map"] as String;
          }
          if (response["words"]  != null) {
            var w = response["words"] as String;
            words = "//$w";
          }

          // List keyW3W = resW3W.keys.toList();
          // List valueW3W = resW3W.values.toList();
          // map = valueRes[keyRes.indexOf("map")];
          // words = "//" + valueRes[keyRes.indexOf("words")];

          setState(() {});
        }
      }
    }
  }
}

class CustomMapScreenArguments {
  bool isTestMode;
  String userName;
  String email;

  CustomMapScreenArguments(this.isTestMode,this.userName,this.email);
}


