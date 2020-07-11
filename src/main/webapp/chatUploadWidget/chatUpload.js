(function() {

  var component = {
    id: '28c20c83-c75b-4ea2-bf1b-b1cfdb097703',
    name: 'chatUpload',
    icon: 'aoc-home',
    template: '<div class="widget widget--hello-world">\n\n  <h5 class="widget__header aoc-home">Hello World Widget</h5>\n\n  <div class="widget__content--indented" cc-scrollbar>\n    <p>{{ greeting }}</p>\n\n    <form class="say-hello">\n      Type message: <input type="text" name="message" ng-model="message">\n      <button ng-click="sayHello(message)">Send</button>\n    </form>\n\n    <span>Interaction Object</span>\n    <pre>{{ interaction | json }}</pre>\n\n    <span>Agent Object</span>\n    <pre>{{ agent | json }}</pre>\n\n    <span>Navigation Object</span>\n    <pre>{{ navigation | json }}</pre>\n\n    <span>Locale Object</span>\n    <pre>{{ locale | json }}</pre>\n\n    <span>Customer Object</span>\n    <pre>{{ customer | json }}</pre>\n\n    <span>Media Object</span>\n    <pre>{{ media | json }}</pre>\n\n    <span>Context Object</span>\n    <pre>{{ context | json }}</pre>\n\n  </div>\n\n</div>\n'
  };

  component.ui = angular.module('28c20c83-c75b-4ea2-bf1b-b1cfdb097703', ['core.services.WidgetAPI']);
  component.ui.directive('helloWorld', widgetComponent);

  function widgetComponent(WidgetAPI) {

    // main widget container
    function widgetContainer(scope, element, params) {
      var api = new WidgetAPI(params);

      scope.greeting = 'This is an example widget using the Open Widget Framework';

      scope.sayHello = function(message) {
        api.sendMessage(message);
        api.sendNotification('info', 'Widget: ' + message);
      };

      // interaction card data
      api.onDataEvent('onInteractionEvent', function(data) {
        scope.interaction = data;
      });

      // customer data from CRM (voice only)
      api.onDataEvent('onCRMDataEvent', function(data) {
        scope.customer = data;
      });

      // interaction context data
      api.onDataEvent('onContextDataEvent', function(data) {
        scope.context = data;
      });

      // media data from chat, sms, email, social
      api.onDataEvent('onMediaDataEvent', function(data) {
        scope.media = data;
      });

      // triggered on agent state changes
      api.onDataEvent('onAgentStateEvent', function(data) {
        scope.agent = data;
      });

      // triggered when the widget receives a message from another widget
      api.onDataEvent('onMessageEvent', function(data) {
        scope.message = data;
      });

      // triggered on navigation change
      api.onDataEvent('onNavigationEvent', function(data) {
        scope.navigation = data;
      });

      // triggered on language change
      api.onDataEvent('onLocaleUpdatedEvent', function(data) {
        scope.locale = data;
        api.sendNotification('info', 'Widget: Locale was changed');
      });

      // called when widget is destroyed
      element.on('$destroy', function() {
        api.unregister();
        scope.$destroy();
      });

    }

    return {
      scope: {},
      replace: true,
      template: component.template,
      link: widgetContainer
    };
  }

})();
