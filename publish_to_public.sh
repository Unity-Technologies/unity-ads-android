#!/bin/bash

set -e
set -v
set -x

PUBLIC_REPO_NAME=unity-ads-android
PUBLIC_REPO_OWNER=Unity-Technologies

if [ -z "$TRAVIS_TAG" ]; then
  echo "Not a tag, skipping publishing";
else

  if [ -z "$GITHUB_RELEASE_TOKEN" ]; then
    echo "No GITHUB_RELEASE_TOKEN set, skipping publishing"
    exit 1
  fi

  echo "Doing publish to public Github repository";
  zip -r snapshot.zip *
  git clone https://${GITHUB_RELEASE_TOKEN}@github.com/${PUBLIC_REPO_OWNER}/${PUBLIC_REPO_NAME}
  unzip -o snapshot.zip -d ${PUBLIC_REPO_NAME}
  cp public_README.md ${PUBLIC_REPO_NAME}/README.md
  rm ${PUBLIC_REPO_NAME}/public_README.md
  cd ${PUBLIC_REPO_NAME}
  git config user.email "travis@foo.unity3d.com"
  git config user.name "Unity Ads Travis"
  git add -A
  git commit -m "Release ${TRAVIS_TAG}"
  git tag $TRAVIS_TAG
  git push
  git push --tags

  sleep 5  # Waits 5 seconds for Github to set tag, otherwise the release will fail

  # push a Github release to public repo
  RESPONSE=$(curl -H "Content-Type: application/json" \
     -H "Authorization: token ${GITHUB_RELEASE_TOKEN}" \
     -X POST -d "{\"tag_name\":\"${TRAVIS_TAG}\",\"name\":\"Unity Ads ${TRAVIS_TAG}\"}" \
     https://api.github.com/repos/${PUBLIC_REPO_OWNER}/${PUBLIC_REPO_NAME}/releases)

  echo $RESPONSE

  UPLOAD_URL=$(echo $RESPONSE | jq .upload_url | tr -d '"')

  # prepare the upload url from the upload template url
  TEMPLATE_SUFFIX={?name,label}
  BASE_UPLOAD_URL="${UPLOAD_URL%$TEMPLATE_SUFFIX}"

  # upload the install package as asset to Github release
  curl --verbose -H "Content-Type: application/zip" \
       -H "Authorization: token ${GITHUB_RELEASE_TOKEN}" \
       -X POST --data-binary @lib/build/outputs/aar/unity-ads.aar "${BASE_UPLOAD_URL}?name=unity-ads.aar"

  # upload the same thing but renamed to a zip file
  curl --verbose -H "Content-Type: application/zip" \
       -H "Authorization: token ${GITHUB_RELEASE_TOKEN}" \
       -X POST --data-binary @lib/build/outputs/aar/unity-ads.aar "${BASE_UPLOAD_URL}?name=unity-ads.zip"

fi
