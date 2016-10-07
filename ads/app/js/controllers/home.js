function HomeCtrl($scope, Word, Config) {
    const vm = this;
    var element = 'Mat-17-1';
    var lang = 'en';

    var getWord = function(config) {
      vm.config = config;
      vm.word = Word.get({
          version: config.version,
          element: element,
      });

      vm.image = element;
    };

    Config.get({lang: lang}, getWord);
}

export default {
    name: 'HomeCtrl',
    fn: HomeCtrl
};

HomeCtrl.$inject = ['$scope', 'Word', 'Config'];
