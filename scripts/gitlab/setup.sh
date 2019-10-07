set -o xtrace

brew update
brew install rbenv
if ! grep -q 'export PATH="$HOME/.rbenv/bin:$PATH"' ~/.bash_profile; then
    echo 'export PATH="$HOME/.rbenv/bin:$PATH"' >> ~/.bash_profile
fi
if ! grep -q 'eval "$(rbenv init -)"' ~/.bash_profile; then
	echo 'eval "$(rbenv init -)"' >> ~/.bash_profile
fi
RUBY_VERSION=$(cat .ruby-version)
source ~/.bash_profile
echo < cat .ruby-version
echo n | rbenv install $RUBY_VERSION
rbenv shell $RUBY_VERSION
gem install bundler
bundle install

echo y | $ANDROID_HOME/tools/bin/sdkmanager --update
echo y | $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-26" \
    "build-tools;28.0.2" \
    "extras;google;m2repository" \
    "extras;android;m2repository" \
    "extras;google;google_play_services" \
    "sources;android-26" \
    "platform-tools" \
    "emulator" \
    "tools" \
    "system-images;android-26;google_apis;x86"
