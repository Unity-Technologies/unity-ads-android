function Books($resource) {
  'ngInject';
  return $resource('data/books.json');
}

export default {
  name: 'Books',
  fn: Books
};
