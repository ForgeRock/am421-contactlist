angular
        .module('services.auth.openam', [])
        .constant('openamConstants', {
            serverURI: 'http://login.example.com:18080/am'
        })
        .factory('loginService', function ($http, $rootScope, openamConstants, $q) {

            var loginService = {};

            loginService.login = function (username, password) {
                var req = {
                    method: 'POST',
                    url: openamConstants.serverURI + '/json/authenticate',
                    params: {
                        //DONE Ch4L2Ex2: Add query parameters authIndexType:'service' and authIndexValue:'testSelectRole'
                        authIndexType:'service',
                        authIndexValue:'testSelectRole'
                    },
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Accept-API-Version': 'resource=1.1, protocol=1.0',
                        'X-OpenAM-Username': username,
                        'X-OpenAM-Password': password
                    },
                    data: {}
                };
                return $http(req); // returning with the promise

            };

            loginService.submitChoice = function (receivedData, selectedIndex) {
                // this method sends back received data with a simple modification:
                // selectedIndex in ChoiceCallback is modified to the one 
                // that is received as an argument of this function.

                receivedData.callbacks[0].input[0].value = selectedIndex;

                var req = {
                    method: 'POST',
                    url: openamConstants.serverURI + '/json/authenticate',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Accept-API-Version': 'resource=1.1, protocol=1.0'
                    },
                    data: receivedData
                };
                return $http(req); // returning with the promise

            };

            loginService.getChoiceCallback = function (data) {
                //this function transforms data into the following format:
                var choiceCallback = {
                    selectedRole: null, // contains a number (index of selected role)
                    selectableRoles: [] // contains an array of role names
                };

                var selectedIndex = 0;

                $.each(data.callbacks, function (callbackIndex, callback) {
                    if (callback.type === 'ChoiceCallback') {
                        $.each(callback.output, function (outputIndex, output) {
                            if (output.name === 'choices') {
                                var i = 0;
                                $.each(output.value, function (choiceIndex, choice) {
                                    var option = {
                                        value: i++,
                                        label: choice
                                    };
                                    choiceCallback.selectableRoles.push(option);
                                });
                            } else if (output.name === 'defaultChoice') {
                                selectedIndex = output.value;
                            }
                        })
                    }
                });

                choiceCallback.selectedRole = choiceCallback.selectableRoles[selectedIndex];

                return choiceCallback;
            };

            loginService.validateToken = function (tokenId) {
                //validates tokenId against OpenAM
                console.info("openam.validateToken(" + tokenId + ")");
                //DONE Ch4L2Ex1: Send a POST request to serverURI/json/sessions/<tokenId>?_action=validate
                var req = {
                        method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Accept-API-Version': 'resource=1.1, protocol=1.0'
                    },
                    url: openamConstants.serverURI + "/json/sessions/" + tokenId,
                    data: {},
                    params: {
                        _action: 'validate'
                    }

                };
                return $http(req).then(function (response) {
                    var tokenInfo = response.data;
                    tokenInfo.tokenId = tokenId;
                    return tokenInfo;
                }); // returning with the promise
            };

            loginService.getUserAttributes = function (tokenId, username) {
                
                //gets user attributes for a user with username
                //and tokenId
                console.info("openam.getUserAttributes(" + tokenId + ")");
                var attrMapping = {
                    "username": "uid",
                    "mail": "email",
                    "cn": "name",
                    "sn": "familyName",
                    "givenName": "givenName"
                };
                //DONE Ch4L2Ex1: Get the user profile by sending a GET request to serverURI/json/users/<username>?_fields=<comma separated field names>. Hint: use the encodeURIComponent function to urlencode the username in the path.
                //DONE Ch4L2Ex1: Ex1: Pass the tokenId as a header named iPlanetDirectoryPro.
                //DONE Ch4L2Ex1: Query the following fields: username,mail,cn,sn,givenName.
                var req = {
                    'method': 'GET',
                    'url': openamConstants.serverURI + "/json/users/" + encodeURIComponent(username),
                    headers: {
                        'iPlanetDirectoryPro': tokenId
                    },
                    params: {
                        _fields: "username,mail,cn,sn,givenName"
                    }
                };

                var deferred = $q.defer();

                $http(req).then(function (response) {
                    //transforms attribute map based on attrMapping object
                    //if mapping is not defined for an attribute, copies it
                    //with the same name.
                    //if the attribute's value is an array type, then it
                    //is transformed to the value of the first element in the
                    //original array
                    var user = {};
                    $.each(response.data, function (key, value) {
                        if ($.isArray(value)) {
                            value = value[0];
                        }
                        var newKey = attrMapping[key];
                        if (newKey === undefined) {
                            newKey = key;
                        }
                        user[newKey] = value;
                    });

                    //DONE Ch4L4Ex3: call getPrivilegesFromOpenAM
                    loginService.getPrivilegesFromOpenAM(tokenId)
                            .then(function (privileges) {
                                user.privileges = privileges;
                                deferred.resolve(user);
                            }, function (errorResponse) {
                                deferred.reject(errorResponse);
                            });

                }, function (errorResponse) {
                    deferred.reject(errorResponse);
                });

                return deferred.promise;
            };
            loginService.logout = function (tokenId) {
                //Invalidates session with id: tokenId
                console.info("openam.logout called with tokenId=" + tokenId);
                //DONE Ch4L2Ex1: Send a logout request to OpenAM. Send a POST request to serverURI/json/sessions/?_action=logout with an iPlanetDirectoryPro header.
                //DONE Ch4L2Ex1: Send Content-Type and Accept headers as well with the value 'application/json'.
                var req = {
                    'method': 'POST',
                    'url': openamConstants.serverURI + "/json/sessions/",
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Accept-API-Version': 'resource=1.1, protocol=1.0',
                        'iPlanetDirectoryPro': tokenId
                    },
                    data: {},
                    params: {
                        _action: 'logout'
                    }
                    
                };
                var promise = $http(req);
                return promise;
            };

            loginService.getPrivilegesFromOpenAM = function (tokenId) {
                console.info("openam.getPrivileges(" + tokenId + ")");
                //DONE Ch4L4Ex3: Complete the policy evaluation REST request: resource should be "privileges", application should be "ContactListPrivileges"
                var req = {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Accept-API-Version': 'resource=2.0, protocol=1.0',
                        'iPlanetDirectoryPro': tokenId
                    },
                    url: openamConstants.serverURI + "/json/policies",
                    data: {
                        application: 'ContactListPrivileges',
                        resources: ['privileges']
                    },
                    params: {
                        _action: 'evaluate'
                    }
                };

                return $http(req).then(function (response) {
                    console.info("Policy evaluation raw result: " + JSON.stringify(response.data));
                    var privileges = {};
                    angular.forEach(response.data, function (entitlement) {
                        privileges = entitlement.actions;
                    });

                    console.log(JSON.stringify(privileges));

                    return privileges;

                });
            };

            loginService.getPrivilegesFromLocalBackend = function (tokenId) {
                console.info("getPrivilegesFromLocalApp(" + tokenId + ")");
                var req = {
                    'method': 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'Authorization': 'Bearer ' + tokenId
                    },
                    url: "rest/auth/privileges"
                };
                return $http(req)
                        .then(function (response) {
                            var privileges = {};
                            angular.forEach(response.data, function (privilege) {
                                privileges[privilege] = true;
                            });

                            console.log(JSON.stringify(privileges));
                            return privileges;
                        });
            };

            loginService.customizeHTTP = function (http) {
                http.defaults.headers.common.Authorization
                        = "Bearer " + encodeURIComponent($rootScope.user.tokenId);
            };

            return loginService;
        });

