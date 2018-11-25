// Make sure to include the `ui.router` module as a dependency
angular.module('contactList', [
    'services.auth',
    'contactList.userProfiles',
    'contactList.contactgroups',
    'contactList.login',
    'contactList.forgottenPassword',
    'ui.router',
    'ngAnimate',
    'focusOn'
])

        .run(
                function ($rootScope, $state, $stateParams) {

                    // It's very handy to add references to $state and $stateParams to the $rootScope
                    // so that you can access them from any scope within your applications.For example,
                    // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
                    // to active whenever 'contacts.list' or one of its decendents is active.
                    $rootScope.$state = $state;
                    $rootScope.$stateParams = $stateParams;
                    console.info("app.run - $state, $stateParams added to rootscope")
                }

        )
        .run(function ($rootScope, $state) {
            $rootScope.$on("loggedIn", function (event, data) {
                console.info("app event handler: loggedIn: " + JSON.stringify(data));
                $rootScope.user = data;
                localStorage.user = JSON.stringify(data);
//                window.alert("Successfully logged in, dear " + data.givenName + "!");
                if ($rootScope.stateBeforeLogin !== null && $rootScope.stateBeforeLogin !== undefined) {
                    $state.go($rootScope.stateBeforeLogin,
                            $rootScope.stateParamsBeforeLogin);
                    $rootScope.stateBeforeLogin = null;
                    delete $rootScope.stateParamsBeforeLogin;
                } else {
                    $state.go("contactgroups");
                }
            });

            $rootScope.$on("login", function (event, data) {
                console.info("app event handler: login");
                delete $rootScope.user;
                delete localStorage.user;
                $state.go("login");
            });
            console.info("app.run - event handlers registered")

            $rootScope.alerts = [];
            $rootScope.closeAlert = function (index, alert) {
                $rootScope.alerts.splice(index, 1);
            };
            $rootScope.bindFunctionsToCurrentScope = function(functions, $scope) {
                angular.forEach(functions, function(func, name) {
                    $scope[name] = func.bind(null, $scope);
                    //console.log("bound function: $scope." + name);
                });
            }
            $rootScope.clearAlerts = function(force) {
                console.log("CLEAR ALERTS force: " + force + " $rootScope.keepAlerts: " + $rootScope.keepAlerts);
                console.log("$rootScope.alerts: " + JSON.stringify($rootScope.alerts));
                var forceDeletionMode = force === true || $rootScope.keepAlerts !== true;
                var newAlerts = [];
                angular.forEach($rootScope.alerts, function(alert, index) {
                    if (!forceDeletionMode && !alert.markedForDeletion) {
                        alert.markedForDeletion = true;
                        newAlerts.push(alert);
                    }
                });
                $rootScope.alerts = newAlerts;
                console.log("$rootScope.alerts after clearing: " + JSON.stringify($rootScope.alerts));
                delete $rootScope.keepAlerts;
            };
            $rootScope.replaceAlerts = function(alerts) {
                if (alerts !== undefined && alerts.length !== undefined && alerts.length > 0) {
                    $rootScope.alerts = angular.copy(alerts);
                } else {
                    $rootScope.alerts = [];
                }
            };
            $rootScope.addAlert = function(alert) {
                $rootScope.alerts.push(alert);
            };
            $rootScope.addSuccessMessage = function(message) {
                $rootScope.alerts.push({type: 'success', msg: message, dismissOnTimeout : 2000});
            };
            $rootScope.addErrorMessage = function(message) {
                $rootScope.alerts.push({type: 'danger', msg: message});
            };
            $rootScope.addWarningMessage = function(message) {
                $rootScope.alerts.push({type: 'warning', msg: message, dismissOnTimeout : 2000});
            };
        })
        .config(
                function ($stateProvider, $urlRouterProvider) {

                    /////////////////////////////
                    // Redirects and Otherwise //
                    /////////////////////////////

                    // Use $urlRouterProvider to configure any redirects (when) and invalid urls (otherwise).
                    $urlRouterProvider

                            // If the url is ever invalid, e.g. '/asdf', then redirect to '/' aka the home state
                            .otherwise('/');
                    //////////////////////////
                    // State Configurations //
                    //////////////////////////

                    // Use $stateProvider to configure your states.
                    $stateProvider

                            //////////
                            // Home //
                            //////////

                            .state("home", {
                                url: "/",
                                template: '<div class="margin"><p class="lead">Welcome to ContactList Demo</p>' +
                                        '<p>Use the menu above to navigate.</p></div>'

                            })

                            ///////////
                            // About //
                            ///////////

                            .state('about', {
                                url: '/about',
                                templateUrl: "about.html"
                            });
                    console.info("app.config - $urlRouterProvider configured");
                }
        );
