release:
	./gradlew :lib:assembleRelease

clean:
	./gradlew :lib:clean

test: push-test-server-address start-test-server exec-tests stop-test-server

test-unit-tests: push-test-server-address start-test-server exec-unit-tests stop-test-server

build-test-apk: clean
	./gradlew :lib:assembleAndroidTest --full-stacktrace

test-emulator: start-test-server exec-tests stop-test-server

test-hosted: push-test-server-address-hosted exec-tests

test-usb: push-test-server-address-local setup-adb-reverse start-test-server exec-only-unit-tests stop-test-server dismantle-adb-reverse

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

start-test-server:
	cd test_server && npm install
	cd test_server && node app.js &

stop-test-server:
	cd test_server && cat access.log
	cd test_server && kill `cat process.pid` && rm process.pid

sonar:
	./gradlew sonarqube --info

javadoc:
	./gradlew :lib:generateReleaseJavadoc
