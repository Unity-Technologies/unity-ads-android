#############################################################
# This script will use am_instrument_parser from android SDK  #
# to parse android am instruments test raw output into        #
# junit.xml format.                                           #
# Usage: python parseresults.py req1 req2 opt1                #
#   req1: input raw file path                                 #
#   req2: output junit.xml file path                          #
#   opt1: name of Root- test suite (default "Root")           #
#############################################################

import sys
import re
import string
from am_instrument_parser import TestResult
from am_instrument_parser import ParseAmInstrumentOutput

if(len(sys.argv) < 3):
    print("Please provide inputFile and outputFile as arguments!")
    sys.exit()

inputFile = sys.argv[1]
outputFile = sys.argv[2]
rootSuiteName = "Root"
if(len(sys.argv) > 3):
    rootSuiteName = re.sub('[^A-Za-z0-9\.\-_, ]+', '', sys.argv[3])

print("Will read from file '" + inputFile + "' and output to '" +
      outputFile + "'")
print("Using '" + rootSuiteName + "' as test suite root name")

with open(inputFile, "r") as myfile:
    data = myfile.read()

numTests = "-1"
numTestsLine = -1
reNumTests = re.compile(r'INSTRUMENTATION_STATUS: numtests=(\d*)$')
for line in data.splitlines():
    numTestsLine += 1
    if(reNumTests.match(line)):
        numTests = str(reNumTests.search(line).group(1))
        break

print("DEBUG: Number of lines checked to find numTests = '" + str(numTestsLine) + "'")
output, bundle = ParseAmInstrumentOutput(data)
testResults = output

failures = 0
skipped = 0
for result in testResults:
    if(result.GetStatusCode() < 0):
        if(result.GetStatusCode() == -3):
            skipped += 1
        else:
            failures += 1
if(numTests != str(len(testResults))):
    failures += 1

print("Amount of steps = " + str(len(testResults)) +
      " (Claimed " + str(numTests) + ")")

# add the step that checks that all tests have been executed the the number of
# total test steps
totalNumTests = len(testResults) + 1

with open(outputFile, "w") as outfile:
    outfile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    outfile.write("<testsuites name=\"Unit Tests\" tests=\"" + str(totalNumTests) + "\" failures=\"" + str(failures) + "\" skipped=\"" + str(skipped) + "\"" + ">\n")
    outfile.write("\t<testsuite name=\"" + rootSuiteName + "\" tests=\"" + str(totalNumTests) + "\" failures=\"" + str(failures) + "\" skipped=\"" + str(skipped) + "\"" + ">\n")

    for result in testResults:
        outfile.write("\t\t<testcase name=\"" + str(result.GetTestName()) + "\">\n")
        if(result.GetStatusCode() < 0):
            if(result.GetStatusCode() == -3):
                outfile.write("\t\t\t<skipped />\n")
            else:
                outfile.write("\t\t\t<failure> <![CDATA[" + str(result.GetFailureReason()) + "]]></failure>\n")
        outfile.write("\t\t</testcase>\n")
    outfile.write("\t\t<testcase name=\"All tests were executed\">\n")
    if(numTests != str(len(testResults))):
        outfile.write("\t\t\t<failure> <![CDATA[ Expected '" + numTests + "' steps. Got results for '" + str(len(testResults)) + "'" "]]></failure>\n")
    outfile.write("\t\t</testcase>\n")
    outfile.write("\t</testsuite>\n")
    outfile.write("</testsuites>\n")

print(outputFile + " exported")
