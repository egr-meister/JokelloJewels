# Jokello Jewels — R8 rules.
# Shipped with isMinifyEnabled=false first (staged R8). These rules are ready for when
# minification is enabled after a verified non-minified release.

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.AnnotationsKt

# Keep generated serializers and Companion.serializer() for @Serializable classes.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    static **$* *;
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
# Serializable objects (navigation routes are @Serializable objects).
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# App data models and typed navigation routes.
-keep class com.jokellojewels.game.data.** { *; }
-keep class com.jokellojewels.game.ui.nav.** { *; }
