function Word($resource) {
  'ngInject';
  return $resource('/data/word/:element/:version.json');
}

export default {
  name: 'Word',
  fn: Word
};
