'use strict';

/*global app: false */


app.factory('RegistrationFactory', function($http, $rootScope) {
  return {
    getRegistrationsCount: function() {
      return $http.get('/trainees/me/registrationsCount')
        .then(function(result) {
          $rootScope.totalRegistrations = result.data
        });
    }
  };
});
