function HomeCtrl($scope, Data, Config) {
    // ViewModel
    const vm = this;
    var element = 'Mat-14-22';
    var lang = 'ar';

    var getData = function(config) {
      vm.config = config;
      vm.data = Data.get({
          version: config.version,
          element: element,
      });
      vm.data.image = element;
    };

    Config.get({lang: lang}, getData);
}

export default {
    name: 'HomeCtrl',
    fn: HomeCtrl
};

HomeCtrl.$inject = ['$scope', 'Data', 'Config'];
