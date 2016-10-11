'use strict';

import config from '../config';
import gulp   from 'gulp';
import archiver from 'gulp-archiver';

gulp.task('zip', function() {

  return gulp.src(config.zip.src)
    .pipe(archiver(config.zip.name, config.zip.options))
    .pipe(gulp.dest(config.zip.dest));
});
