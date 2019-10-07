release:
	./gradlew :unity-ads:assembleRelease

clean:
	./gradlew :unity-ads:clean

test: test-hosted

test-local: push-test-server-address exec-tests

test-unit-tests: push-test-server-address exec-unit-tests

build-test-apk: clean
	./gradlew :unity-ads:assembleAndroidTest --full-stacktrace

test-emulator: exec-tests

test-hosted: push-test-server-address-hosted exec-tests

test-usb: push-test-server-address-local setup-adb-reverse exec-only-unit-tests dismantle-adb-reverse

exec-tests: exec-unit-tests exec-hybrid-tests

exec-unit-tests: clean
	adb shell input keyevent 82
	./gradlew :unity-ads:connectedCheck --full-stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.LegacyTestSuite

exec-hybrid-tests: clean
	adb shell input keyevent 82
	./gradlew :unity-ads:connectedCheck --full-stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.HybridTestSuite

push-test-server-address:
	echo http://$(shell ifconfig |grep "inet" |grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" |grep -v -E "^0|^127" -m 1):8080 > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

push-test-server-address-hosted:
	echo "http://unity-ads-test-server.unityads.unity3d.com" > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

push-test-server-address-local:
	echo "http://localhost:8080" > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

setup-adb-reverse:
	adb reverse tcp:8080 tcp:8080

dismantle-adb-reverse:
	adb reverse --remove-all

javadoc:
	./gradlew :unity-ads:javadoc

zip: release
	cp unity-ads/build/outputs/aar/unity-ads-release.aar unity-ads.aar
	zip -9r unity-ads.aar.zip unity-ads.aar
	rm unity-ads.aar

verify-release-build:
	if [[ -f "unity-ads.aar.zip" ]]; then \
		echo 'unity-ads.aar.zip exists'; \
	else \
		echo 'unity-ads.aar.zip does not exist'; \
		exit 1; \
	fi;

use-local-webview:
	sed -i '' 's/return "https:\/\/config.unityads.unity3d.com\/webview\/" + getWebViewBranch() + "\/" + flavor + "\/config.json";/return "new-ip";/' "unity-ads/src/main/java/com/unity3d/services/core/properties/SdkProperties.java"
	sed -i '' 's/return ".*";/return "http:\/\/$(shell ifconfig |grep "inet" |grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" |grep -v -E "^0|^127" -m 1):8000\/build\/" + flavor + "\/config.json";/' "unity-ads/src/main/java/com/unity3d/services/core/properties/SdkProperties.java"

use-public-webview:
	sed -i '' 's/return ".*";/return "https:\/\/config.unityads.unity3d.com\/webview\/" + getWebViewBranch() + "\/" + flavor + "\/config.json";/' "unity-ads/src/main/java/com/unity3d/services/core/properties/SdkProperties.java"

create-android-26-emulator:
	echo "no" | ${ANDROID_HOME}/tools/bin/avdmanager create avd --name "android-26-test" --package "system-images;android-26;google_apis;x86" --device "Nexus 6P" --tag google_apis --abi google_apis/x86 --force

start-android-26-emulator:
	${ANDROID_HOME}/emulator/emulator -port 5556 -avd android-26-test -no-window -noaudio -no-boot-anim -memory 2048 -partition-size 1024 &
