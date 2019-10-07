set -o xtrace

echo "no" | ${ANDROID_HOME}/tools/bin/avdmanager create avd --name "android-26-test" --package "system-images;android-26;google_apis;x86" --device "Nexus 6P" --tag google_apis --abi google_apis/x86 --force
${ANDROID_HOME}/emulator/emulator -port 5556 -avd android-26-test -no-window -noaudio -no-boot-anim -memory 2048 -partition-size 1024 2>&1 &
echo $! > emulator_PID

# Wait for Android to finish booting
BOOTCOMPLETE_WAIT_CMD="$ANDROID_HOME/platform-tools/adb wait-for-device shell getprop dev.bootcomplete"
BOOT_COMPLETED_WAIT_CMD="$ANDROID_HOME/platform-tools/adb wait-for-device shell getprop sys.boot_completed"
BOOT_ANIM_WAIT_CMD="$ANDROID_HOME/platform-tools/adb wait-for-device shell getprop init.svc.bootanim"

until [[ $( $BOOTCOMPLETE_WAIT_CMD | grep -m 1 1 ) && $( $BOOT_COMPLETED_WAIT_CMD | grep -m 1 1 ) && $( $BOOT_ANIM_WAIT_CMD | grep -m 1 stopped ) ]]; do
  echo "Waiting for emulator..."
  sleep 1
done

# Unlock the Lock Screen
$ANDROID_HOME/platform-tools/adb shell input keyevent 82

# Run instrumentation test
bundle exec fastlane instrumentation_test
TEST_EXIT=$?

# Stop the background processes
kill $(head -n 1 emulator_PID)

rm -rf emulator_PID

exit $TEST_EXIT
