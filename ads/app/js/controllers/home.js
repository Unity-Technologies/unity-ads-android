import rwc from 'random-weighted-choice';
import aes from 'crypto-js/aes';
import _ from 'lodash';

function HomeCtrl($location, $cookies, $scope, Word, Config, Level) {
  'ngInject';

  const vm = this;
  $scope.vm = vm;

  vm.prefix = '';
  vm.id = 'Jn-14-1';
  const lang = 'th';

  var loaded = [
    ['com.wds.ads.api.Sdk','loadComplete', [], 'onLoadComplete']
  ];
  var init = false;
  window.nativebridge = {
    handleCallback: (params) => {
      console.log('handleCallback: ' + JSON.stringify(params));

      _.forEach(params, (paramArray) => {
        if(paramArray[0] == 'onLoadComplete') {
          console.log('onLoadComplete invoked: ' + JSON.stringify(paramArray));
          var config = paramArray[2];
          $scope.$apply((scope) => {
            vm.prefix = _.last(config);
            scope.$root.baseUrl = vm.prefix;
          });
        }
      });

      if(!init) {
        var inited = [['com.wds.ads.api.Sdk','initComplete', [], 'onInitComplete']];
        window.webviewbridge
          .handleInvocation(JSON.stringify(inited));
        var ready = [
          ['com.wds.ads.api.Listener','sendReadyEvent', ['defaultVideoAndPictureZone'], 'CALLBACK_01'],
          ['com.wds.ads.api.Placement', 'setPlacementState', ['defaultVideoAndPictureZone', 'READY'], 'CALLBACK_02']
        ];
        window.webviewbridge
          .handleInvocation(JSON.stringify(ready));
      }
      init = true;

    },
    handleInvocation: (params) => {
      console.log('handleInvocation', params);
      if(params[0] == 'webview' && params[1] == 'show') {
        var openAdUnit = [['com.wds.ads.api.AdUnit','open', [1, ['webview'], 0], 'CALLBACK_02']];
        window.webviewbridge
          .handleInvocation(JSON.stringify(openAdUnit));
        console.log('opened adunit');
      }

      _nativeBridge.AndroidAdUnit.open(1, [ 'videoplayer', 'webview' ], e, n, 1, i);
      // on show, gives time to get ready
      window.webviewbridge
        .handleCallback(params[2], 'OK', JSON.stringify([]));
    },
    handleEvent: (params) => {
      console.log('handleEvent', params);
    }
  };
  window.webviewbridge
    .handleInvocation(JSON.stringify(loaded));

  var read = $cookies.getObject('read') || {};
  var readByWeight = _.reduce(read, (result,value,key) => {
    result[value.weight] = (result[value.weight] || 0) + value.value;
    return result;
  },{});

  var onRead = (element, value) => {
    console.log('read');
    read[element.id] = {
      value: value,
      weight: element.rank
    };
    $cookies.putObject('read', read);
  }

  $scope.read = element => onRead(element, 1);
  $scope.$on('read', ($evt) => onRead(vm.element, .5));

  $scope.$watchGroup(['vm.config.version', 'vm.element'], val => {
    if (!val[0] || !(val[0] && val[1])) return;

    vm.word = Word.get({
      version: vm.config.version,
      element: vm.id,
    });
    vm.image = vm.id;

  });

  // todo: move this?
  $scope.$watchCollection('vm.levels', val => {
    if (!val || val.length == 0) return;
    var addWeight = 0;
    var levels = _.reduce(vm.levels, (result, level, idx) => {
      var nextLevelDiff = vm.levels.length > idx + 1 ?
        level.rank - vm.levels[idx + 1].rank : level.rank / 2;

      _.each(level.elements, id => {
        var subtractWeight = (read[id] || {value: 0}).value * nextLevelDiff;
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

    var randomId = rwc(levels);
    vm.element = _.find(levels, {id: randomId});
    // todo: switch element once its selected
    // vm.id = randomId;

    console.log(vm.element, levels);

  });

  $scope.$watch('vm.prefix', val => {
    console.log(val);
    vm.config = Config.get({lang: lang});
    vm.levels = Level.query({});
  });

}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};
