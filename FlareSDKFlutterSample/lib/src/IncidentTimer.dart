import 'dart:async';
import 'dart:collection';
import 'package:flutersideml/src/CustoMapScreen.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:vibration/vibration.dart';

class IncidentTimer extends StatefulWidget {
  const IncidentTimer({Key? key}) : super(key: key);
  static const routeName = '/extractArguments';
  @override
  State<IncidentTimer> createState() => _IncidentTimer();
}

class _IncidentTimer extends State<IncidentTimer> {
  static const channel = MethodChannel("com.sideml.flutersideml");
  // Step 2
  Timer? countdownTimer;
  String email = '';
  String userName = '';
  bool isTestMode = false;
  var seconds = 0;
  late BuildContext mContext;
  @override
  void initState() {
    super.initState();

    Vibration.vibrate(
      pattern: [10, 2000, 500, 2000, 500, 2000, 500, 2000, 500, 2000, 500,
        2000, 500, 2000, 500, 2000, 500, 2000, 500, 2000, 500,
        2000, 500, 2000, 500, 2000, 500, 2000, 500, 2000, 500],
      // intensities: [10, 128, 0, 255, 0, 64, 0, 255],
    );

    Future.delayed(const Duration(seconds: 1), () {
      final arg = ModalRoute.of(mContext)!.settings.arguments as IncidentTimerScreenArguments;
      if(arg.isTestMode != null && arg.isTestMode == true){
        seconds = 6;
      }else{
        seconds = 31;
      }
      isTestMode = arg.isTestMode;
      email = arg.email;
      userName = arg.userName;
      setState(() {
      });

      startTimer();
      setCountDown();
    });
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
  }
  void startTimer() {
    countdownTimer =
        Timer.periodic(const Duration(seconds: 1), (_) => setCountDown());
  }

  void setCountDown() {
    var sec = seconds - 1;
    if (sec <= 0) {
      countdownTimer!.cancel();
        Vibration.cancel();
      Navigator.pop(context);
      Navigator.pushNamed(mContext,
          "/CustomMapScreen", arguments: CustomMapScreenArguments(
              isTestMode,
              userName,
              email
          ));
      // Navigator.of(context)
      //     .pushReplacement(MaterialPageRoute(builder: (context) => const CustomMapScreen()));
    }
    setState(() {
      seconds = sec;
    });
  }

  @override
  Widget build(BuildContext context) {
    mContext = context;
    return Scaffold(
        body:Container(
          alignment: Alignment.center,
          child:  Column(
            children: <Widget>[
              Container(
                  margin: const EdgeInsets.only(left: 8, top: 50),
                  alignment: Alignment.topLeft,
                  child: IconButton(
                    onPressed: () {
                      if (countdownTimer != null) {
                        countdownTimer!.cancel();
                      }
                      callForResumeSideEngine();
                          Vibration.cancel();
                      Navigator.pop(context);
                    },
                    icon: const Icon(Icons.close, color: Colors.black),
                  )),

              // Step 8
              Expanded(flex: 1,child:  Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Container(
                    alignment: Alignment.center,
                    child: const Text(
                      'CUSTOM UI',
                      textScaleFactor: 2,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                          color: Colors.black,
                          fontWeight: FontWeight.w600,
                          fontSize: 16),
                    ),
                  ),

                  Row(

                    mainAxisAlignment: MainAxisAlignment.center,
                    children: <Widget>[
                      Container(
                        margin: const EdgeInsets.only(top: 20),
                        alignment: Alignment.center,
                        child: Text(
                          seconds == 0 ?"":
                          '$seconds',
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              color: Colors.black,
                              fontSize: 50),
                        ),
                      ),
                      Text(
                        seconds == 0 ?"":'SECONDS',
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.black,
                            fontSize: 14),
                      )
                    ],
                  )
                ],
              )
              )],
          ),
        )
    );
  }


  Future<void> callForResumeSideEngine() async {
    print("ERROR⚠️|️" + "callForResumeSideEngine: " + ": ");
    final LinkedHashMap<Object?, Object?> result =
    await channel.invokeMethod("resumeSideEngine");
    if (kDebugMode) {
      print(result.entries.first.value);
    }
  }
}
class IncidentTimerScreenArguments {
  bool isTestMode;
  String userName;
  String email;

  IncidentTimerScreenArguments(this.isTestMode,this.userName,this.email);
}
