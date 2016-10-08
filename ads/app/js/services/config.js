function Config($resource) {
  'ngInject';
  return $resource('data/config/:lang.json');
}

export default {
  name: 'Config',
  fn: Config
};
