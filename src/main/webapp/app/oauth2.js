angular
        .module('services.auth.oauth2', [])
        .constant('openamConstants', {
            serverURI: 'http://login.example.com:18080/am'
        })
        .factory('oauth2Util', function () {
            var oauth2Util = {
                generateRandomString: function (length) {
                    var text = "";
                    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                    for (var i = 0; i < length; i++) {
                        text += possible.charAt(Math.floor(Math.random() * possible.length));
                    }
                    return text;
                }
            };
            return oauth2Util;
        })
        .factory('oauth2Service', function ($http, $rootScope, oauth2Util) {
            var metaData = null;
            var metaDataPromise = null;
            var oauth2 = {
                init: function () {
                    console.info("oauth2.loading metadata...");
                    if (metaDataPromise) {
                        return metaDataPromise;
                    }

                    var req = {
                        method: 'GET',
                        url: 'http://login.example.com:18080/am/oauth2/.well-known/openid-configuration',
                        headers: {
                            'Accept': 'application/json'
                        }
                    };
                    metaDataPromise = $http(req).then(function (response) {
                        metaData = response.data;
                        console.log("OpenID Connect metadata loaded: " + JSON.stringify(metaData));
                    });
                    return metaDataPromise;
                },
                startAuthorizationFlow: function () {
                    var requestParameters = {
                        "response_type": "token id_token",
                        "client_id": "contactList",
                        "realm": "/",
                        //DONE Ch5L1Ex3: Request the following scopes: openid,profile,email and contactlist-privileges. Separate them with spaces.
                        //TODO Ch6L1Ex2: Add new scope: uma_protection. Separate them with spaces.
                        "scope": "openid profile email contactlist-privileges uma_protection",
                        //DONE Ch5L1Ex3: Set redirect URI: http://app.test:8080/contactlist/oauth2ResponseConsumer.html
                        "redirect_uri": "http://app.test:8080/contactlist/oauth2ResponseConsumer.html",
                        "state": oauth2Util.generateRandomString(16), //random state
                        //DONE Ch5L1Ex3: Pass "selectRole" as the "acr_values" -> this will be mapped to the testSelectRole authentication chain at the OAuth2Provider.
                        "acr_values": "selectRole",
                        "nonce": oauth2Util.generateRandomString(16) //random nonce
                    };

                    localStorage.oauth2RequestParameters = JSON.stringify(requestParameters); // Saving oauth2 request paramters in localStorage

                    //DONE Ch5L1Ex3: Redirect the browser to the authorization endpoint and pass the request parameters.
                    //DONE Ch5L1Ex3: The authorization endpoint should be retrieved from the metadata just like this: metaData.authorization_endpoint.
                    //DONE Ch5L1Ex3: Add the URLEncoded version of the requestParameters. Use $.param function to encode requestParameters.
                    var url = metaData.authorization_endpoint + '?'
                        + $.param(requestParameters);

                    window.location.replace(url);
                }
            };

            oauth2.init();

            return oauth2;
        })
        .factory('loginService', function ($http, $rootScope) {

            var loginService = {
                login: function (username, password) {
                    console.info("oauth2.login('" + username + "',*****)");

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
                    console.info("oauth2.validateToken(" + tokenId + ")");
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json',
                            'Authorization': 'Bearer ' + tokenId
                        },
                        url: "rest/auth/tokenInfo"
                    };
                    return $http(req)
                            .then(function (response) {
                                return {
                                    tokenId: tokenId,
                                    valid: true,
                                    uid: response.data.user.uid
                                };
                            });
                },
                getUserAttributes: function (tokenId, username) {
                    console.info("oauth2.getUserAttributes(" + tokenId + ")");
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json',
                            'Authorization': 'Bearer ' + tokenId
                        },
                        url: "rest/auth/tokenInfo"
                    };
                    return $http(req).then(function (response) {
                        var user = response.data.user;
                        var privilegeArray = response.data.privileges;
                        var privilegeSet = {};
                        angular.forEach(privilegeArray, function (privilege) {
                            console.log("Found privilege: " + privilege);
                            privilegeSet[privilege] = true;
                        });
                        user.privileges = privilegeSet;
                        console.log("User attributes are: " + JSON.stringify(user));
                        return user;
                    });
                },
                logout: function (tokenId) {
                    console.info("oauth2.logout called with tokenId=" + tokenId);
                    var req = {
                        'method': 'DELETE',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId)
                    };
                    var promise = $http(req).then(function (response) {
                        var logoutURL = "http://login.example.com:18080/am/UI/Logout?"
                                + $.param({goto: window.location.href});

                        window.location.replace(logoutURL);
                    });
                    return promise;
                },
                customizeHTTP: function (http) {
                    http.defaults.headers.common.Authorization
                            = "Bearer " + encodeURIComponent($rootScope.user.tokenId);
                }

            };
            return loginService;
        });

