angular
        .module('services.auth.saml2', [])
        .constant('openamConstants', {
            serverURI: 'http://login.example.com:18080/am'
        })
        .factory('loginService', function ($http, $rootScope, openamConstants) {

            var loginService = {
                login: function (username, password) {
                    console.info("saml2.login(" + tokenId + ")");

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
                    console.info("saml2.validateToken(" + tokenId + ")");
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId)
                    };
                    return $http(req); // returning with the promise
                },
                getUserAttributes: function (tokenId, username) {
                    console.info("saml2.getUserAttributes(" + tokenId + ")");
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId),
                        transformResponse: function (data, headers) {
                            data = JSON.parse(data);
                            var user = data.user;
                            user.username = user.uid;
                            return user;
                        }
                    };
                    return $http(req); // returning with the promise

                },
                logout: function (tokenId) {
                    console.info("saml2.logout called with tokenId=" + tokenId);

                    var req = {
                        'method': 'DELETE',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId)
                    };

                    var promise = $http(req);
                    promise.then(function () {
                        var relayStateURL = window.location.protocol + "//"
                                + window.location.hostname + ":"
                                + window.location.port
                                + window.location.pathname;
                        console.info("relayStateURL: " + relayStateURL);

                        window.location.replace(openamConstants.serverURI
                                + "/IDPSloInit?metaAlias=" + encodeURIComponent("/idp")
                                + "&binding=urn:oasis:names:tc:SAML:2.0:bindings:SOAP"
                                + "&RelayState=" + encodeURIComponent(relayStateURL));
                    });
                    return promise;
                },
                getPrivileges: function (tokenId) {
                    console.info("saml2.getPrivileges(" + tokenId + ")");
                    var privilegesPrefix = "contactlist://privileges/";
                    var req = {
                        'method': 'GET',
                        headers: {
                            'Accept': 'application/json'
                        },
                        url: "rest/auth/tokens/" + encodeURIComponent(tokenId),
                        transformResponse: function (data, headers) {
                            data = JSON.parse(data);
                            var privileges = {};
                            console.info("Privileges: " + data.privileges);
                            $.each(data.privileges, function (index, privilegeName) {
                                var privilege = privilegeName.substring(privilegesPrefix.length);
                                privileges[privilege] = {allowed: true};
                            });
                            return privileges;
                        },
                    };
                    return $http(req); // returning with the promise
                },
                customizeHTTP: function (http) {
                    http.defaults.headers.common.Authorization
                            = "Bearer " + encodeURIComponent($rootScope.user.tokenId);
                }
            };
            return loginService;
        });

