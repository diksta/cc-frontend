module.exports = function (grunt) {
    // Displays the elapsed execution time of grunt tasks
    require('time-grunt')(grunt);

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        // Builds Sass
        sass: {
          dev: {
            options: {
                style: 'expanded'
            },
            files: [
            {
              expand: true,
              cwd: '../sass/',
              src: ['**/*.scss'],
              dest: '<%= dirs.public %>/stylesheets/',
              ext: '.css'
            }]
          },
          prod: {
            options: {
                style: 'compressed'
            },
            files: [{
              expand: true,
              cwd: '../sass/',
              src: ['**/*.scss'],
              dest: '<%= dirs.public %>/stylesheets/',
              ext: '.min.css'
            }]
          }
        },
        cssmin: {
          public: {
            files: [{
              expand: true,
              cwd: '<%= dirs.public %>/stylesheets/',
              src: ['!*.css', '*.min.css'],
              dest: '<%= dirs.public %>/stylesheets/',
              ext: '.min.css'
            }]
          }
        },
        dirs: {
            public: "../../../public",
            js: "../js/",
            images: "../img/"
        },
        watch: {
            compileCSS: {
                files: ['../sass/**/*.scss'],
                tasks: ['sass']
            }
        },
        copy: {
            images: {
                files: [{
                    expand: true, // true ensures it goes to sub-directories
                    src: '**/*',
                    cwd: '<%= dirs.images %>',
                    dest: '<%= dirs.public %>/images/'
                }]
             },
             js: {
                files: [{
                    expand: true, // true ensures it goes to sub-directories
                    src: ['**/*', '!Gruntfile.js', '!**/node_modules/**', '!**/*.json'],
                    cwd: '<%= dirs.js %>',
                    dest: '<%= dirs.public %>/javascript/'
                }]
             }
        },
        concurrent: {
            dev: ['copy:images', 'copy:js', 'sass'],
            prod:['copy:images', 'copy:js', 'sass:prod']
        },
        clean : {
            options: {
                force: true
            },
            public: ["<%= dirs.public %>"]
        },
        browserSync: {
            dev: {
                bsFiles: {
                    src : [
                    '<%= dirs.public %>/stylesheets/*.css',
                    '../../views/*.scala.html'
                    ]
                },
                options: {
                    watchTask: true, // < VERY important
                    proxy: "http://localhost:9366/childcare-calculator/",
                    port: 9001
                }
            }
        }
    });

    // will read the dependencies/devDependencies/peerDependencies in your package.json
    // and load grunt tasks that match the provided patterns.
    // Loading the different plugins
    require('load-grunt-tasks')(grunt);

    // Default task(s).
    grunt.registerTask('default', ['dev'])
    grunt.registerTask('dev', ['clean:public', 'concurrent:dev', 'cssmin:public', 'browserSync', 'watch']);
    grunt.registerTask('prod', ['clean:public', 'concurrent:prod', 'cssmin:public']);

    // Run bower install
    grunt.registerTask('bower-install', function() {
        var done = this.async();
        var bower = require('bower').commands;
        bower.install().on('end', function(data) {
            done();
        }).on('data', function(data) {
            console.log(data);
        }).on('error', function(err) {
            console.error(err);
            done();
        });
    });
};