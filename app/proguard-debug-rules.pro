-dontobfuscate

-keep class * implements dagger.MembersInjector { *; }
-keep class dagger.hilt.** { *; }
-keepclassmembers class com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment$Companion { void setViewPagerSmoothScroll(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment$Companion { void setViewPagerSmoothScroll(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.feature_views.pieChart.PieChartView$Companion { void setDisableAnimationsForTest(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.navigation.ScreenResolver$Companion { void setDisableAnimationsForTest(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate$Companion { void setRestartAppIsBlocked(boolean); }
-keepclassmembers class com.example.util.simpletimetracker.core.mapper.ColorMapper$Companion { synthetic java.util.List getAvailableColors$default(com.example.util.simpletimetracker.core.mapper.ColorMapper$Companion,boolean,int,java.lang.Object); }
-keep class com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider { *; }
-keep class com.example.util.simpletimetracker.core.utils.TestUtils { *; }
-keep class kotlin.collections.CollectionsKt { *; }
-keep class androidx.test.espresso.IdlingRegistry { *; }
-keep class androidx.test.espresso.IdlingResource { *; }
-keep class androidx.test.espresso.IdlingResource$ResourceCallback { void onTransitionToIdle(); }
-keep class com.google.gson.** { *; }
-keep class androidx.concurrent.futures.CallbackToFutureAdapter$Resolver { *; }
-keep class androidx.concurrent.futures.CallbackToFutureAdapter$Completer { *; }
-keep class androidx.concurrent.futures.CallbackToFutureAdapter { *; }
-keep class androidx.concurrent.futures.ResolvableFuture { *; }
-keep class com.google.common.util.concurrent.ListenableFuture { *; }
