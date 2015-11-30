'use strict';

/*global app: false */


app.factory('AlertFactory', function() {
  return {
    alerts: [],
    addAlert: function (message, type) {
      this.alerts.push({
        type: type,
        msg: message
      });
    },
    closeAlert: function (index) {
      this.alerts.splice(index, 1);
    }
  }
});
