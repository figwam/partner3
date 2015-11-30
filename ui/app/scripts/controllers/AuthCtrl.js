'use strict';

/*global app: false */

/**
 * The navigation controller.
 */
app.controller('AuthCtrl', ['$rootScope', '$scope', '$auth', '$state', '$anchorScroll', 'PartnerFactory', function ($rootScope, $scope, $auth, $state, $anchorScroll, PartnerFactory) {

  /**
   * Initializes the controller.
   */
  $scope.init = function () {
    $scope.code = null;
    $scope.response = null;


    /*
    PartnerFactory.get()
      .success(function (data) {
        $rootScope.partner = data;
        console.log($rootScope.partner.extId)
        $location.path("/me");
      })
      .error(function (error) {
        //console.log("1...."+JSON.stringify(error))
        if (!error.success)
        $location.path("/");
      });
  */

    PartnerFactory.get().then(
      function(response) {
        $rootScope.partner = response.data;
        $state.go("me")
      },
      function(response) {
        if(response.status == 401) {
          // do locally logout, cause some login inconsistency (Can happen if server is restarted!)
          // do logout on client side and redirect to root
          $auth.logout().then(function() {$state.go("home")});
        } else {
          // Also in other error cases do client side logout
          // TODO: improve! Distinguish between error cases
          $auth.logout().then(function() {$state.go("home")});
        }
      });


  };

  /**
   * Indicates if the partner is authenticated or not.
   *
   * @returns {boolean} True if the partner is authenticated, false otherwise.
   */
  $scope.isAuthenticated = function () {
    return $auth.isAuthenticated();
  };

}]);
