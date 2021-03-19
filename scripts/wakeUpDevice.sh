#!/bin/bash -e
wakefulness=$(adb shell dumpsys power | grep 'mWakefulness=' | cut -d "=" -f2)

if [[ "$wakefulness" == "Awake" ]] ; then
	echo "Device Awake"
	exit 0;
else
	echo "Waking up device"
	adb shell input keyevent KEYCODE_WAKEUP
fi

adb shell input keyevent 82

displaySB2=$(adb shell dumpsys power | grep 'mHoldingDisplaySuspendBlocker=' | cut -d "=" -f2)
wakeLockSB2=$(adb shell dumpsys power | grep 'mHoldingWakeLockSuspendBlocker='| cut -d "=" -f2)
if [[ "$displaySB2" == "true" && "$wakeLockSB2" == "false" ]] ; then
	echo "Unlocking device with passcode 5168"
	adb shell input text 5168 && adb shell input keyevent 66
fi

if [[ "$wakefulness" == "Awake" ]] ; then
	echo "Device Awake"
	exit 0;
fi