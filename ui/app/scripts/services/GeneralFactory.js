'use strict';

/*global app: false */

/**
 * The factory.
 */
app.factory('GeneralFactory', function($http, $rootScope) {
  return {
    getEnums: function() {
      return $http.get('/enums')
        .then(function(result) {
          $rootScope.enums = result.data.enums
      });
    }
  };
});
