function OnConfig($stateProvider, $locationProvider, $urlRouterProvider) {
    'ngInject';

    // $locationProvider.html5Mode(true);

    $stateProvider
        .state('Home', {
            url: '/',
            controller: 'HomeCtrl as home',
            templateUrl: 'home.html',
            title: 'Home'
        });

    $urlRouterProvider.otherwise('/');

}

export default OnConfig;
