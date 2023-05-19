import 'dart:async';
import 'dart:collection';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';

class SurveyVideoScreen extends StatefulWidget {
  const SurveyVideoScreen({Key? key}) : super(key: key);
  static const routeName = '/extractArguments';

  @override
  State<SurveyVideoScreen> createState() => _SurveyVideoScreen();
}

class _SurveyVideoScreen extends State<SurveyVideoScreen> {
  static const channel = MethodChannel("com.sideml.flutersideml");

  // Step 2
  String surveyUrl = '';
  late BuildContext mContext;

  @override
  void initState() {
    super.initState();

    Future.delayed(const Duration(seconds: 1), () {
      final arg = ModalRoute.of(mContext)!.settings.arguments
          as SurveyVideoScreenArguments;

      surveyUrl = arg.surveyUrl;

      setState(() {});
    });
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    mContext = context;
    return Scaffold(
        body: Container(
      alignment: Alignment.center,
      child: Column(children: <Widget>[
        Container(
            margin: const EdgeInsets.only(left: 8, top: 50),
            alignment: Alignment.topLeft,
            child: IconButton(
              onPressed: () {
                callForResumeSideEngine();
                Navigator.pop(context);
              },
              icon: const Icon(Icons.close, color: Colors.black),
            )),
      Expanded(
          child: WebViewWidget(
              controller: WebViewController()
                ..setJavaScriptMode(JavaScriptMode.unrestricted)
                ..setBackgroundColor(const Color(0x00000000))
                ..setNavigationDelegate(
                  NavigationDelegate(
                    onProgress: (int progress) {
                      // Update loading bar.
                    },
                    onPageStarted: (String url) {},
                    onPageFinished: (String url) {},
                    onWebResourceError: (WebResourceError error) {},
                    onNavigationRequest: (NavigationRequest request) {
                      if (request.url
                          .startsWith('https://www.youtube.com/')) {
                        return NavigationDecision.prevent;
                      }
                      return NavigationDecision.navigate;
                    },
                  ),
                )
                ..loadRequest(Uri.parse(surveyUrl)))

      )
      ]),
    ));
  }

  Future<void> callForResumeSideEngine() async {
    final LinkedHashMap<Object?, Object?> result =
        await channel.invokeMethod("resumeSideEngine");
    if (kDebugMode) {
      print(result);
    }
  }
}

class SurveyVideoScreenArguments {
  String surveyUrl;

  SurveyVideoScreenArguments(this.surveyUrl);
}
