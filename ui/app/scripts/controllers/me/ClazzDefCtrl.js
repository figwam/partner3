'use strict';

/*global app: false */

/**
 * The clazz controller.
 *
 */
app.controller('ClazzDefCtrl', ['$rootScope', '$state', '$scope', '$http', '$templateCache', 'AlertFactory', '$filter', 'GeneralFactory', function($rootScope, $state, $scope, $http, $templateCache, AlertFactory, $filter, GeneralFactory) {


  $scope.totalClazzes = 0
  $scope.clazzesPerPage = 10

  $scope.clazzdef = {
    "duration":5,
    "contingent":1
  }
  $scope.ui = {}

  getResultsPage(1)
  GeneralFactory.getEnums()

  $scope.pagination = {
    current: 1
  };

  $scope.pageChanged = function(newPage) {
    getResultsPage(newPage);
  };

  function getResultsPage(pageNumber) {
    //play start paging from 0 --> (pageNumber-1)
    $http.get('/clazzes/partners/me?p='+(pageNumber-1)+'&s=1&f='+($rootScope.clazzesSearchString == null ? '':$rootScope.clazzesSearchString))
      .then(function(result) {
        $rootScope.clazzes = result.data
        $scope.totalClazzes = result.data.total
      });
  }

  $scope.create = function() {
    var start = new Date($scope.clazzdef.startFrom)
    $scope.clazzdef.endAt = new Date(start.getTime()+($scope.clazzdef.duration*60*1000))
    $scope.clazzdef.activeFrom = new Date
    $scope.clazzdef.activeTill = new Date(2222, 0, 31) //31.01.2222 year in future. In 2222 this app will have a bug, but then I don't care
    $scope.clazzdef.tags = $scope.ui.tags.id
    $scope.clazzdef.recurrence = $scope.ui.recurrence.id
    $http({
      method: "POST",
      url: "/partners/me/studio/clazzdefs",
      data: $scope.clazzdef,
      headers: { 'Content-Type': 'application/json; charset=UTF-8'},
      cache: $templateCache}).
    then(function(response) {
      $scope.status = response.status
      $scope.data = response.data
      AlertFactory.addAlert(response.data.message, "success")
      getResultsPage(1)
      $scope.go("me.clazzes")
    }, function(response) {
      $scope.data = response.data
      $scope.status = response.status
      AlertFactory.addAlert(response.data.message, "danger")
    });
  };

  $scope.onTimeStartSet = function (newDate, oldDate) {
    $scope.clazzdef.startFrom = newDate
    $scope.ui.startFrom = $filter('date')(newDate, "EEE, dd.MM.yyyy hh:mm");
  }

  $scope.beforeRenderStart = function ($view, $dates, $leftDate, $upDate, $rightDate) {
    var currentDate = new Date();

    var yearViewDate = new Date(currentDate.getFullYear(), 0);
    var yearViewDateValue = yearViewDate.getTime();

    var monthViewDate = new Date(currentDate.getFullYear(), currentDate.getMonth());
    var monthViewDateValue = monthViewDate.getTime();

    var dayViewDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
    var dayViewDateValue = dayViewDate.getTime();

    var hourViewDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate(), currentDate.getHours());
    var hourViewDateValue = hourViewDate.getTime();

    var minuteViewDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate(), currentDate.getHours(), currentDate.getMinutes());
    var minuteViewDateValue = minuteViewDate.getTime();

    for (var index = 0; index < $dates.length; index++) {

      var date = $dates[index];

      // Disable if it's in the past
      var dateValue = date.localDateValue();
      switch ($view) {

        case 'year':
          if (dateValue < yearViewDateValue) {
            date.selectable = false;
          }
          break;

        case 'month':
          if (dateValue < monthViewDateValue) {
            date.selectable = false;
          }
          break;

        case 'day':
          if (dateValue < dayViewDateValue) {
            date.selectable = false;
          }
          break;

        case 'hour':
          if (dateValue < hourViewDateValue) {
            date.selectable = false;
          }
          break;

        case 'minute':
          if (dateValue < minuteViewDateValue) {
            date.selectable = false;
          }
          break;
      }
    }
  }

}]);

