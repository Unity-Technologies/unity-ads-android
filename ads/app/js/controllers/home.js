import rwc from 'random-weighted-choice';
import _ from 'lodash';

function HomeCtrl($cookies, $scope, Word, Config, Level) {
  const vm = this;
  $scope.vm = vm;

  vm.id = 'Jn-14-1';
  var lang = 'en';

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
    if (val.length == 0) return;
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

  vm.config = Config.get({lang: lang});
  vm.levels = Level.query();
}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};

HomeCtrl.$inject = ['$cookies', '$scope', 'Word', 'Config', 'Level'];
