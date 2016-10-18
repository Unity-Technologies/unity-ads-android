'use strict';

import config from '../config';
import changed from 'gulp-changed';
import gulp from 'gulp';
import gulpif from 'gulp-if';
import gulpfile from 'gulp-file';
import tap from 'gulp-tap';
import jsonlint from 'gulp-jsonlint';
import cryptojs from 'crypto-js';
import AES from 'crypto-js/aes';
import browserSync from 'browser-sync';
import _ from 'lodash';

const key = 'Bread is Good!';
var keyJson;

gulp.task('encrypt', function() {

    var encrypt = function(file, t) {
        var encrypted = AES.encrypt(file.contents.toString('base64'), key);

        var output = {
            ivHex: encrypted.iv.toString(),
            keyHex: encrypted.key.toString()
        };

        keyJson = JSON.stringify(output);
        console.log(keyJson);

        file.path += '.enc';
        file.contents = Buffer.from(encrypted.toString(), 'base64');

        gulpfile('secure.json', keyJson)
          .pipe(gulp.dest(config.data.dest));
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
        .pipe(gulp.dest(config.data.dest))
        .pipe(browserSync.stream());
});

gulp.task('data', ['encrypt']);
