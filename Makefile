release:
	./gradlew :lib:assembleRelease

clean:
	./gradlew :lib:clean

test: test-hosted

test-local: push-test-server-address exec-tests

test-unit-tests: push-test-server-address exec-unit-tests

build-test-apk: clean
	./gradlew :lib:assembleAndroidTest --full-stacktrace

test-emulator: exec-tests

test-hosted: push-test-server-address-hosted exec-tests

test-usb: push-test-server-address-local setup-adb-reverse exec-only-unit-tests dismantle-adb-reverse

exec-tests: exec-unit-tests exec-hybrid-tests

exec-unit-tests: clean
	adb shell input keyevent 82
	./gradlew :lib:connectedCheck --full-stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.UnitTestSuite

exec-hybrid-tests: clean
	adb shell input keyevent 82
	./gradlew :lib:connectedCheck --full-stacktrace -Pandroid.testInstrumentationRunnerArguments.class=com.unity3d.ads.test.HybridTestSuite

push-test-server-address:
	echo http://$(shell ifconfig |grep "inet" |grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" |grep -v -E "^0|^127" -m 1):8080 > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

push-test-server-address-hosted:
	echo "http://terminal.applifier.info:18080" > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

push-test-server-address-local:
	echo "http://localhost:8080" > testServerAddress.txt
	adb push testServerAddress.txt /data/local/tmp

setup-adb-reverse:
	adb reverse tcp:8080 tcp:8080

dismantle-adb-reverse:
	adb reverse --remove-all

javadoc:
	./gradlew :lib:generateReleaseJavadoc

zip: release
	cp lib/build/outputs/aar/unity-ads-release.aar unity-ads.aar
	zip -9r builds.zip unity-ads.aar
	rm unity-ads.aar

use-local-webview:
	sed -i '' 's/return "https:\/\/config.unityads.unity3d.com\/webview\/" + getWebViewBranch() + "\/" + flavor + "\/config.json";/return "new-ip";/' "lib/src/main/java/com/unity3d/ads/properties/SdkProperties.java"
	sed -i '' 's/return ".*";/return "http:\/\/$(shell ifconfig |grep "inet" |grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" |grep -v -E "^0|^127" -m 1):8000\/build\/dev\/config.json";/' "lib/src/main/java/com/unity3d/ads/properties/SdkProperties.java"

use-public-webview:
	sed -i '' 's/return ".*";/return "https:\/\/config.unityads.unity3d.com\/webview\/" + getWebViewBranch() + "\/" + flavor + "\/config.json";/' "lib/src/main/java/com/unity3d/ads/properties/SdkProperties.java"
