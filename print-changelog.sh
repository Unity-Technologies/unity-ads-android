#!/bin/bash

# Prints all merged github pull requests after last release tag

REPOURL=$(git info|awk -F= '/^remote.origin.url=/ {print $2}')
PREVTAG=$(git tag|sed -n '$p')
COMMIT=$(git rev-parse $PREVTAG)
git log $PREVTAG..HEAD --merges --grep "Merge pull request #" | grep "^    Merge pull request #" -A2 | grep -v "^--" | while read FOO1 FOO2 FOO3 PRNUMBER FOO5 PRBRANCH; do
	read FOO
	read PRTITLE

	PRNUMBER=$(echo $PRNUMBER | tr -d '#')
	PRLINK=$REPOURL/pull/$PRNUMBER
	printf "[PR#%b: %b](%b)\n" "$PRNUMBER" "$PRTITLE" "$PRLINK"
done
