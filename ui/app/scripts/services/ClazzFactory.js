'use strict';

/*global app: false */

/**
 * The clazz factory.
 */
app.factory('ClazzFactory', function($http, $rootScope) {
  return {
    get: function() {
      return $http.get('/clazzes?p=0&s=1&f=');
    },
    getClazzCount: function() {
      return $http.get('/clazzesCount')
        .then(function(result) {
          $rootScope.totalClazzes = result.data
        });
  }
  };
});
