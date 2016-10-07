'use strict';

import config from '../config';
import changed from 'gulp-changed';
import gulp from 'gulp';
import gulpif from 'gulp-if';
import tap from 'gulp-tap';
import jsonlint from 'gulp-jsonlint';
// import cryptojs from 'crypto-js';
import AES from 'crypto-js/aes';
import browserSync from 'browser-sync';

gulp.task('data', function() {

    var key = 'Bread is Good!';
    console.log(AES);

    var encrypt = function(file) {
        var encrypted = AES.encrypt(file.contents.toString(), key);
        console.log(encrypted.toString());
        // var decrypted = AES.decrypt(encrypted.toString(), key);
        // console.log(decrypted.toString(cryptojs.enc.Utf8));
        file.contents = new Buffer(encrypted.toString());
    };

    return gulp.src(config.data.src)
//        .pipe(changed(config.data.dest)) // Ignore unchanged files
        .pipe(jsonlint())
        .pipe(jsonlint.reporter())
        .pipe(gulpif(global.isProd, tap(encrypt)))
        .pipe(gulp.dest(config.data.dest))
        .pipe(browserSync.stream());
});
