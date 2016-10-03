#!/bin/bash

# Prints all merged github pull requests after last release tag

REPOURL=$(git config --get remote.origin.url)
if [[ $REPOURL != https* ]]; then
	SSH_PREFIX="git@github.com:"
	REPOURL=${REPOURL%.git}
	REPOURL="https://github.com/${REPOURL#$SSH_PREFIX}"
fi

CURRENT_TAG=$(git describe --abbrev=0 --tags)
PREVTAG=$(git describe --abbrev=0 --tags ${CURRENT_TAG}~1)
COMMIT=$(git rev-parse $PREVTAG)
git log $PREVTAG..HEAD --merges --grep "Merge pull request #" | grep "^    Merge pull request #" -A2 | grep -v "^--" | while read FOO1 FOO2 FOO3 PRNUMBER FOO5 PRBRANCH; do
	read FOO
	read PRTITLE

	PRNUMBER=$(echo $PRNUMBER | tr -d '#')
	PRLINK=$REPOURL/pull/$PRNUMBER
	printf "[PR#%b: %b](%b)\n" "$PRNUMBER" "$PRTITLE" "$PRLINK"
done
