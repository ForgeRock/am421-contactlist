angular
        .module('services.auth.openam-min', [])
        .constant('openamConstants', {
            //TODO Ch4L1Ex3: Define serverURI as 'http://login.example.com:18080/am'
            serverURI: null
        })
        .factory('loginService', function ($http, $rootScope, openamConstants, $q) {

            var loginService = {
                login: function (username, password) {
                    console.info("openam-min.login(" + username + ", ****** )");

                    //TODO Ch4L1Ex3: Send a POST request to serverURI/json/authenticate.
                    //TODO Ch4L1Ex3: Send the credentials as headers: "X-OpenAM-Username" and "X-OpenAM-Password"
                    var req = {
                        method: 'POST',
                        url: 'rest/auth/login',
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'application/json',
                            'Accept-API-Version': 'resource=1.1, protocol=1.0'
                        },
                        data: {
                        }
                    };
                   return $http(req); // returning with the promise

                },
                validateToken: function (tokenId) {
                    console.info("openam-min.validateToken(" + tokenId + ")");
                    //mock implementation that always returns with: no valid token
                    var result = {
                        data : {
                           valid: false
                        }
                    };

                    var deferred = $q.defer();
                    deferred.resolve(result);
                    return deferred.promise;
                },
                logout: function (tokenId) {
                    console.info("openam-min.logout(" + tokenId + ")");
                    //mock implementation that always returns with success

                    var result = { data : {success: true}};

                    var deferred = $q.defer();
                    deferred.resolve(result);
                    return deferred.promise;

                },
                getUserAttributes: function (tokenId, username) {
                    console.info("openam-min.getUserAttributes(" + tokenId + ")");
                    //mock implementation that returns with username as the full name

                    var user = {
                           uid: username,
                           tokenId: tokenId,
                           name: username,
                           privileges: {}
                    };

                    var deferred = $q.defer();
                    deferred.resolve(user);
                    return deferred.promise;
                },
                customizeHTTP: function (http) {
                http.defaults.headers.common.Authorization
                        = "Bearer " + encodeURIComponent($rootScope.user.tokenId);
                }
            };
            return loginService;
        });

