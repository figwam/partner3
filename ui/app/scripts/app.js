'use strict';

/**
 * The application.
 */
var app = angular.module('uiApp', [
  'ngResource',
  'ngMessages',
  'ngCookies',
  'ui.router',
  'ui.bootstrap',
  'satellizer',
  'validation.match',
  'angularUtils.directives.dirPagination',
  'ngAnimate',
  'ngLoadingSpinner',
  'ui.bootstrap.datetimepicker'
]);

/**
 * The run configuration.
 */
app.run(function($state,$rootScope) {

  /**
   * The partner data.
   *
   * @type {{}}
   */
  $rootScope.partner = {}
  $rootScope.formData = {}

  // for the state handling
  $rootScope.$state = $state


});

/**
 * The application routing.
 */
app.config(function ($urlRouterProvider, $stateProvider, $httpProvider, $authProvider, $compileProvider ) {


/*
   //http://stackoverflow.com/questions/22754393/in-a-chrome-app-using-angularjs-can-i-use-the-ngsrc-directive-directly-for-inte

    var currentImgSrcSanitizationWhitelist = $compileProvider.imgSrcSanitizationWhitelist();

    var newImgSrcSanitizationWhiteList = currentImgSrcSanitizationWhitelist.toString().slice(0,-1)+'|filesystem:chrome-extension:'+'|blob:chrome-extension%3A'+currentImgSrcSanitizationWhitelist.toString().slice(-1);

    console.log("Changing imgSrcSanitizationWhiteList from "+currentImgSrcSanitizationWhitelist+" to "+newImgSrcSanitizationWhiteList);
  //$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|chrome-extension):/);
  $compileProvider.imgSrcSanitizationWhitelist(newImgSrcSanitizationWhiteList);
*/

  $urlRouterProvider
    .otherwise('/');

  $stateProvider
    .state('home', {url:'/',
      views: {
        'header': {
          templateUrl: '/views/header.html',
          controller: 'HomeCtrl'
        },
        'content': {
          templateUrl: '/views/signIn.html',
          controller: 'SignInCtrl'
        },
        'footer': {
          templateUrl: '/views/footer.html'
        }
      }})
    .state('home.signIn', {url:'signIn',
      views: {
        'content@': {
          templateUrl: '/views/signIn.html',
          controller: 'SignInCtrl'
        }
      }})
    .state('home.signOut', { url:'signOut',
      views: {
        'content@': {
          templateUrl: '/views/home.html',
          controller: 'SignOutCtrl'
        }
      }})
    .state('me', { url: '/me',
      views: {
        'header': {
          templateUrl: '/views/me/header.html'
        },
        'sidebar': {
          templateUrl: '/views/me/sidebar.html'
        },
        'content': {
          templateUrl: '/views/me/dashboard.html'
        },
        'footer': {
          templateUrl: '/views/footer.html'
        }
      }})
    .state('me.clazzes', {
      views: {
        'content@': {
          templateUrl: '/views/me/clazzes.html',
          controller: 'ClazzDefCtrl'
        }
      }})
    .state('me.clazzes.edit', {
      views: {
        'content@': {
          templateUrl: '/views/me/clazzesEdit.html',
          controller: 'ClazzDefCtrl'
        }
      }})


  //http://www.webdeveasy.com/interceptors-in-angularjs-and-useful-examples/
  $httpProvider.interceptors.push(function($q, $injector) {
    return {
      request: function(request) {
        // Add auth token for Silhouette if trainee is authenticated
        var $auth = $injector.get('$auth');
        if ($auth.isAuthenticated()) {
          request.headers['X-Auth-Token'] = $auth.getToken();
        }

        // Add CSRF token for the Play CSRF filter
        var cookies = $injector.get('$cookies');
        var token = cookies.get('PLAY_CSRF_TOKEN');
        if (token) {
          // Play looks for a token with the name Csrf-Token
          // https://www.playframework.com/documentation/2.4.x/ScalaCsrf
          request.headers['Csrf-Token'] = token;
        }

        return request;
      }
      /*
      ,

      responseError: function(rejection) {
        if (rejection.status === 401) {
          $injector.get('$state').go('home');// usually signIn
        }
        return $q.reject(rejection);
      }
      */
    };
  });

  // Auth config
  //$authProvider.httpInterceptor = true; // Add Authorization header to HTTP request
  //$authProvider.loginOnSignup = true;
  //$authProvider.loginRedirect = '/me';
  //$authProvider.logoutRedirect = '/';
  //$authProvider.signupRedirect = '/me';
  //$authProvider.loginUrl = '/signIn';
  //$authProvider.signupUrl = '/signUp';
  //$authProvider.loginRoute = '/signIn';
  //$authProvider.signupRoute = '/signUp';
  //$authProvider.tokenName = 'token';
  //$authProvider.tokenPrefix = 'satellizer'; // Local Storage name prefix
  //$authProvider.authHeader = 'X-Auth-Token';
  //$authProvider.platform = 'browser';
  //$authProvider.storageType = 'localStorage';
  //$authProvider.storageType = 'localStorage';

  $authProvider.httpInterceptor = true; // Add Authorization header to HTTP request
  $authProvider.withCredentials = true;
  $authProvider.tokenRoot = null ;
  $authProvider.cordova = false ;
  $authProvider.baseUrl = '/' ;
  $authProvider.loginUrl = '/signIn' ;
  $authProvider.signupUrl = '/signUp' ;
  $authProvider.unlinkUrl = '/auth/unlink/' ;
  $authProvider.tokenName = 'token' ;
  $authProvider.tokenPrefix = 'satellizer' ; // Local Storage name prefix
  $authProvider.authHeader = 'X-Auth-Token' ;
  $authProvider.authToken = 'Bearer';
  $authProvider.storageType = 'localStorage' ;

  // Facebook
  $authProvider.facebook({
    clientId: '1503078423241610',
    url: '/authenticate/facebook',
    scope: 'email',
    scopeDelimiter: ',',
    requiredUrlParams: ['display', 'scope'],
    display: 'popup',
    type: '2.0',
    popupOptions: { width: 481, height: 269 }
  });

  // Google
  $authProvider.google({
    clientId: '526391676642-nbnoavs078shhti3ruk8jhl4nenv0g04.apps.googleusercontent.com',
    url: '/authenticate/google',
    scope: ['profile', 'email'],
    scopePrefix: 'openid',
    scopeDelimiter: ' ',
    requiredUrlParams: ['scope'],
    optionalUrlParams: ['display'],
    display: 'popup',
    type: '2.0',
    popupOptions: { width: 580, height: 400 }
  });

  // VK
  $authProvider.oauth2({
    clientId: '4782746',
    url: '/authenticate/vk',
    authorizationEndpoint: 'http://oauth.vk.com/authorize',
    name: 'vk',
    scope: 'email',
    scopeDelimiter: ' ',
    requiredUrlParams: ['display', 'scope'],
    display: 'popup',
    popupOptions: { width: 495, height: 400 }
  });

  // Twitter
  $authProvider.twitter({
    url: '/authenticate/twitter',
    type: '1.0',
    popupOptions: { width: 495, height: 645 }
  });

  // Xing
  $authProvider.oauth1({
    url: '/authenticate/xing',
    name: 'xing',
    popupOptions: { width: 495, height: 500 }
  });
});


