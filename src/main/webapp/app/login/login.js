angular.module('contactList.login', [
    'ui.router', 'services.auth'
]).config(
        function ($stateProvider, $urlRouterProvider) {

            $stateProvider
                    .state('login', {
                        url: "/login?tokenId",
                        templateUrl: "app/login/login.html",
                        noAuth: true,
                        //DONE Ch5L1Ex3: Inject the oauth2Service into the controller: add a new parameter to this function named oauth2Service
                        controller: function ($rootScope, $scope, $stateParams, loginService, oauth2Service) {
                            console.info("LoginWindowCtrl start");

                            $scope.clearMessages = function () {
                                $scope.message = $scope.error = null;
                            };

                            $scope.clear = function () {
                                $scope.username = $scope.password = null;
                                $scope.clearMessages();
                            };

                            $scope.clear();

                            //DONE Ch4L2Ex2: Modify the handleAuthSuccessResponse function to handle the response if the authentication phase is in SelectRole module. 
                            //DONE Ch4L2Ex2: If response.data.stage === 'SelectRole2' then convert the response data into a parsed format with loginService.getChoiceCallback(response.data) and store it in $scope.choiceCallback.
                            //DONE Ch4L2Ex2: If response.data.stage === 'SelectRole2' then also save the complete response in $scope.data: we will basicly send this back after the user chooses the role. See $scope.loginAs function.
                            //DONE Ch4L2Ex2: The original code which extracts the tokenId and fetches the user profile can go into the else branch.
                            $scope.handleAuthSuccessResponse = function (response) {
                                console.info("loginService.login returned with: " + JSON.stringify(response));
                                $scope.clearMessages();
                                if (typeof response.data.callbacks !== "undefined") {
                                    if (response.data.callbacks[0].type ==='ChoiceCallback') {
                                        $scope.data = response.data;
                                        $scope.choiceCallback=loginService.getChoiceCallback(response.data);
                                    }
                                } else {
                                    var tokenId = response.data.tokenId;
                                    $rootScope.fetchUserProfile(tokenId, $scope.username);
                                }
                            };

                            $scope.login = function () {
                                $scope.message = '';
                                if (!$scope.username || !$scope.password) {
                                    $scope.error = 'invalid username or password';
                                    return;
                                }
                                var promise = loginService.login($scope.username, $scope.password)
                                        .then($scope.handleAuthSuccessResponse, function (status) {
                                            $scope.choiceCallback = null;
                                            $scope.error = 'invalid username or password';
                                        });
                                return promise;
                            };

                            //DONE Ch5L1Ex3: Expose oauth2Service.startAuthorizationFlow function as loginWithOAuth2 in $scope
                            $scope.loginWithOAuth2 = oauth2Service.startAuthorizationFlow;

                            //DONE Ch4L2Ex2: Create loginAS function which is called when the user selects one of the offered roles.
                            //DONE Ch4L2Ex2: In the loginAS function call loginService.submitChoice with $scope.data and role.value which will send back the received callback structure stored in $scope.data with the selected role.
                            //DONE Ch4L2Ex2: In the loginAS function handle the promise returned by the submitChoice function with the $scope.handleAuthSuccessResponse function. In the error handling function clear the $scope.choiceCallback and provide an error message in $scope.error.
                            $scope.loginAs = function (role) {
                                loginService.submitChoice($scope.data, role.value)
                                        .then($scope.handleAuthSuccessResponse,
                                                function (response) {
                                                    $scope.choiceCallback = null;
                                                    $scope.error = 'Error during sending selected role';
                                                });
                            };

                            //If tokenId is sent in the location hash, like #/login?tokenId=abc123
                            //checking its validity
                            var tokenId = $stateParams.tokenId;
                            //DONE Ch5L1Ex3: If the access token is passed in the url like #/login?tokenId=abc123, jump to the token validation state immediately.
                            //DONE Ch5L1Ex3: if tokenId presents call $rootScope.validateToken(tokenId);
                            if (tokenId) {
                                $rootScope.validateToken(tokenId);
                            }
                        },
                        resolve: {
                            //DONE Ch5L1Ex3: Add a new depencency here to initialize oauth2Service before the login controller starts
                            //DONE Ch5L1Ex3: Call oauth2Service's init() method which will return with a promise. The angular router will wait until this promise is resolved.
                            //DONE Ch5L1Ex3: Hint: uncomment the following lines
                            oauth2Init: function (oauth2Service) {
                                return oauth2Service.init();
                            }
                        }
                    });
            console.info("login: stateprovider init");

        })

        .run(function ($rootScope, $state, loginService) {
            $rootScope.validateToken = function (tokenId) {
                loginService.validateToken(tokenId)
                        .then(function (tokenInfo) {
                            console.info("validateToken response:" + JSON.stringify(tokenInfo));
                            if (tokenInfo.valid === true) {
                                $rootScope.fetchUserProfile(tokenInfo.tokenId, tokenInfo.uid);
                            } else {
                                $rootScope.$broadcast('login');
                            }
                        }, function (status) {
                            console.info("validateToken failed:" + JSON.stringify(status));
                            $rootScope.$broadcast('login');
                        });
            };
            $rootScope.fetchUserProfile = function (tokenId, username) {
                console.info("Fetching user profile for user " + username);
                loginService.getUserAttributes(tokenId, username)
                        .then(function (user) {
                            console.info("loginService.getUserAttributes returned with: " + JSON.stringify(user));
                            user.privilegeNames = Object.keys(user.privileges).sort();
                            user.tokenId = tokenId;
                            $rootScope.$broadcast('loggedIn', user);
                        });
            };
            $rootScope.$on('$stateChangeStart',
                    function (event, toState, toParams, fromState, fromParams) {
                        console.info("STATE CHANGE START from: " + fromState.name + ", to: " + toState.name)
                        if (toState.noAuth === undefined && $rootScope.user === undefined) {
                            $rootScope.stateBeforeLogin = toState;
                            $rootScope.stateParamsBeforeLogin = toParams;
                            event.preventDefault();
                            $state.transitionTo("login");
                        }
                    });
            $rootScope.logout = function () {
                loginService.logout($rootScope.user.tokenId)
                        .then(function () {
                            console.info("successfully logged out")
                            $rootScope.user = null;
                            delete localStorage.user;
                            $rootScope.$broadcast('logout');
                        }, function () {
                            console.info("could not log out")
                        });
                $state.transitionTo("login");
            }

            console.info("login.init: localStorage.user=" + localStorage.user);
            if (localStorage.user) {
                try {
                    var user = JSON.parse(localStorage.user);
                    if (user.tokenId) {
                        console.info("login.init: user.tokenId" + user.tokenId);
                        $rootScope.validateToken(user.tokenId);
                    } else {
                        $rootScope.$broadcast('login');
                    }
                } catch (e) {
                    console.error("login.init exception: " + e)
                    delete localStorage.user;
                    $rootScope.$broadcast('login');
                }
            } else {
                $rootScope.$broadcast('login');
            }
        })
        ;
