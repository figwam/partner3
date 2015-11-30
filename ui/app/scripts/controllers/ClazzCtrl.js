'use strict';

/*global app: false */

/**
 * The clazz controller.
 *
 */
app.controller('ClazzCtrl', ['$rootScope', '$scope', '$http', '$location', '$templateCache','ClazzFactory', function($rootScope, $scope, $http, $templateCache, $location, ClazzFactory) {


  $scope.totalClazzes = 0;
  $scope.clazzesPerPage = 10;
  getResultsPage(1)
  $scope.pagination = {
    current: 1
  };

  $scope.pageChanged = function(newPage) {
    getResultsPage(newPage);
  };

  function getResultsPage(pageNumber) {
    //play start paging from 0 --> (pageNumber-1)
    $http.get('/clazzes?p='+(pageNumber-1)+'&s=1&f='+($rootScope.clazzesSearchString == null ? '':$rootScope.clazzesSearchString))
      .then(function(result) {
        $rootScope.clazzes = result.data
        $scope.totalClazzes = result.data.total
      });
  }

  // calling our submit function.
  $scope.submitSearch = function () {
    $rootScope.clazzesSearchString = $scope.searchString
    getResultsPage(1);
  };


}]);



app.filter('searchFor', function(){
  return function(arr, searchString){
    if(!searchString){
      return arr;
    }
    var result = [];
    searchString = searchString.toLowerCase();
    angular.forEach(arr, function(item){
      if(item.searchMeta.toLowerCase().indexOf(searchString) !== -1){
        result.push(item);
      }
    });
    return result;
  };
});
