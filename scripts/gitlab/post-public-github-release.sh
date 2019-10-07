set -o xtrace

CURRENT_COMMIT_ID=${CI_COMMIT_SHA};
echo "CURRENT_COMMIT_ID=$CURRENT_COMMIT_ID";

# TODO get correct REF from public repo
echo "CI_COMMIT_REF_NAME=$CI_COMMIT_REF_NAME";
[[ $CI_COMMIT_REF_NAME =~ ^release\/([0-9\.]*)$ ]];
RELEASE_VERSION=${BASH_REMATCH[1]};
if [[ ! $RELEASE_VERSION ]]; then
    [[ $CI_COMMIT_REF_NAME =~ ^feature\/ci$ ]];
    RELEASE_VERSION=${BASH_REMATCH[0]};
fi
echo "RELEASE_VERSION=$RELEASE_VERSION";

make verify-release-build;

if [[ $CURRENT_COMMIT_ID && $RELEASE_VERSION && $GITHUB_PERSONAL_ACCESS_TOKEN ]]; then
    CREATE_RELEASE_RESPONSE=$(curl -X POST --fail \
        -H "Authorization: token $GITHUB_PERSONAL_ACCESS_TOKEN" \
        -d "{ \"tag_name\": \"${RELEASE_VERSION}\", \"target_commitish\": \"${CURRENT_COMMIT_ID}\", \"name\": \"Unity Ads ${RELEASE_VERSION}\", \"body\": \"Unity Ads ${RELEASE_VERSION}\", \"draft\": true }" \
        "https://api.github.com/repos/Unity-Technologies/unity-ads-android/releases");
    UPLOAD_URL=$(echo "$CREATE_RELEASE_RESPONSE" | jq -r '.upload_url' | cut -f1 -d"{")
    if [[ UPLOAD_URL && UPLOAD_URL != null ]]; then
        echo "UPLOAD_URL=$UPLOAD_URL";
        curl -X POST --fail \
            -H "Authorization: token $GITHUB_PERSONAL_ACCESS_TOKEN" \
            -H "Content-Type: application/zip" \
            --data-binary @unity-ads.aar.zip \
            "$UPLOAD_URL?name=unity-ads.aar.zip&label=unity-ads.aar.zip";
    else
        echo "UPLOAD_URL not found";
        exit 1;
    fi
else
    echo "CURRENT_COMMIT_ID or RELEASE_VERSION or GITHUB_PERSONAL_ACCESS_TOKEN not found";
    exit 1;
fi
