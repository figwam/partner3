'use strict';

/*global app: false */

/**
 * The home controller.
 */
app.controller('HomeCtrl', ['$rootScope', '$scope', '$state', '$location', '$anchorScroll', function($rootScope, $scope, $state, $location, $anchorScroll) {




  // calling our submit function.
  $scope.submitSearchRedirect = function () {
    $rootScope.clazzesSearchString = $scope.searchString
    $state.go('home.clazzes')
  };

  // calling our submit function.
  $scope.submitOfferRedirect = function (selectedOfferName, selectedOfferId, skipAbo) {
    $scope.formData.aboId = selectedOfferId
    $scope.formData.aboName = selectedOfferName
    skipAbo ? $state.go('home.signUp.profile') : $state.go('home.signUp.abo')
    $location.hash('home');
    $anchorScroll();
  };

  $scope.scrollTo = function (id) {
    $state.go('home')
    $location.hash(id);
    $anchorScroll();
  }

}]);
