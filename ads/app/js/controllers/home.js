import rwc from 'random-weighted-choice';
import _ from 'lodash';

function HomeCtrl($cookies, $scope, Word, Config, Level) {
  const vm = this;
  $scope.vm = vm;

  vm.id = 'Ex-14-10';
  var lang = 'en';

  var read = $cookies.getObject('read') || {};
  var readByWeight = _.reduce(read, (result,value,key) => {
    result[value.weight] = (result[value.weight] || 0) + value.value;
    return result;
  },{});

  var onRead = (element, value) => {
    read[element.id] = {
      value: value,
      weight: element.rank
    };
    $cookies.putObject('read', read);
  }

  $scope.read = element => onRead(element, 1);
  $scope.$on('read', ($evt) => onRead(vm.element, .5));

  $scope.$watchGroup(['vm.config.version', 'vm.element'], val => {
    if (!val[0]) return;
    vm.word = Word.get({
      version: vm.config.version,
      element: vm.id,
    });
    vm.image = vm.id;

  });

  $scope.$watchCollection('vm.levels', val => {
    if (val.length == 0) return;
    var addWeight = 0;
    var levels = _.reduce(vm.levels, (result, level, idx) => {
      console.log('add', addWeight);
      var nextLevelDiff = vm.levels.length > idx + 1 ?
        level.rank - vm.levels[idx + 1].rank : level.rank / 2;

      console.log('sub', nextLevelDiff);
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

    console.log(levels);
    var randomId = rwc(levels);
    vm.element = _.find(levels, {id: randomId});
    console.log(vm.element);
  });

  vm.config = Config.get({lang: lang});
  vm.levels = Level.query();
}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};

HomeCtrl.$inject = ['$cookies', '$scope', 'Word', 'Config', 'Level'];
