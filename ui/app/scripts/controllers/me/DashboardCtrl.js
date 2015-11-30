'use strict';

/*global app: false */

/**
 * The clazz controller.
 *
 */
app.controller('DashboardCtrl', ['$rootScope', '$scope', '$http', 'ClazzFactory', 'RegistrationFactory', 'GeneralFactory', function($rootScope, $scope, $http, ClazzFactory, RegistrationFactory, GeneralFactory) {

  RegistrationFactory.getRegistrationsCount();
  ClazzFactory.getClazzCount();

}]);
