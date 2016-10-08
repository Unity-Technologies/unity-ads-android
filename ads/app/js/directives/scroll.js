function Scroll() {
  return {
    restrict: 'EA',
    link: (scope, element, attr) => {
      var bottom = (e) => e.prop('scrollHeight') -
        e.prop('scrollTop') - 1 === e.prop('clientHeight');

      element.on('scroll', () => {
        if(!bottom(element)) {
          return;
        }
        scope.$emit('read');
      });
    }
  };
}

export default {
  name: 'scroll',
  fn: Scroll
};
