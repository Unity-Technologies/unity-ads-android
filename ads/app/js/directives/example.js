function ExampleDirective() {

  return {
    restrict: 'EA',
    templateUrl: 'directives/example.html',
    scope: {
      title: '@',
      message: '@clickMessage'
    },
    link: (scope, element) => {
      id.on('click', () => {
        window.alert('Element clicked: ' + scope.message);
      });
    }
  };
}

export default {
  name: 'exampleDirective',
  fn: ExampleDirective
};
