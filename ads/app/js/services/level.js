function Level($resource) {
  'ngInject';
  return $resource('/data/level.json');
}

export default {
  name: 'Level',
  fn: Level
};
