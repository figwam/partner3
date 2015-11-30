/**
 * Alerts Controller
 */

app.controller('AlertsCtrl', ['$scope','AlertFactory', function ($scope, AlertFactory) {

  $scope.alerts = AlertFactory.alerts;

  /* Example of adding alerts to scope */
/*
   $scope.alerts = [{
   type: 'success',
   msg: 'Something good happened and I want to tell it to user'
   }, {
   type: 'danger',
   msg: 'Something really bad happened and I want to tell it to user'
   }];

*/
  $scope.addAlert = function (message, type) {
    AlertFactory.addAlert(message, type)
  };

  $scope.closeAlert = function (index) {
    AlertFactory.closeAlert(index)
  };


}]);
