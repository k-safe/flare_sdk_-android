# SideEngineAndroid-SDK

## Documentation

Looking for iOS documentation? [Click Here](https://k-safe.github.io/busbysdk_ios/)

### **Minimum Requirements**

    - Android
    - Android Studio 4.0.0 (or later)
    - Java 8 (or later)

#### **Supported devices and emulators**
    - Supports armeabi-v7a, arm64-v8a, x86, and x86_64 architectures, as well as emulator images for these architectures.

#### **Supported API Levels**
    - Android API Level 21 (Lollipop) and higher.

#### **Developer tools**
To build the associated [Quickstart project](https://github.com/k-safe/busbysdk_android/tree/main/SideEngineSample) you will need [Android Studio](https://developer.android.com/studio/index.html) with installed SDK Platform for API Level 24, as well as the supporting libraries.

### **1. Add SideEngine SDK to your app**

#### **Add authentication token in gradle.properties**

        gitUserName=Your git userName
        gitToken=Your git token

#### **Add the library link below to App build.gradle**

        implementation 'com.k-safe:sideengine:1.3.8'

        //TODO: If you want to use the custom UI with map, you need to add map lib
        implementation 'com.google.android.gms:play-services-maps:18.0.2'

#### **Add the lines below to Project build.gradle**

     allprojects {
        repositories {
            google()
            mavenCentral()
            maven {
              name = "SideEngineSDK"
              url = uri("https://maven.pkg.github.com/k-safe/busbysdk_android")
              credentials {
                username = gitUserName
                password = gitToken
              }
            }
        }
    }

#### Refresh gradle using sync


### **2. Initialise SideEngine in your app**

Add the following initialisation code to your application. If you're using a Quickstart sample project, this has been done for you.

    - import com.sos.busbysideengine.BBSideEngine;

### **3. Configure SideEngine:**
You can configure SideEngine for production or sandbox mode. Choose between the standard and custom theme options. If you select the standard theme, you will have the default UI. If you would like to use your own UI, select custom theme option.

   	//You can configure SideEngine for production OR sandbox mode
	//TODO: import static com.sos.busbysideengine.Constants.ENVIRONMENT_SANDBOX;
    //TODO: import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

    //You can choose theme option custom OR standard, if you select the standard theme, you will have the default UI. If you would like to use your own UI, select custom theme option.
    //TODO: import static com.sos.busbysideengine.Constants.BBTheme.CUSTOM;
	//TODO: import static com.sos.busbysideengine.Constants.BBTheme.STANDARD;

    //Sandbox mode: Use this mode only during development and integration (You can use a STANDARD OR CUSTOM theme)
	- BBSideEngine.configure(this,"Your license key here", ENVIRONMENT_SANDBOX, STANDARD);

    -------------------------------------OR-----------------------------------------------

    //Production mode: Use this mode when you are ready to release your app to the playstore (You can use a STANDARD OR CUSTOM theme)
	- BBSideEngine.configure(this,"Your license key here", ENVIRONMENT_PRODUCTION, STANDARD);


### **4. Get SideEngine sdk instance:**

    - BBSideEngine bbSideEngine = BBSideEngine.getInstance(this);

### **5. Configure user parameters and start sideEngine**

- Configure your license ( add in Applicationclass or First activity )

    - BBSideEngine.configure(this,"Your license key here", ENVIRONMENT_PRODUCTION, STANDARD);

####  **Start SideEngine**

    - BBSideEngine bbSideEngine = BBSideEngine.getInstance(this);
    - bbSideEngine.setBBSideEngineListener(this);
    - bbSideEngine.startSideEngine(this, false);
    - bbSideEngine.enableActivityTelemetry(false); //This is true by default. If user sets this to "false" then activity telemetry will not be sent to Flare.
    - bbSideEngine.setLocationNotificationTitle("Protection is active");
    //User details: (if the user values below are empty, the user emergency contact cannot see these details in the SMS, Email, Slack and Webhook when an incident is detected.)
    - bbSideEngine.setRiderName("App user name here"); (Optional)
    - bbSideEngine.setUserEmail("Email"); (Optional) 
    - bbSideEngine.setUserId("Unique rider ID"); (Optional)


	- Test mode (This mode is used only to demonstrate to the user how the incident process works after an incident is detected.)

		bbSideEngine.startSideEngine(this, true);
    - Live mode reads the SIDE engine algorithm to get the actual incident result.
        bbSideEngine.startSideEngine(this, false);

    - Stop SideEngine when your journey ends - (This will release all SIDE processes and system memory)
        bbSideEngine.stopSideEngine();

###   **6. Enable Foreground service - Sticky Notification**

The Flare Android SDK necessitates the utilisation of device sensors to detect incidents. However, these sensors are only operational in the foreground on Android devices. To maintain continuous sensor operation even when the device is put in the background, we employ a foreground service that utilises a sticky notification. This sticky notification guarantees your device's complete protection by keeping the sensors running.

        - bbSideEngine.setStickyEnable(true).


**Customise Sticky Notification:**

Additionally, in certain instances, it may be desirable to customise the appearance of the sticky notification to match your application. To enable this, we provide a set of methods.

    - Modify the background colour of your sticky notification (with the default hue being white):
           
          - bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
          
    - Modify the notification icon (Default icon being R.drawable.ic_launcher):
           
          - bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
          
    - Amend notification title ( Default Title is: "Protection is active")
           
          - bbSideEngine.setLocationNotificationTitle("Notification Title")
          
    - Modify the notification description:
           
          - bbSideEngine.setNotificationDescText("Notification Description")
    
    - In order to modify the colour of notification description, update the 'color.xml' file by including the following colour tags with corresponding names and codes:
    
          - notification_stop_navigation_text_color (Default: #000000)
          - notification_title_text_color (Default: #000000)
          - notification_desc_text_color (Default: #000000)
    
    - To modify the small notification icons, append them to a specific folder with corresponding dimensions. The icon file should be named "ic_notification.png", and placed in the following folders with their respective sizes in pixels:  
        - drawable-hdpi : 36x36
        - drawable-mdpi : 24x24
        - drawable-xhdpi : 48x48
        - drawable-xxhdpi : 72x72
        - drawable-xxxhdpi : 96x96


#### **7. Customise the SideEngine theme(Optional).**

    - bbSideEngine.setIncidentTimeInterval(45) = Default 30 seconds
    - bbSideEngine.setIncidentHeader("header");  //Only for standard theme
    - bbSideEngine.setIncidentInfoMessage("message"); //Only for standard theme
    - bbSideEngine.setIncidentPageHeaderColor("#ff0000"); //Only for standard theme
    - bbSideEngine.setIncidentPageBackgroundColor("#ff00ff"); //Only for standard theme
    - bbSideEngine.setIncidentPageHeaderMessageColor("#ffffff"); //Only for standard theme
    - bbSideEngine.setSwipeButtonBgColor(R.color.white) = Default "ffffff" //Only for standard theme
    - bbSideEngine.setSwipeButtonTextSize(18) = Default 16 //Only for standard theme
    - bbSideEngine.setSwipeButtonText("Swipe to Cancel"); //Only for standard theme
    - bbSideEngine.setStickyEnable(true) = Default true // Used to enable/disable sticky notification used to guarantee the service runs in the background.


    - bbSideEngine.setImpactBody("Detected a potential fall or impact involving"); //This message appears in the SMS, email, webhook and slack body with rider name passed in this method (bbSideEngine.setRiderName("App user name here");) parameter




#### **Event list:**

* SideEngine events listener, sample code below:

        @Override
        public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {
          Log.d("SideEngine response: ", response.toString());
          switch (type) {
            case CONFIGURE:
              // if status = true then you are ready to start the Side engine process. Do not call the Side engine process until you have received status = true
              break;
            case START:
              //Update your UI here (e.g. update START button color or text here when SIDE engine started)
              break;
            case STOP:
              //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
              break;
            case INCIDENT_DETECTED:
              //Threshold reached and you will redirect to the countdown page.
              //Return incident status and confidence level, you can fetch confidence using the code below:
              String mConfidence = response.getString("confidence");
              //Send SMS or Email code here to notify your emergency contact (See Github example for sample code)
              break;
            case INCIDENT_CANCEL:
              //User canceled countdown to get event here, this is only called if you configured the standard theme.
              break;
            case INCIDENT_AUTO_CANCEL:
              //GPS reports that user is still moving at average speed or distance - assume speed bump or similar
              break;
            case TIMER_STARTED:
              //Countdown timer started after breach delay, this is only called if you configured the standard theme.
              break;
            case TIMER_FINISHED:
              //Countdown timer completed and jump to the incident summary page, this is only called if you configured the standard theme.
              break;
            case INCIDENT_ALERT_SENT:
              //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
              break;
            case INCIDENT_VERIFIED_BY_USER:
              //User verified incident true or false
              break;
            case SMS:
              //Returns SMS delivery status and response payload
              break;
            case EMAIL:
              //Returns email delivery status and response payload
              break;
          }
        }

        enum BBSideOperation {
            CONFIGURE, //Configure side engine
            START,  //Start side engine
            STOP,   //Stop side engine
            SMS,    //Returns SMS delivery status
            EMAIL,  //Returns email delivery status
            INCIDENT_DETECTED,  //Threshold reached and you will be redirected to the countdown page
            INCIDENT_CANCEL,    //User cancelled countdown
            INCIDENT_AUTO_CANCEL, //When GPS reports that user is still moving at average speed or distance - assume speed bump or similar
            INCIDENT_ALERT_SENT,    //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
            INCIDENT_VERIFIED_BY_USER,   //User verified incident true or false
            TIMER_STARTED,  //Countdown timer started
            TIMER_FINISHED  //Countdown timer finished
        }

### **8. Send SMS**

- Function to send out SMS when an incident is detected(Called once per incident).

    ContactClass contacts = new ContactClass();
    contacts.setUserName('name');
    contacts.setPhoneNumber('number');
    contacts.setCountryCode('counteryCode);

    bbSideEngine.sendSMS(contacts, testModeFlag);


### **9. Send Email**

- Function to send out an Email when an incident is detected(Called once per incident).

    - bbSideEngine.sendEmail("example@gmail.com", testModeFlag);


* SMS & Email sending requires the event below:

        @Override
        public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {
            if (type == INCIDENT_DETECTED) {
                //Use either one or both
                    ContactClass contacts = new ContactClass();
                    contacts.setUserName('name');
                    contacts.setPhoneNumber('number');
                    contacts.setCountryCode('counteryCode);

                    bbSideEngine.sendSMS(contacts, testModeFlag);
                    bbSideEngine.sendEmail("example@gmail.com", testModeFlag);
            }
        }

#### **10. Custom Theme Only:**
- SIDE engine Listener is the same for standard and custom theme options, but you can manage your UI using the sample code below: Reference link [Click Here](https://github.com/k-safe/busbysdk_android/blob/main/SideEngineSample/app/src/main/java/com/example/myapplication/CustomThemeActivity.java)
- It is necessary to implement the **BBSideEngineListener**  listener and override the method as indicated below:

        @Override
        public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {
            switch (type) {
                case INCIDENT_DETECTED:
                    //Threshold reached and you will be redirected to the countdown page.
                    Log.d("status", "" + status);
                    //Return incident status and confidence level, you can fetch confidence using the code below:
                    if (status) {
                        boolean mCustomTheme = response.getBoolean("customTheme");
                        //Send SMS or Email code here to notify your emergency contact (Github example for sample code)
                        bbSideEngine.sendSMS(contacts, testModeFlag)
                        bbSideEngine.sendEmail("example@gmail.com", testModeFlag);
                        //You can open your custom count down controller here in the custom theme.
                        if (mCustomTheme) {
                            Intent intent = new Intent(CustomThemeActivity.this, CustomUiActivity.class);
                            intent.putExtra("userName", etUserName.getText().toString().trim());
                            intent.putExtra("email", etUserEmail.getText().toString().trim());
                            intent.putExtra("btnTestClicked", btnTestClicked);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
            }
        }

- Additionally, to handle your code's GET methods, you can obtain another listener. Firstly, set the below listener in your code by using the

      - BBSideEngine.getInstance(null).setBBSideEngineListenerInLib(this);

- It is necessary to implement the **BBSideEngineUIListener**  listener and override the method as indicated below:

- The following method provides SMS delivery status and response payload:

      - void onSendSMSCallback(boolean status, JSONObject response);

- The following method provides the delivery status of an email and a response callback:

      - void onSendEmailCallback(boolean status, JSONObject response);

- The subsequent procedure furnishes the incident cancellation callback with status and response:

      - void onIncidentCancelCallback(boolean status, JSONObject response);

- The subsequent procedure offers automatic cancellation of incident status and response callback:

      - void onIncidentAutoCancelCallback(boolean status, JSONObject response);

- The following method facilitates the provision of incident verification by user callback status and response:

      - void onIncidentVerifiedByUser(boolean status, JSONObject response);

- The subsequent procedure is initiated upon attainment of the threshold, and will trigger an incident alert status and a response callback:

      - void onIncidentAlertCallback(boolean status, JSONObject response);
      //If GPS reports that user is still moving at average speed or distance - assume speed bump or similar

- The subsequent procedure presents the callback for the Close Survey Video activity:

      - void onCloseSurveyVideoActivityCallback();

- Transmit alerts to partner channels (Webhook, Slack, Send SMS, Send Email) and modify the Flare backend to record the occurrence. For a sample code reference, kindly refer to: [Click Here](https://github.com/k-safe/busbysdk_android/blob/main/SideEngineSample/app/src/main/java/com/example/myapplication/CustomUiActivity.java)
  - This process will be invoked after the countdown has elapsed and the user has confirmed the incident.

        - BBSideEngine.getInstance(null).notifyPartner();

  - A function is available to obtain the What3word location for display in a custom Map view controller. To view sample code, please refer to [Click Here](https://github.com/k-safe/busbysdk_android/blob/main/SideEngineSample/app/src/main/java/com/example/myapplication/CustomUiActivity.java)

        - BBSideEngine.getInstance(null).fetchWhat3WordLocation(CustomUiActivity.this);

  - Use this function in scenarios when an incident is triggered more than once.

        - BBSideEngine.getInstance(null).resumeSideEngine()

  - Use this function in scenarios when an incident cancel.

        - BBSideEngine.getInstance(null).sideEngineCancelIncident()

  - Use this function in scenarios when you want to resume side engine when app in background.

        - BBSideEngine.getInstance(null).resumeSensorIfAppInBackground()

  - If your partner incident survey is open and you are in custom mode, call use this method on the incident summary page to ask for user input in the survey video

        - BBSideEngine.getInstance(null).startSurveyVideoActivity()

  - Use this function in scenarios when you want to identify app is in forground/background.

        - Common.getInstance().isAppInBackground()


### **11. Other configurations**
    Allow Location permissions for SDK functionality to work smoothly.


* Happy coding!
