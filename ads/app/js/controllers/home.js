function HomeCtrl($scope, Word, Config) {
    // ViewModel
    const vm = this;
    var element = 'Mat-6-19';
    var lang = 'es';

    var getWord = function(config) {
      vm.config = config;
      vm.word = Word.get({
          version: config.version,
          element: element,
      });

      vm.word.image = element;
    };

    Config.get({lang: lang}, getWord);
}

export default {
    name: 'HomeCtrl',
    fn: HomeCtrl
};

HomeCtrl.$inject = ['$scope', 'Word', 'Config'];
