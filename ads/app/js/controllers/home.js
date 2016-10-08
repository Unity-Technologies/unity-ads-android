import random from "weighted-random";
import _ from "lodash";

function HomeCtrl($cookies, $scope, Word, Config, Level) {
  const vm = this;
  $scope.vm = vm;

  vm.id = 'Ex-14-10';
  var lang = 'en';

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

    var levels = _.reduce(vm.levels, function (result, level) {
      _.each(level.elements, function (element) {
        result.push({
          weight: level.rank,
          id: element
        });
      });
      return result;
    }, []);

    console.log(levels);

    var mapped = _.map(levels, 'rank');
    var index = random(mapped);

    
    // vm.element = levels[index].element;
  });

  vm.config = Config.get({lang: lang});
  vm.levels = Level.query();

}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};

HomeCtrl.$inject = ['$cookies', '$scope', 'Word', 'Config', 'Level'];
