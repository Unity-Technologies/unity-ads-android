import rwc from 'random-weighted-choice';
import aes from 'crypto-js/aes';
import _ from 'lodash';

function HomeCtrl($location, $cookies, $scope, $interval, Culture, Word, Config, Level, Books) {
    'ngInject';

    const vm = this;
    $scope.vm = vm;

    vm.isWebView = window.webviewbridge;
    vm.prefix = '';
    vm.assets = '';
    // vm.id = 'Jn-14-1';
    const lang = 'en';

    var setupTimer = () => $interval((i) => {
      vm.timer = 5 - i;
      if(i == 5) {
        vm.showClose = true;
      }
    }, 1000, 5, true);

    var setupBridge = () => {

      var loaded = [
          ['com.wds.ads.api.Sdk', 'loadComplete', [], 'onLoadComplete']
      ];
      var init = false;

      window.nativebridge = {
          handleCallback: (params) => {
              console.log('handleCallback: ' + JSON.stringify(params));

              _.forEach(params, (paramArray) => {
                  if (paramArray[0] == 'onLoadComplete') {
                      console.log('onLoadComplete invoked: ' + JSON.stringify(paramArray));
                      var config = paramArray[2];
                      $scope.$apply((scope) => {
                          vm.assets = config[config.length - 2];
                          vm.prefix = config[config.length - 1];
                          scope.$root.baseUrl = vm.prefix;
                      });
                  }
              });

              if (!init) {
                  var inited = [
                      ['com.wds.ads.api.Sdk', 'initComplete', [], 'onInitComplete']
                  ];
                  window.webviewbridge
                      .handleInvocation(JSON.stringify(inited));
                  var ready = [
                      ['com.wds.ads.api.Listener', 'sendReadyEvent', ['defaultVideoAndPictureZone'], 'CALLBACK_01'],
                      ['com.wds.ads.api.Placement', 'setPlacementState', ['defaultVideoAndPictureZone', 'READY'], 'CALLBACK_02']
                  ];
                  window.webviewbridge
                      .handleInvocation(JSON.stringify(ready));
              }
              init = true;

          },
          handleInvocation: (params) => {
              console.log('handleInvocation', params);
              if (params[0] == 'webview' && params[1] == 'show') {
                  var openAdUnit = [
                      ['com.wds.ads.api.AdUnit', 'open', [1, ['webview'], -1], 'CALLBACK_02']
                  ];
                  window.webviewbridge
                      .handleInvocation(JSON.stringify(openAdUnit));
                  console.log('opened adunit');

                  vm.pick();

                  window.webviewbridge
                      .handleCallback(params[2], 'OK', JSON.stringify([]));
                  $scope.$apply((scope) => {
                    scope.close = () => {
                      var closeAdUnit = [
                          ['com.wds.ads.api.AdUnit', 'close', [], 'onClosed']
                      ];
                      window.webviewbridge
                        .handleInvocation(JSON.stringify(closeAdUnit));
                    };
                    setupTimer();
                  });
              }
          },
          handleEvent: (params) => {
              console.log('handleEvent', params);
          }
      };
      window.webviewbridge
          .handleInvocation(JSON.stringify(loaded));
    }

    if(window.webviewbridge) {
      setupBridge();
    } else {
      setupTimer();
    }

    var readByWeight = _.reduce(vm.read, (result, value, key) => {
        result[value.weight] = (result[value.weight] || 0) + value.value;
        return result;
    }, {});

    var setRead = (element, value) => {
      vm.read[element.id] = {
          value: value,
          weight: element.rank
      };
      $cookies.putObject('read', vm.read);
      if(window.webviewbridge) {
        var updateRead = [
            ['com.wds.ads.api.Read', 'read', [], 'onReadUpdated']
        ];
        window.webviewbridge
          .handleInvocation(JSON.stringify(updateRead));
      }
    };

    var onRead = (element, value) => {
      setRead(element, value);

      if(!vm.isWebView) {
        return;
      }

      var launchWeb = [
          ['com.wds.ads.api.Intent', 'launch', [{
            action: 'android.intent.action.VIEW',
            uri: vm.readUrl
          }], 'onReadUpdated']
      ];
      window.webviewbridge
        .handleInvocation(JSON.stringify(launchWeb));
    };

    var interpolate = (string, params) => {
      var parts = string.split('//');
      var result = [parts[0] + '//'];
      _.forEach((parts[1] || '').split(':'), (segment, i) => {
        if (i === 0) {
          result.push(segment);
        } else {
          var segmentMatch = segment.match(/(\w+)(?:[?*])?(.*)/);
          var key = segmentMatch[1];
          result.push(params[key]);
          result.push(segmentMatch[2] || '');
          delete params[key];
        }
      });
      return result.join('');
    };

    $scope.read = element => onRead(element, 1);
    $scope.$on('read', ($evt) => setRead(vm.element, .5));

    $scope.$watchGroup(['vm.config.version', 'vm.element'], val => {
        if (!val[0] || !(val[0] && val[1])) return;

        var location = vm.id.split('-');
        var book = _.find(vm.books, {abbr: location[0]});

        vm.readUrl = interpolate(vm.config.url, {
          abbr: location[0],
          chapter: location[1],
          verse: location[2],
          version: vm.config.version,
          ord: book.ord
        });

        if(vm.word) {
          vm.word.verse = '';
        }

        vm.word = Word.get({
            version: vm.config.version,
            element: vm.id,
        });
        vm.image = vm.id;
    });

    // todo: move this?
    $scope.$watchCollection('vm.levels', val => {
        if (!val || val.length == 0) return;
        $scope.$watch('vm.read', val => {
          console.log('read updated');
          if(!vm.levels) {
            return;
          }

          var addWeight = 0;
          vm.weightTable = _.reduce(vm.levels, (result, level, idx) => {
              var nextLevelDiff = vm.levels.length > idx + 1 ?
                  level.rank - vm.levels[idx + 1].rank : level.rank / 2;
              _.each(level.elements, id => {
                  var subtractWeight = (vm.read[id] || {
                      value: 0
                  }).value * nextLevelDiff;
                  result.push({
                      rank: level.rank,
                      weight: level.rank + addWeight - subtractWeight,
                      id: id
                  });
              });

              var percentComplete = (readByWeight[level.rank] / level.elements.length);
              addWeight = nextLevelDiff * percentComplete || 0;
              return result;
          }, []);
          if(window.webviewbridge) {
            return;
          }

          vm.pick();
        }, true);

        vm.read = $cookies.getObject('read') || {};
        console.log('read cookie: ' + JSON.stringify(vm.read));
    });

    vm.pick = () => {
      if(!vm.weightTable) {
        return;
      }

      var randomId = rwc(vm.weightTable);
      vm.element = _.find(vm.weightTable, {
          id: randomId
      });
      vm.id = randomId;
    };

    $scope.$watch('vm.prefix', val => {
      vm.culture.$promise.then(() => {
        var detectedLang = navigator.language.split('-')[0];

        var params = {
          lang: vm.culture[detectedLang] ? detectedLang : lang
        };

        vm.config = Config.get(params, config => {
          if(config.url) {
            return;
          }

          config.url = vm.defaultConfig.url;
        });

        vm.levels = Level.query();
      });
    });

    var loadDefaultConfig = () => {
      vm.culture = Culture.get();
      vm.defaultConfig = Config.get({ lang: 'default' });
      vm.books = Books.query();
    };

    $scope.$root.$watch('baseUrl', (val) => {
      if(!val) {
        return;
      }
      loadDefaultConfig();
    });

    loadDefaultConfig();
}

export default {
    name: 'HomeCtrl',
    fn: HomeCtrl
};
