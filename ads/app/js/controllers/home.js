function HomeCtrl($scope, DataService) {
    // ViewModel
    const vm = this;
    var element = 'Mat-14-22';
    vm.data = DataService.get({
        lang: 'esv',
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
