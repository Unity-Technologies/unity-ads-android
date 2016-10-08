import random from 'weighted-random';
import _ from 'lodash';

function HomeCtrl($scope, Word, Config, Level) {
    const vm = this;
    $scope.vm = vm;

    vm.element = 'Ex-14-10';
    var lang = 'en';

    $scope.$watchGroup(['vm.config.version', 'vm.element'], function(val) {
        if (!val[0]) return;
        vm.word = Word.get({
            version: vm.config.version,
            element: vm.element,
        });
        vm.image = vm.element;
    });

    $scope.$watchCollection('vm.levels', function(val) {
        if (val.length == 0) return;

        var levels = _.reduce(vm.levels, function(result, level) {
            _.each(level.elements, function(element) {
                result.push({
                    rank: level.rank,
                    element: element
                });
            });
            return result;
        }, []);

        var mapped = _.map(levels, 'rank');
        var index = random(mapped);

        // vm.element = levels[index].element;

        // more x in 1000, less 1000
        //

        // if x all 1000
        // 800, 900 > 1000 (more likely to see 900 and then 1000)

    });

    vm.config = Config.get({lang: lang});
    vm.levels = Level.query();

}

export default {
    name: 'HomeCtrl',
    fn: HomeCtrl
};

HomeCtrl.$inject = ['$scope', 'Word', 'Config', 'Level'];
