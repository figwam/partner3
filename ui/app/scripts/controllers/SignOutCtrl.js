'use strict';

/*global app: false */

/**
 * The sign out controller.
 */
app.controller('SignOutCtrl', ['$auth', '$location', function($auth, $location) {
  if (!$auth.isAuthenticated()) {
    return;
  }
  $auth.logout()
    .then(function() {
      $location.path("/")
      /*
       An alert could be placed here with some message like "Some message to user"
       see: https://angular-ui.github.io/bootstrap/#/alert
       */
    });
}]);
