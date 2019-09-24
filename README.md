# analyzeBiomarkers

## Key Files
- app\src\main\java\com\example\testPhoto\MainActivity.java
- app\src\main\java\com\example\testPhoto\Analyze_Activity.java
- app\src\main\AndroidManifest.xml
- app\src\main\res\layout\activity_analyze.xml
- app\src\main\res\layout\activity_main.xml

## Explanation
### Main Activity
- Contains the code detailing the overall app code including the app's ability to take a picture of the uPAD
### Analyze Activity
- Key activity for analyzing the picture of the uPAD was taken
- Currently not much code in there right now
### Android Manifest
- Contains the basis for the foundation of the code
### Build.Gradle
- Contains all the implemented modules (including OpenCV341) needed to perform functions
### Activity_Analyze.xml
- Design file for the analyzing activity
- This is where you design the visual side of the app specifically for the analyzing activity
### Activity_Main.xml
- Design file for the main activity screen
- This is where you edit the visual design of the main screen of the app
  - Must establish what buttons will be on the screen in this file and then use the identified button variable in the corresponding .java file to give that button function
