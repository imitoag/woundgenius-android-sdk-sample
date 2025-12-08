-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod


-keep class com.example.woundsdk.data.pojo.** { *; }
-keepclassmembers class com.example.woundsdk.data.pojo.** { *; }


-keep class com.example.woundsdk.ui.** { *; }
-keepclassmembers class com.example.woundsdk.ui.** { *; }

-keep class com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity$Companion {
    public *;
}

-keep class com.example.woundsdk.storage.shared.SharedMemoryImpl { *; }

-keep class com.example.woundsdk.storage.shared.SharedMemory { *; }

-keepclassmembers class com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity {
    static ** Companion;
}

-keepnames class com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity$Companion$Arguments {
    <fields>;
}

-keepnames class com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity$Companion$ResArgs {
    <fields>;
}

-keep class com.example.woundsdk.ui.screen.whatsnew.WhatsNewActivity { *; }


-keep class com.example.woundsdk.ui.screen.videotutorial.VideoPlayerActivity$Companion {
    public *;
}


-keepclassmembers class com.example.woundsdk.ui.screen.videotutorial.VideoPlayerActivity {
    public static final ** Companion;
}

-keep class com.example.woundsdk.ui.screen.bodypicker.BodyPickerActivity$Companion {
   public *;
}


-keepclassmembers class com.example.woundsdk.ui.screen.bodypicker.BodyPickerActivity {
    public static final ** Companion;
}


-keepnames class com.example.woundsdk.ui.screen.bodypicker.BodyPickerActivity$Companion$Args {
    <fields>;
}


-keepnames class com.example.woundsdk.ui.screen.bodypicker.BodyPickerActivity$Companion$ResArgs {
    <fields>;
}


-keepclassmembers class ** {
    public static final *** *;
}

-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}


-keep class com.example.woundsdk.di.** { *; }
-keep class com.example.woundsdk.storage.** { *; }
-keep class com.example.woundsdk.utils.** { *; }
