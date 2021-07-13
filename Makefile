debug:
	./gradlew :unity-ads:assembleDebug

release:
	./gradlew :unity-ads:assembleRelease

clean:
	./gradlew :unity-ads:clean

coverage:
	./gradlew -i jacocoTestReport --debug

coverage-ci: debug test-ci coverage

zip: release
	cp unity-ads/build/outputs/aar/unity-ads-release.aar unity-ads.aar
	zip -9r unity-ads.aar.zip unity-ads.aar
	rm unity-ads.aar

javadoc:
	./gradlew :unity-ads:javadoc

device-connected:
	adb get-state 1>/dev/null 2>&1 && echo 'Device Attached' && exit 0 || echo 'Device NOT Attached' && exit -1

wake-up-device:
	scripts/wakeUpDevice.sh

test: device-connected wake-up-device run-all-tests

test-local: device-connected wake-up-device push-test-server-address-ip run-all-tests

test-local-webview-staging: device-connected wake-up-device push-test-server-address-staging run-all-tests

test-local-staging-localhost: device-connected wake-up-device push-test-server-address-localhost run-all-tests

run-all-tests: test-instrumentation test-legacy test-integration

test-ci:
	./gradlew unity-ads:connectedDebugAndroidTest -i -w --stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.InstrumentationTestSuite,com.unity3d.ads.test.LegacyTestSuite

test-instrumentation:
	./gradlew unity-ads:connectedDebugAndroidTest -i -w --stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.InstrumentationTestSuite

test-legacy:
	./gradlew unity-ads:connectedDebugAndroidTest -i -w --stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.LegacyTestSuite 

test-integration:
	./gradlew unity-ads:connectedDebugAndroidTest -i -w --stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.IntegrationTestSuite 

push-test-server-address-ip:
	echo http://$(shell ifconfig |grep "inet" |grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" |grep -v -E "^0|^127" -m 1):8080 > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

push-test-server-address-staging:
	echo "https://unity-ads-test-server.unityads.unity3d.com" > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

push-test-server-address-localhost:
	echo "http://localhost:8080" > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

use-local-webview:
	sed -i '' 's/return baseURI + getWebViewBranch() + "\/" + flavor + "\/config.json";/return "new-ip";/' "unity-ads/src/main/java/com/unity3d/services/core/properties/SdkProperties.java"
	sed -i '' 's/return ".*";/return "http:\/\/$(shell ifconfig |grep "inet" |grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" |grep -v -E "^0|^127" -m 1):8000\/build\/" + flavor + "\/config.json";/' "unity-ads/src/main/java/com/unity3d/services/core/properties/SdkProperties.java"

use-public-webview:
	sed -i '' 's/return ".*";/return baseURI + getWebViewBranch() + "\/" + flavor + "\/config.json";/' "unity-ads/src/main/java/com/unity3d/services/core/properties/SdkProperties.java"

create-android-26-emulator:
	${ANDROID_HOME}/tools/bin/sdkmanager --install "system-images;android-26;google_apis;x86"
	echo "no" | ${ANDROID_HOME}/tools/bin/avdmanager create avd --name "android-26-test" --package "system-images;android-26;google_apis;x86" --device "Nexus 6P" --tag google_apis --abi google_apis/x86 --force

start-android-26-emulator:
	${ANDROID_HOME}/emulator/emulator -port 5556 -avd android-26-test -no-window -noaudio -no-boot-anim -memory 2048 -partition-size 1024 &

kill-emulator:
	adb emu kill