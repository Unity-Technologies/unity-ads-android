function Data($resource) {
  'ngInject';
  return $resource('/data/:element/:version.json');
}

export default {
  name: 'Data',
  fn: Data
};
