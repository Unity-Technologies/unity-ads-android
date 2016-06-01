#!/bin/bash

# Take a screenshot from connected android device into a child-folder named 'screenshots'
# Arg1: screenshot filename
function take_screenshot {
  adb shell screencap -p /sdcard/screencap.png &&
  adb pull /sdcard/screencap.png &&
  mkdir screenshots
  mv screencap.png "screenshots/$1"
}

set -o xtrace
echo "Extracting tests.zip..."
unzip tests.zip

test_port="18080"
test_server="http://terminal.applifier.info:$test_port"
TEST_RESULTS_FILE="test_results.txt"

echo "pinging google to check connectivity"
adb shell ping -c 3 google.fi
echo "testing browser"
adb shell am start -a android.intent.action.VIEW -d "$test_server/google.png"
sleep 3
echo "take screenshot"
take_screenshot "browser1.png"
adb shell input keyevent 4
sleep 2
echo "testing browser"
adb shell am start -a android.intent.action.VIEW -d "$test_server/google.png" --ez create_new_tab true
sleep 3
echo "take screenshot"
take_screenshot "browser2.png"

# Play store needs to be opened once to setup some stuff on device required for ads.
echo "Open play store"
adb shell am start -a android.intent.action.VIEW -d  market://details?id=com.glu.stardomkim
sleep 2
echo "take screenshot"
take_screenshot "play-store.png"


# Push server address to device
echo "$test_server" > testServerAddress.txt
test -e testServerAddress.txt && adb push testServerAddress.txt /data/local/tmp || true
echo "Testserver address = '$(cat testServerAddress.txt)'"

adb shell input keyevent 82
testapk="application.apk"
echo "test application '$testapk': $(ls -la $testapk)"
echo "Installing apk"
adb install -r "$testapk"
echo "Installed"


echo "*** Listing instrumentation on device: "
adb shell pm list instrumentation
echo "*** Listing installed packages"
adb shell pm list packages
echo "***"

echo "Send keyevent to open screen"
adb shell input keyevent 82

echo "**************************"
echo "**************************"
echo "*** Starting Unit test ***"
echo "**************************"
echo "**************************"
echo "*** [$(date)] ***"

# We cannot risk running out of time or we have no results and TD leaves process stuck.
(sleep 300 ; echo "[$(date)] 300s spent... Killing all adb!" ; killall adb ; sleep 2 ; killall -9 adb) &

echo "*********"
echo "*********"
echo "*********"
echo "*********"

adb shell am instrument -r -w -e class com.unity3d.ads.test.UnitTestSuite,com.unity3d.ads.test.HybridTestSuite com.unity3d.ads.test/android.support.test.runner.AndroidJUnitRunner 2>&1 |tee $TEST_RESULTS_FILE

echo "[$(date)]Test Done!"
echo "parsing results"
echo "which python = '$(which python 2>&1)'"
echo "Python version = '$(python --version 2>&1)'"
adb devices
phone_name="td-$(adb shell getprop ro.product.manufacturer | tr -d '[[:space:]]')-$(adb shell getprop ro.product.model | tr -d '[[:space:]]')"
echo "Phone name is '$phone_name'"
echo "Running 'python parseresults.py \"$TEST_RESULTS_FILE\" \"$phone_name\" TEST-all.xml 2>&1'"
echo "$(python parseresults.py "$TEST_RESULTS_FILE" TEST-all.xml \"$phone_name\" 2>&1)"
echo "The test results as junit xml:"
echo "$(cat TEST-all.xml)"
echo "Listing files"
ls -la
echo "Listing screenshots dir"
ls -la screenshots/
echo "$TEST_RESULTS_FILE contains:"
cat $TEST_RESULTS_FILE

echo "Jobs running: $(jobs)"
echo "Jobs running: $(jobs -p)"
echo "Killing all background jobs"
kill "$(jobs -p)"
sleep 5
echo "Jobs running: $(jobs)"
echo "Killing harder"
kill -9 "$(jobs -p)"
sleep 5
echo "Jobs running: $(jobs)"
echo "killing adb"
adb kill-server
killall adb
