'use strict';

describe('Controller: SignInCtrl', function () {

  // load the controller's module
  beforeEach(module('uiApp'));

  var SignInCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    SignInCtrl = $controller('SignInCtrl', {
      $scope: scope
    });
  }));

  it('should have no signed in user to start', function () {
    expect(scope.email).toBe(null);
  });

  it('should add items to the list', function () {
    scope.email = 'alex@alex.com';
    scope.password= 'secret';
    scope.rememberMe = false;
    scope.submit();
    expect(scope.email).toBe('alex@alex.com');
  });
});
