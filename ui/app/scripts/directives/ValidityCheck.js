/**
 * Created by alex on 10/11/15.
 */
app.directive('checker', function () {
  //http://stackoverflow.com/questions/16841587/angularjs-validity-not-reset-once-setvalidity-called
  //http://stackoverflow.com/questions/23011819/angularjs-setvalidity-not-working-while-checking-for-email-uniqueness
  return {
    restrict: 'A',
    scope: {
      checkValidity: '=checkValidity' // isolate directive's scope and inherit only checking function from parent's one
    },
    require: 'ngModel', // controller to be passed into directive linking function
    link: function (scope, elem, attr, ctrl) {
      //var email = elem.attr('name');

      // check validity on field blur
      elem.bind('blur keyup, input', function () {
        ctrl.$setValidity('trainee.exists', true);
        ctrl.$setValidity('invalid.data', true);
        /*
         scope.checkValidity(elem.val(), function (res) {
         if (res.valid) {
         ctrl.$setValidity(email, true);
         } else {
         ctrl.$setValidity(email, false);
         }
         });
         */
      });

      // set "valid" by default on typing
      /*elem.bind('keyup', function () {
       ctrl.$setValidity('trainee.exists', true);
       });*/
    }
  };
});
