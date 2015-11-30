'use strict';

/*global app: false */

/**
 * The partner factory.
 */
app.factory('PartnerFactory', function($http) {
  return {
    get: function() {
      return $http.get('/partner');
    }
  };
});
