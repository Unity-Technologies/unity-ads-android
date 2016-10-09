'use strict';

import config from '../config';
import changed from 'gulp-changed';
import gulp from 'gulp';
import gulpif from 'gulp-if';
import tap from 'gulp-tap';
import jsonlint from 'gulp-jsonlint';
import cryptojs from 'crypto-js';
import AES from 'crypto-js/aes';
import browserSync from 'browser-sync';
import archiver from 'gulp-archiver';

const key = 'Bread is Good!';

gulp.task('encrypt', function() {
    var encrypt = function(file) {
      var encrypted = AES.encrypt(file.contents.toString('base64'), key);
      console.log(encrypted);
      file.path += '.enc';
      file.contents = Buffer.from(encrypted.toString(), 'base64');
    };
    var decrypt = function(file) {
      var decrypted = AES.decrypt(file.contents.toString('base64'), key);
      file.path = file.path.replace('.enc', '');
      file.contents = Buffer.from(decrypted.toString(cryptojs.enc.Utf8), 'base64');
    };

    return gulp.src(config.data.src)
        .pipe(changed(config.data.dest))
        .pipe(jsonlint())
        .pipe(jsonlint.reporter())
        .pipe(gulpif(global.isProd, archiver('data.zip', {})))
        .pipe(gulpif(global.isProd, tap(encrypt)))
        // .pipe(gulp.dest(config.data.dest))
        // .pipe(gulpif(global.isProd, tap(decrypt)))
        .pipe(gulp.dest(config.data.dest))
        .pipe(browserSync.stream());
});

gulp.task('data', ['encrypt']);
