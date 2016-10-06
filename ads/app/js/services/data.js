function DataService($resource) {
  'ngInject';
  var service = $resource('/data/:element/:lang.json');
  return service;
}

export default {
  name: 'DataService',
  fn: DataService
};
