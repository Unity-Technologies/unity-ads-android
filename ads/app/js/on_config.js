function OnConfig($stateProvider, $locationProvider, $urlRouterProvider, $httpProvider) {
    'ngInject';

    $locationProvider.html5Mode({
      enabled: true,
      requireBase: false
    });

    $stateProvider
        .state('Home', {
            url: '/',
            controller: 'HomeCtrl as home',
            templateUrl: 'home.html',
            title: 'Home'
        });

    $urlRouterProvider.otherwise('/');

    $httpProvider.interceptors.push(($rootScope) => {
      return {
        request: config => {
          console.log(config.url + ' ' + $rootScope.baseUrl);
          config.url = ($rootScope.baseUrl || '') + config.url;
          return config;
        }
      };
    });

}

export default OnConfig;
