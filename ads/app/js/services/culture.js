function Culture($resource) {
  'ngInject';
  return $resource('data/culture.json');
}

export default {
  name: 'Culture',
  fn: Culture
};
