import UIKit
import Flutter
import BBSideEngine
import IQKeyboardManager
import GoogleMaps  // Add this import

let shared = BBSideEngineManager.shared


@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {

    var globResult: FlutterResult?


  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {

     IQKeyboardManager.shared().isEnabled = true // manage keyboard behaviour
      GMSServices.provideAPIKey("AIzaSyDRfMrw9zJJI6R4ilNeBGxlH_vlNB2snYM")


    //Production mode: Use this mode when you are ready to release your app to the AppStore (You can use .standard or .custom theme)

     let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
        let batteryChannel = FlutterMethodChannel(name: "com.sideml.flutersideml",binaryMessenger: controller.binaryMessenger)

            batteryChannel.setMethodCallHandler({
           (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in

                self.globResult = result

                if(call.method == "configure"){

                    var isCustom = false
                    var mode = ""
                    var lic = ""


                    if let args = call.arguments as? [String : Any]{
 
                        if let value = args["mode"] as? String {
                            mode = value
                        }

                        if let value = args["lic"] as? String {
                            lic = value
                        }

                        if let value = args["isCustom"] as? Bool {
                            isCustom = value
                        }
                    }

                    var theme: BBTheme = isCustom ?  .custom : .standard
                    self.configureSideEngine(mode: mode, lic :lic, theme: theme)

                } else if(call.method == "startSideML") {

                    var isStarted = false

                    if let args = call.arguments as? [String : Any]{

                        if let value = args["userName"] as? String {
                            shared.riderName = value
                        }

                        if let value = args["email"] as? String {
                            shared.riderEmail = value
                        }
                        if let value = args["isStarted"] as? Bool {
                            isStarted = value
                        }

                    }

                    shared.deviceId = self.uniqueId()
                    shared.riderId = self.uniqueId();

                    self.startStopSideEngine(isStarted: isStarted)

                } else if(call.method == "stopSideML"){

                    var isStarted = false

                    if let args = call.arguments as? [String : Any]{

                        if let value = args["isStarted"] as? Bool {
                            isStarted = value
                        }
                    }

                    self.startStopSideEngine(isStarted: isStarted)

                } else if(call.method == "incidentDetected"){


                } else if(call.method == "resumeSideEngine"){

                    shared.resumeSideEngine() //Pass SIDE engine mode here

                } else if(call.method == "openSurveyUrl"){

                    if(shared.surveyVideoURL == ""){
                        let checkoutResult = CheckoutResult(success: false, type: 1, payload: nil)
                        result(checkoutResult.dictionaryRepresentation)

                    } else {
                        let checkoutResult = CheckoutResult(success: true, type: 1, payload: ["surveyVideoURL":shared.surveyVideoURL])
                        result(checkoutResult.dictionaryRepresentation)
                    }

                } else if(call.method == "checkSurveyUrl"){

                       if(shared.surveyVideoURL == ""){

                           let checkoutResult = CheckoutResult(success: false, type: 1, payload: nil)
                           result(checkoutResult.dictionaryRepresentation)

                       }else{
                           let checkoutResult = CheckoutResult(success: true, type: 1, payload: nil)
                           result(checkoutResult.dictionaryRepresentation)
                       }

                } else if(call.method == "timerFinish"){

                    // notify custom partner

                    if let args = call.arguments as? [String : Any]{

                        if let value = args["userName"] as? String {
                            shared.riderName = value
                        }

                        if let value = args["email"] as? String {
                            shared.riderEmail = value
                        }
                    }


                    //TODO: Set user id
                     shared.riderId = self.uniqueId();


                    //TODO: call method for fetching W3W Location data
                    shared.fetchWhat3WordLocation { response in
                        print("Fetched w3w location:", response)

                      //  let checkoutResult = CheckoutResult(success: response.success, type: response.type.rawValue, payload: response.payload)

                        result(response)

                    }

                    shared.notifyPartner();


                } else if(call.method == "customPartnerNotify"){

                    // notify custom partner
                    shared.notifyPartner();

                }else if(call.method == "startSOSML"){
                    if let args = call.arguments as? [String : Any]{

                        if let value = args["userName"] as? String {
                            shared.riderName = value
                        }

                        if let value = args["email"] as? String {
                            shared.riderEmail = value
                        }
                        if let value = args["isActive"] as? String {
                            shared.activeSOS()
                        }else{
                            shared.deActiveSOS()
                        }
                    }
                }else if(call.method == "startFlareAwareML"){
                    if let args = call.arguments as? [String : Any]{
                        if let value = args["isActive"] as? String {
                            shared.startFlareAware()
                        }else{
                            shared.stopFlareAware()
                        }
                    }
                } else {
                    result(FlutterMethodNotImplemented)
                        return
                }
         })


      GeneratedPluginRegistrant.register(with: self)
     return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

    private func startStopSideEngine(isStarted: Bool) {

        if(isStarted) {
            shared.startSideEngine() //Pass SIDE engine mode here
        } else {
            shared.stopSideEngine()
        }

    }

    private func configureSideEngine(mode: String, lic :String,  theme: BBTheme) {
        if(mode === "production"){
            shared.configure(accessKey: lic, mode: .production, theme: theme)
        }else{
            shared.configure(accessKey: lic, mode: .sandbox, theme: theme)
        }

        setListener()
    }

        private func setListener() {

            shared.sideEventsListener { (response) in
                //Handle callback for specific event

                if response.type == .configure && response.success == true {
                    print("SIDE engine response is: \(response.type)")

                    // if response.success = true then you are ready to start the Side engine process. Do not call the Side engine process until you have received response.success = true
                    let checkoutResult = CheckoutResult(success: response.success, type: response.type.rawValue, payload: response.payload)
                    if let tempResult = self.globResult {
                        tempResult(checkoutResult.dictionaryRepresentation)
                    }


                } else if response.type == .start && response.success == true {
                    print("SIDE engine response is: \(response.type)")

                    //Update your UI here (e.g. update START button color or text here when SIDE engine started)
                    let checkoutResult = CheckoutResult(success: response.success, type: response.type.rawValue, payload: response.payload)
                    if let tempResult = self.globResult {
                        tempResult(checkoutResult.dictionaryRepresentation)
                    }

                } else if response.type == .stop && response.success == true {
                    print("SIDE engine response is: \(response.type)")

                    //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
                    let checkoutResult = CheckoutResult(success: response.success, type: response.type.rawValue, payload: response.payload)
                    if let tempResult = self.globResult {
                        tempResult(checkoutResult.dictionaryRepresentation)
                    }
                } else if response.type == .incidentDetected {
                    print("SIDE engine response is: \(response.type)")

                    //Threshold reached and you will redirect to the countdown page.
                    //Return incident status and confidence level, you can fetch confidence using the code below:
                    if let confidence = response.payload?["confidence"] {
                        print("SIDE engine confidence is: \(confidence)")
                    }

                    let checkoutResult = CheckoutResult(success: response.success, type: response.type.rawValue, payload: response.payload)
                    if let tempResult = self.globResult {
                        tempResult(checkoutResult.dictionaryRepresentation)
                    }

                    //Send SMS or Email code here to notify your emergency contact (Github example for sample code)
                } else if response.type == .incidentCancel {
                    print("SIDE engine response is: \(response.type)")

                    //User canceled countdown countdown to get event here, this is called only if you configured thenstandard theme.
                } else if response.type == .incidentAutoCancel {
                    print("SIDE engine response is: \(response.type)")

                    //GPS reports that user is still moving at average speed or distance - assume speed bump or similar
                } else if response.type == .timerStarted {
                    print("SIDE engine response is: \(response.type)")

                    //Countdown timer started after breach delay, this is called only if you configured the standard theme.
                } else if response.type == .timerFinished {
                    print("SIDE engine response is: \(response.type)")

                    //Countdown timer completed and jump to the incident summary page, this is only called if you configured the standard theme.
                } else if response.type == .incidentAlertSent {
                    print("SIDE engine response is: \(response.type)")
                    //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
                    let checkoutResult = CheckoutResult(success: response.success, type: response.type.rawValue, payload: response.payload)
                    if let tempResult = self.globResult {
                        tempResult(checkoutResult.dictionaryRepresentation)
                    }
                } else if response.type == .sms {
                    print("SIDE engine response is: \(response.type)")

                    //Returns SMS delivery status and response payload
                } else if response.type == .email {
                    print("SIDE engine response is: \(response.type)")

                    //Returns email delivery status and response payload
                } else if response.type == .location {
                    print("SIDE engine response is: \(response.type)")

                    //Returns collecation object
                }
            }
        }

    //Generate random uniqueID
            func uniqueId() -> String {
                 return UIDevice.current.identifierForVendor!.uuidString
            }
}

class CheckoutResult {

    var success: Bool = false
    var type: Int
    var payload: [String: Any]?

    /// Constructor
    init(success: Bool, type: Int, payload: [String: Any]?) {
        self.type = type;
        self.payload = payload;
        self.success = success
    }

    /// The dictionary representation of this class
    var dictionaryRepresentation : [String:Any] {
        return ["type" : self.type,  "success" : self.success, "payload": self.payload ?? nil]
    }
}
