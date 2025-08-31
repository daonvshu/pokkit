# =======================
# Compose 相关保留规则
# =======================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.text.platform.ReflectionUtil { *; }
-keep class androidx.compose.foundation.relocation.*Kt* { *; }

# 保留桥接、inline 函数，防止被删或重写
-keep class *Kt__* { *; }

# =======================
# AndroidX 其他库
# =======================
-keep class androidx.collection.** { *; }
-keep class androidx.lifecycle.** { *; }

# =======================
# Kotlinx Coroutines 相关
# =======================
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.** { *; }
-dontwarn kotlinx.coroutines.flow.**
-dontwarn kotlinx.coroutines.internal.**

-keepclasseswithmembers class * {
    kotlinx.coroutines.CoroutineScope *(...);
}

# =======================
# Kotlin Serialization
# =======================
-keep class ** {
    @kotlinx.serialization.Serializable *;
}
# 保留所有自动生成的 $$serializer 类
-keepclassmembers class **$$serializer {
    *;
}

# =======================
# Retrofit 相关规则
# =======================
# 避免 Retrofit 反射泛型、注解等信息丢失
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# 保留 Retrofit 接口方法及参数，允许缩减混淆
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# R8 特殊处理避免接口混淆导致问题
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Suspended 函数相关保留
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Retrofit Response 保留
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# 避免 Retrofit Kotlin Extensions 警告
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# =======================
# OkHttp & Okio 相关
# =======================
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-dontwarn okio.**
-keep class okio.** { *; }

# =======================
# 通用忽略警告
# =======================
# Animal Sniffer 相关
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# javax.annotation 相关（JSR 305 注解）
-dontwarn javax.annotation.**

# Android system warnings
-dontwarn android.os.**
-dontwarn android.annotation.**

# Kotlin Unit 相关
-dontwarn kotlin.Unit

# =======================
# Material 2 相关警告忽略（项目使用 Material 3）
# =======================
-dontwarn androidx.compose.material.**

# =======================
# 其他保留属性
# =======================
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, KotlinMetadata

# 保留 Kotlin Metadata 注解及相关桥接文件生成的元数据
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# 如果使用 Kotlin 反射，保留其元数据
# -keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.reflect.jvm.internal.impl.utils.** { *; }
-keepclassmembers class kotlin.reflect.jvm.internal.impl.utils.** { *; }
-dontwarn kotlin.jvm.internal.**

# 通用规则：Kotlin 支持反射与注解
# -keepclassmembers class ** { @kotlin.Metadata *; }
-dontwarn kotlin.**

# Exposed
-keep class org.jetbrains.exposed.** { *; }
-keepclassmembers class org.jetbrains.exposed.** { *; }
-dontwarn org.postgresql.util.PGobject

-dontwarn org.apache.commons.logging.LogFactory
-dontwarn org.apache.commons.logging.**

-keep class org.sqlite.** { *; }

-keepattributes RuntimeVisibleAnnotations
-keep class com.daonvshu.protocol.codec.annotations.Type
-keep class com.daonvshu.protocol.codec.annotations.Subscribe
-keep @com.daonvshu.protocol.codec.annotations.Type class * { *; }
-keepclassmembers @com.daonvshu.protocol.codec.annotations.Type class * {
    public <init>(...);
 }
-keepclassmembers class * {
    @com.daonvshu.protocol.codec.annotations.Subscribe <methods>;
}

-dontwarn javax.xml.**
-keep class javax.xml.** { *; }
-keep class org.simpleframework.** { *; }

# network bean
-keep @org.simpleframework.xml.Root class * { *; }
-keep @kotlinx.serialization.Serializable class * {*;}