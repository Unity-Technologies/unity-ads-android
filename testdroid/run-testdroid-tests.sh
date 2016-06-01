#!/bin/bash

# Required environemnt variables:
# TESTDROID_CLIENT_DIR
# TD_API_KEY
# TD_RUN_NAME
# TD_DEVICE_GROUP_ID
# TD_SCHEDULER

echo "SHELL='$SHELL'"
cd "$(dirname "$0")"

set -o xtrace

git clone https://github.com/Applifier/testdroid-ssa-client

TESTDROID_CLIENT_DIR='testdroid-ssa-client'
if [ ! -d "$TESTDROID_CLIENT_DIR" ]; then
  echo "Please make sure testdroid-ssa-client is cloned in a subdirectory"
  exit 2
fi

if [ -z "${TD_API_KEY}" ]; then echo "Please specify testdroid API key!" ; usage ; exit 1 ; fi
if [ -z "${TD_RUN_NAME}" ]; then echo "Please specify testdroid run name!" ; usage ; exit 1 ; fi
if [ -z "${TD_DEVICE_GROUP_ID}" ]; then echo "Please specify testdroid Device group ID!" ; usage ; exit 1 ; fi
if [ -z "${TD_SCHEDULER}" ]; then echo "Please specify testdroid scheduler!" ; usage ; exit 1 ; fi

origdir=$(pwd)
rm -rf "${origdir:?}/testfolder" &&
mkdir "${origdir:?}/testfolder" &&
cp run-tests.sh "$origdir/testfolder" &&
rm -rf android_am_instrument_parser2jUnit-xml
git clone https://github.com/ujappelbe/android_am_instrument_parser2jUnit-xml.git &&
cp android_am_instrument_parser2jUnit-xml/am_instrument_parser.py "$origdir/testfolder" &&
cp android_am_instrument_parser2jUnit-xml/parseresults.py "$origdir/testfolder" &&

cp "../lib/build/outputs/apk/androidTest.apk" "$origdir/androidTest.apk" &&
(
  cd "$TESTDROID_CLIENT_DIR"
  set +o xtrace
  echo -e "running with params:
    ./testdroid_cmdline.sh -u \"(TD_API_KEY-here)\" \t
                           -t \"UnityAds_android\" \t
                           -a \"$origdir/androidTest.apk\" \t
                           -r \"$TD_RUN_NAME\" \t
                           -d \"$TD_DEVICE_GROUP_ID\" \t
                           -c \"$TD_SCHEDULER\" \t
                           -z \"$origdir/testfolder\""

  ./testdroid_cmdline.sh -u "$TD_API_KEY" \
                         -t "UnityAds_android" \
                         -a "$origdir/androidTest.apk" \
                         -r "$TD_RUN_NAME" \
                         -d "$TD_DEVICE_GROUP_ID" \
                         -c "$TD_SCHEDULER" \
                         -z "$origdir/testfolder"
  set -o xtrace
  exitval=$?
  exitval=0
  if [ "$exitval" -ne 0 ]; then echo "testdroid_cmdline returned non zero! '$exitval'" ; exit $exitval ; fi
  RESULTS=$(cat results/*.xml |grep -m 1 'testsuites name="Unit Tests"')
  if [ -z "${RESULTS}" ]; then echo "Could not read the results! RESULTS='$RESULTS'" ; exit 1 ; fi
  if [[ "$RESULTS" =~ tests=\"([[:digit:]]+)\"[[:space:]]*failures=\"([[:digit:]]+)\"[[:space:]]*skipped=\"([[:digit:]]+)\" ]]; then
    echo $BASH_REMATCH
    TESTS="${BASH_REMATCH[1]}"
    FAILED="${BASH_REMATCH[2]}"
    SKIPPED="${BASH_REMATCH[3]}"
    if [ "$TESTS" -lt 1 ]; then
      echo "No tests run!, expected '$TESTS' >= 1"
      exit 200
    fi
    if [ "$FAILED" -ne 0 ]; then
      echo "FAILED test steps! expected '$FAILED' != "
      exit 201
    fi
      echo "Tests run = '$TESTS', failed = '$FAILED', skipped = '$SKIPPED'"
  fi
  exit 0
)
