@rem SET JAVA_HOME=C:\DEV\COMP\JAVA\jdk1.7
SET JAVA_HOME=C:\DEV\COMP\Java\jdk1.8.0_111
SET ANT_HOME=C:\DEV\Tools\apache-ant-1.9.4
SET NDK_HOME=C:\DEV\SDKs\android-ndk-r10d
@rem SET ANT_OPTS=-Dfile.encoding=UTF-8
SET ANT_OPTS=-Dfile.encoding=EUC-KR
SET ANDROID_DEV_HOME=C:\DEV\SDKs\ANDROID\tools
SET ANDROID_PLATFORM_HOME=C:\DEV\SDKs\ANDROID\platform-tools
SET PATH=%NDK_HOME%;%JAVA_HOME%\BIN;%ANT_HOME%\bin;.;%ANDROID_DEV_HOME%;%ANDROID_PLATFORM_HOME%;%path%
SET CLASSPATH=.