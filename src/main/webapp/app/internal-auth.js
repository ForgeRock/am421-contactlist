angular
        .module('services.auth.internal', [])
        .constant('openamConstants', {
            serverURI: 'http://login.example.com:18080/am'
        })
        .factory('loginService', function ($http, $rootScope, openamConstants) {

            var loginService = {
                login: function (username, password) {

                    var req = {
                        method: 'POST',
                        url: 'rest/auth/login',
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        },
                        data: {
                            'user': username,
                            'password': password
                        }
                    };
                    return $http(req); // returning with the promise

                },
                validateToken: function (tokenId) {
                    console.info("internal.validateToken(" + tokenId + ")");
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId)
                    };
                    return $http(req)
                            .then(function(response) {
                                return {
                                    tokenId : tokenId,
                                    valid : true,
                                    uid : response.data.user.uid
                                };
                    }); 
                },
                getUserAttributes: function (tokenId, username) {
                    console.info("internal.getUserAttributes(" + tokenId + ")");
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId)
                    };
                    return $http(req).then(function(response) {
                            var user = response.data.user;
                            var privilegeArray = response.data.privileges;
                            var privilegeSet = {};
                            angular.forEach(privilegeArray, function(privilege) {
                                console.log("Found privilege: " + privilege);
                                privilegeSet[privilege] = true;
                            });
                            user.privileges = privilegeSet;
                            console.log("User attributes are: " + JSON.stringify(user));
                            return user;
                    }); 
                },
                logout: function (tokenId) {
                    console.info("internal.logout called with tokenId=" + tokenId);
                    var req = {
                        'method': 'DELETE',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId)
                    };
                    var promise = $http(req);
                    return promise;
                },
                customizeHTTP: function (http) {
                    http.defaults.headers.common.Authorization
                            = "Bearer " + encodeURIComponent($rootScope.user.tokenId);
                }

            };
            return loginService;
        });

