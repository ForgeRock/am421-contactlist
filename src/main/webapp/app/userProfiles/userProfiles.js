angular.module('contactList.userProfiles', [
    'ui.router', 'services.auth', 'services.contact', 'services.userProfile', 'angularResizable', 'ui.bootstrap'
])

        .config(
                function ($stateProvider) {
                    $stateProvider
                            ////////////////////
                            // Contact Groups //
                            ////////////////////
                            .state('userProfiles', {
                                url: '/userProfiles',
                                templateUrl: 'app/userProfiles/userProfileList.html',
                                controller: function ($rootScope, $scope, $state, userProfiles, userProfileFunctions) {

                                    $scope.selected = {};

                                    $scope.userProfiles = userProfiles;
                                    $rootScope.bindFunctionsToCurrentScope(userProfileFunctions, $scope);

                                    console.log(JSON.stringify($state.current));
                                    if ($state.current.name === "userProfiles") {
                                        $state.go("userProfiles.edit", {uid: $rootScope.user.uid});
                                    }
                                },
                                resolve: {
                                    userProfiles: function (userProfileService) {
                                        console.log("RESOLVING userProfiles");
                                        return userProfileService.getUserProfiles();
                                    },
                                    userProfileFunctions: function ($rootScope, userProfileService, $state) {
                                        var updateUserProfileInList = function ($scope, updatedUserProfile) {
                                            var found = false;
                                            $scope.originalUserProfile = angular.copy(updatedUserProfile);
                                            $scope.userProfile = angular.copy(updatedUserProfile);
                                            $scope.selected.uid = updatedUserProfile._id;
                                            angular.forEach($scope.userProfiles, function (userProfile, index) {
                                                if (userProfile._id === updatedUserProfile._id &&
                                                        userProfile.owner === updatedUserProfile.owner) {
                                                    $scope.userProfiles[index] = angular.copy(updatedUserProfile);
                                                    found = true;
                                                }

                                            });
                                            if (!found) {
                                                $scope.userProfiles.push(updatedUserProfile);
                                            }
                                        };
                                        var deleteUserProfileFromList = function ($scope, userProfileToDelete) {
                                            $scope.originalUserProfile = {};
                                            $scope.userProfile = {};
                                            var newUserProfiles = [];
                                            var indexOfDeleted = 0;
                                            angular.forEach($scope.userProfiles, function (userProfile, index) {
                                                if (userProfile._id === userProfileToDelete._id) {
                                                    indexOfDeleted = index;
                                                } else {
                                                    newUserProfiles.push(userProfile);
                                                }
                                            });
                                            $scope.userProfiles = newUserProfiles;
                                            return Math.min(newUserProfiles.length - 1, indexOfDeleted);
                                        };

                                        var loadUserProfile = function ($scope) {
                                            console.log("loading User Profile.... ");
                                            var rev = $scope.userProfile ? $scope.userProfile._rev : undefined;
                                            return userProfileService.getUserProfile($scope.selected.uid, rev)
                                                    .then(function (userProfile) {
                                                        $scope.updateUserProfileInList(userProfile);
                                                        $state.displayName = "Edit " + userProfile.name;
                                                    }, function (response) {
                                                        if (response.status === 404) {
                                                            var deletedUserProfile = {
                                                                _id: $scope.selected.uid
                                                            };
                                                            $scope.changeViewAfterDelete(deletedUserProfile);
                                                        } else if (response.status === 304) {
                                                            $scope.userProfile = angular.copy($scope.originalUserProfile);
                                                            console.log("User Profile has not changed.");
                                                        } else {
                                                            $rootScope.addErrorMessage("Unable to load User Profile: " + response.data.message);
                                                        }
                                                    });
                                        };

                                        var revertUserProfile = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            $scope.loadUserProfile();
                                        };

                                        var changeViewAfterDelete = function ($scope, deletedUserProfile) {
                                            console.log("changeViewAfterDelete " + JSON.stringify(deletedUserProfile));
                                            var indexToShow = $scope.deleteUserProfileFromList(deletedUserProfile);
                                            if ($scope.userProfiles.length === 0) {
                                                $state.go('userProfiles.create');
                                            } else {
                                                var editParams = {uid: $scope.userProfiles[indexToShow]._id};
                                                console.log("editParams: " + JSON.stringify(editParams));
                                                $rootScope.keepAlerts = true;
                                                $state.go('^', editParams);
                                            }
                                        };

                                        var deleteUserProfile = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            userProfileService.deleteUserProfile($scope.selected.uid)
                                                    .then(function (response) {
                                                        $rootScope.addSuccessMessage('Successfully deleted User Profile.');
                                                        $scope.changeViewAfterDelete($scope.userProfile);
                                                    }, function (response) {
                                                        $scope.alerts.push({msg: 'Failed to delete User Profile. (' + response.data.message + ')', type: 'danger'});
                                                    });
                                        };

                                        var saveUserProfile = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            userProfileService.updateUserProfile($scope.selected.uid, $scope.userProfile)
                                                    .then(function (updatedUserProfile) {
                                                        $scope.alerts.push({msg: 'Successfully modified User Profile.', type: 'success', dismissOnTimeout: 2000});
                                                        $scope.updateUserProfileInList(updatedUserProfile);
                                                    }, function (response) {
                                                        if (response.status === 304) {
                                                            $rootScope.addWarningMessage('Contact userProfile was not modified.');
                                                        } else {
                                                            $rootScope.addErrorMessage('Failed to modify User Profile. (' + response.data.message + ')');
                                                        }
                                                    });
                                        };
                                        var createUserProfile = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            userProfileService.createUserProfile($scope.userProfile)
                                                    .then(function (userProfile) {
                                                        $rootScope.addSuccessMessage('Successfully created User Profile.');
                                                        $scope.updateUserProfileInList(userProfile);
                                                        $rootScope.keepAlerts = true;
                                                        $state.go('^.edit', $scope.selected);
                                                    }, function (response) {
                                                        $rootScope.addErrorMessage('Failed to create User Profile. (' + response.data.message + ')');
                                                    });
                                        };

                                        var clearUserProfile = function ($scope) {
                                            $scope.userProfile = {};
                                            $scope.loadedUserProfile = {};
                                            $scope.selected.uid = null;
                                        };

                                        return {
                                            updateUserProfileInList: updateUserProfileInList,
                                            deleteUserProfileFromList: deleteUserProfileFromList,
                                            loadUserProfile: loadUserProfile,
                                            saveUserProfile: saveUserProfile,
                                            clearUserProfile: clearUserProfile,
                                            createUserProfile: createUserProfile,
                                            revertUserProfile: revertUserProfile,
                                            deleteUserProfile: deleteUserProfile,
                                            changeViewAfterDelete: changeViewAfterDelete
                                        };
                                    }

                                }
                            })
                            .state('userProfiles.edit', {
                                url: '/{uid:[\\w\\-]{1,}}',
                                displayName: 'Edit User Profile',
                                templateUrl: 'app/userProfiles/editUserProfile.html',
                                controller: function ($rootScope, $scope, $state, $stateParams, userProfile) {
                                    console.log("userProfiles.edit controller " + JSON.stringify($stateParams));
                                    $scope.selected.uid = $stateParams.uid;

                                    console.log("uid:" + $scope.selected.uid);
                                    $scope.updateUserProfileInList(userProfile);

                                },
                                resolve: {
                                    'userProfile': function (userProfileService, $stateParams) {
                                        console.log("resolving userProfiles.edit.... ")

                                        return userProfileService.getUserProfile($stateParams.uid);
                                    }
                                }

                            })
                            .state('userProfiles.create', {
                                templateUrl: 'app/userProfiles/createUserProfile.html',
                                controller: function ($rootScope, $scope, $state) {
                                    console.log("userProfiles.create controller");

                                    $rootScope.clearAlerts();

                                    $scope.cancel = function () {
                                        $state.go("^");
                                    };

                                    $scope.clearUserProfile();
                                }
                            });
                }
        );
