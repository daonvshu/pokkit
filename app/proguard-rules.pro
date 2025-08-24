-keep class androidx.compose.runtime.** { *; }
-keep class androidx.collection.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.ui.text.platform.ReflectionUtil { *; }

# We're excluding Material 2 from the project as we're using Material 3
-dontwarn androidx.compose.material.**

# Kotlinx coroutines rules seems to be outdated with the latest version of Kotlin and Proguard
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.** { *; }

-dontwarn kotlinx.coroutines.flow.**

-dontwarn kotlinx.coroutines.internal.**

-keepclasseswithmembers class * {
    kotlinx.coroutines.CoroutineScope *(...);
}

-keepattributes *Annotation*

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# OkHttp3
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

-dontwarn retrofit2.AndroidMainExecutor
-dontwarn android.os.**
-dontwarn android.annotation.**

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
# May be used with robolectric or deliberate use of Bouncy Castle on Android
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-dontwarn okio.**
-keep class okio.** { *; }

-keep class ** {
    @kotlinx.serialization.Serializable *;
}

# 保留所有自动生成的 $$serializer 类
-keepclassmembers class **$$serializer {
    *;
}

# 保留 Kotlin Metadata 注解
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature


# 保留桥接文件生成的 Kotlin 元数据
-keep class androidx.compose.foundation.relocation.*Kt* { *; }

# 保留 Kotlin Metadata 注解等
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# 保留 Compose runtime/internals
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# 避免桥接、inline 函数被删除或重写
-keep class *Kt__* { *; }

-dontwarn javax.annotation.**

-dontwarn javax.xml.**
-keep class javax.xml.** { *; }

-keep class kotlin.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.sequences.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.**
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

# Exposed
-keep class org.jetbrains.exposed.** { *; }
-keepclassmembers class org.jetbrains.exposed.** { *; }
-dontwarn org.postgresql.util.PGobject

-dontwarn org.apache.commons.logging.LogFactory
-dontwarn org.apache.commons.logging.**

-keep class org.sqlite.** { *; }

-keep class com.daonvshu.protocol.codec.annotations.**
-keep class com.daonvshu.protocol.** { *; }
-keep @com.daonvshu.protocol.codec.annotations.Type class * { *; }
-keepclassmembers class * {
    @com.daonvshu.protocol.codec.annotations.Subscribe *;
}
