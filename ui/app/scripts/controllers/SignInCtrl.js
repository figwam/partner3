'use strict';

/*global app: false */

/**
 * The sign in controller.
 */
app.controller('SignInCtrl', ['$scope', '$auth', '$state', function($scope, $auth, $state) {

  /**
   * Submits the login form.
   */

  $scope.submit = function() {
    $auth.setStorageType($scope.rememberMe ? 'localStorage' : 'sessionStorage');
    $auth.login({ email: $scope.email, password: $scope.password, rememberMe: $scope.rememberMe })
      .then(function() {
        $state.go("me")
        /*
        An alert could be placed here with some message like "Some message to user"
        see: https://angular-ui.github.io/bootstrap/#/alert
        */
      })
      .catch(function(response) {
        $scope.errorMessage = {};
        angular.forEach(response.data.message, function(message, field) {
          console.log(response.data.message);
          // response.data.message -> Message ist invalid.credentials
          $scope.form.email.$setValidity('invalid.credentials', false);
          $scope.form.password.$setValidity('invalid.credentials', false);
        });
      });
  };

  /**
   * Authenticate with a social provider.
   *
   * @param provider The name of the provider to authenticate.
   */
  $scope.authenticate = function(provider) {
    $auth.authenticate(provider)
      .then(function() {
        /*
         An alert could be placed here with some message like "Some message to user"
         see: https://angular-ui.github.io/bootstrap/#/alert
         */
      })
      .catch(function(response) {
        /*
         An alert could be placed here with some message like "Some message to user"
         see: https://angular-ui.github.io/bootstrap/#/alert
         */
      });
  };
}]);
