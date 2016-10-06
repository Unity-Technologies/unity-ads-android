function HomeCtrl($scope, DataService) {
    // ViewModel
    const vm = this;
    var element = 'Genesis-1';
    vm.data = DataService.get({
        lang: 'cnvs',
        element: element,
    });

    vm.data.image = element;
    console.log(vm.data);
}

export default {
    name: 'HomeCtrl',
    fn: HomeCtrl
};

HomeCtrl.$inject = ['$scope', 'DataService'];
