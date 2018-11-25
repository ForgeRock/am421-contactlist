angular.module('services.userProfile', [
    'ui.router', 'services.auth'
])
        .constant('idRegex', /\/([^\/]+)$/)

        .factory('userProfileService', function ($http, $q, loginService, idRegex) {
            var user = null;
            var userProfilesPromise = null;
            var activeUserProfilesPromise = null;

            var userProfileService = {};

            userProfileService.setUser = function (newUser) {
                if (user !== newUser) {
                    userProfilesPromise = null;
                    activeUserProfilesPromise = null;
                    user = newUser;
                }
            };

            userProfileService.getUserProfiles = function () {
                if (user === null) {
                    return $q.reject("Not authenticated");
                }

                if (!userProfilesPromise) {
                    loginService.customizeHTTP($http);
                    userProfilesPromise = $http.get("rest/profiles")
                            .then(function (response) {
                                return response.data;
                            });
                }
                return userProfilesPromise;
            };
            userProfileService.getActiveUserProfiles = function () {
                if (user === null) {
                    return $q.reject("Not authenticated");
                }

                if (!activeUserProfilesPromise) {
                    loginService.customizeHTTP($http);
                    activeUserProfilesPromise = $http.get("rest/activeProfiles")
                            .then(function (response) {
                                return response.data;
                            });
                }
                return activeUserProfilesPromise;
            };

            userProfileService.createUserProfile = function (userProfile) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    url: "rest/profiles",
                    data: userProfile
                };
                var profilePromise =
                        $http(req)
                        .then(function (response) {
                            var location = response.headers('Location');
                            var revision = response.headers('ETag');
                            console.log("location header: " + location);
                            console.log("ETag header: " + revision);
                            if (location !== null) {
                                var userId = idRegex.exec(location)[1];
                                userProfile._id = userId;
                                userProfile._rev = revision;
                                return userProfile;
                            } else {
                                throw response;
                            }
                        });

                return profilePromise;
            };


            userProfileService.getUserProfile = function (uid, revision) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json'
                    },
                    url: "rest/profiles/" + encodeURIComponent(uid)
                };
                if (revision) {
                    req.headers['If-None-Match'] = revision;
                }
                var userProfilePromise =
                        $http(req)
                        .then(function (response) {
                            return response.data;
                        });

                return userProfilePromise;
            };

            userProfileService.updateUserProfile = function (uid, userProfile) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'If-Match': userProfile._rev
                    },
                    url: "rest/profiles/" + encodeURIComponent(uid),
                    data: userProfile
                };
                var userProfilePromise =
                        $http(req)
                        .then(function (response) {
                            var updatedUserProfile = angular.copy(userProfile);
                            updatedUserProfile._rev = response.headers('ETag');
                            return updatedUserProfile;
                        });

                return userProfilePromise;
            };

            userProfileService.deleteUserProfile = function (uid) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    url: "rest/profiles/" + encodeURIComponent(uid)
                };
                var userProfilePromise =
                        $http(req)
                        .then(function (response) {
                            return response.data;
                        });

                return userProfilePromise;
            };


            return userProfileService;
        })
        .run(function ($rootScope, userProfileService) {
            $rootScope.$on("loggedIn", function (event, data) {
                console.info("setting userProfileService.user to: " + JSON.stringify(data));
                userProfileService.setUser(data);
            });
        })
        ;
