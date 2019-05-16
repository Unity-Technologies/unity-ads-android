# unity-ads-android
Unity Ads Android Development Repository

### Execute unit tests

1. Connect Android device or simulator
1. `make test`
1. Coverage report available at `lib/build/reports/coverage/debug/index.html`

### Build example application

1. `./gradlew assemble`
1. Find example app in the `app/build/outputs/apk/` subfolder

### Releasing

Step 1. Make a staged commit by removing ./publish_to_public.sh from the end of .travis.yml

Step 2. Tag the release, push tag to internal repository, Travis will automatically publish a release in internal repository under the releases tab

Step 3. Final testing with QA

Step 4. Add ./publish_to_public.sh back to .travis.yml, re-tag the release and push tag

#### Updating staged binaries

If issues are found during testing or other changes need to be made before pushing the release to public repository, you need to update the staged binaries.

Step 1. Delete the release from internal repository

Step 2. Continue with release steps 2 and 3
