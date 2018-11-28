angular.module('contactList.contactgroups', [
    'ui.router', 'services.auth', 'services.contact', 'services.userProfile', 'angularResizable', 'ui.bootstrap'
])

        .config(
                function ($stateProvider) {
                    $stateProvider
                            ////////////////////
                            // Contact Groups //
                            ////////////////////
                            .state('contactgroups', {
                                url: '/contactgroups',
                                templateUrl: 'app/contactgroups/selectOwner.html',
                                controller: function ($rootScope, $scope, $state, userProfiles) {
                                    $scope.selected = {};

                                    $scope.userProfiles = userProfiles;
                                    $scope.multipleUserProfiles = userProfiles.length > 1;

                                    if ($state.current.name === "contactgroups") {
                                        $state.go("contactgroups.listgroups", {owner: $rootScope.user.uid});
                                    }
                                },
                                resolve: {
                                    userProfiles: function (userProfileService, $rootScope) {
                                        console.log("RESOLVING contactgroups");
                                        if (!$rootScope.user.privileges['contact-groups/read']) {
                                            console.log("We do not have the privilege to read the list of user profiles");
                                            return [];
                                        }
                                        return userProfileService.getActiveUserProfiles();
                                    }
                                }
                            })
                            .state('contactgroups.listgroups', {
                                url: '/{owner:[\\w\\-]{1,}}',
                                displayName: 'Contact Groups',
                                templateUrl: 'app/contactgroups/selectGroup.html',
                                controller: function ($rootScope, $scope, $stateParams, contactGroups, groupFunctions) {

                                    $scope.selected.owner = $stateParams.owner;
                                    $scope.resourceIsOwnedByCurrentUser = $scope.selected.owner === $rootScope.user.uid;
                                    $scope.selected.groupId = null;
                                    $scope.selected.contactId = null;

                                    $scope.groups = contactGroups;

                                    $rootScope.bindFunctionsToCurrentScope(groupFunctions, $scope);
                                },
                                resolve: {
                                    contactGroups: function (contactService, $stateParams) {
                                        console.log("RESOLVING contactgroups.listgroups..." + JSON.stringify($stateParams));
                                        return contactService.getAllGroups($stateParams.owner);
                                    },
                                    groupFunctions: function ($rootScope, contactService, $state) {
                                        var updateGroupInList = function ($scope, updatedGroup) {
                                            var found = false;
                                            $scope.originalGroup = angular.copy(updatedGroup);
                                            $scope.group = angular.copy(updatedGroup);
                                            $scope.selected.groupId = updatedGroup._id;
                                            $scope.selected.owner = updatedGroup.owner;
                                            angular.forEach($scope.groups, function (group, index) {
                                                if (group._id === updatedGroup._id &&
                                                        group.owner === updatedGroup.owner) {
                                                    $scope.groups[index] = angular.copy(updatedGroup);
                                                    found = true;
                                                }

                                            });
                                            if (!found) {
                                                $scope.groups.push(updatedGroup);
                                            }
                                        };
                                        var deleteGroupFromList = function ($scope, groupToDelete) {
                                            $scope.originalGroup = {};
                                            $scope.group = {};
                                            var newgroups = [];
                                            var indexOfDeleted = 0;
                                            angular.forEach($scope.groups, function (group, index) {
                                                if (group._id === groupToDelete._id &&
                                                        group.owner === groupToDelete.owner) {
                                                    indexOfDeleted = index;
                                                } else {
                                                    newgroups.push(group);
                                                }
                                            });
                                            $scope.groups = newgroups;
                                            return Math.min(newgroups.length - 1, indexOfDeleted);
                                        };

                                        var loadGroup = function ($scope) {
                                            console.log("loading group.... ");
                                            var rev = $scope.group ? $scope.group._rev : undefined;
                                            return contactService.getGroup($scope.selected, rev)
                                                    .then(function (group) {
                                                        $scope.updateGroupInList(group);
                                                        $state.displayName = "Edit " + group.name;
                                                    }, function (response) {
                                                        if (response.status === 404) {
                                                            var deletedGroup = {
                                                                _id: $scope.selected.groupId,
                                                                owner: $scope.selected.owner
                                                            };
                                                            $scope.changeViewAfterDelete(deletedGroup);
                                                        } else if (response.status === 304) {
                                                            $scope.group = angular.copy($scope.originalGroup);
                                                            console.log("Contact Group has not changed.");
                                                        } else {
                                                            $rootScope.addErrorMessage("Unable to load Contact Group: " + response.data.message);
                                                        }
                                                    });
                                        };

                                        //TODO Ch6L1Ex2: Check the manageSharing function: this is responsible for displaying the resource set sharing page in openam
                                        var manageSharing = function ($scope, group) {
                                            var win = window.open('http://login.example.com:18080/am/XUI/#uma/resources/myresources/all/' + group.resourceSetId, '_blank');
                                            if (win) {
                                                //Browser has allowed it to be opened
                                                win.focus();
                                            } else {
                                                //Broswer has blocked it
                                                alert('Please allow popups for this site');
                                            }
                                        };

                                        //TODO Ch6L1Ex2: Check the shareGroup function: it uses the shareGroup function of the contactService
                                        var shareGroup = function ($scope, group) {
                                            contactService.shareGroup(group)
                                                    .then(function (updatedGroup) {
                                                        console.log("share group succeeded: " + JSON.stringify(updatedGroup));
                                                        $scope.alerts.push({msg: 'Successfully shared contact group.', type: 'success', dismissOnTimeout: 2000});
                                                        $scope.updateGroupInList(updatedGroup);
                                                        $state.go('contactgroups.listgroups.listcontacts', $scope.selected);
                                                    }, function (response) {
                                                        if (response.data.startFlowURL) {
                                                            window.location.href = response.data.startFlowURL;
                                                        } else {
                                                            $rootScope.addErrorMessage('Failed to share contact group. (' + response.data.message + ')');
                                                        }
                                                    });
                                        };

                                        var unshareGroup = function ($scope, group) {
                                            contactService.unshareGroup(group)
                                                    .then(function (updatedGroup) {
                                                        console.log("unshare group succeeded: " + JSON.stringify(updatedGroup));
                                                        $scope.alerts.push({msg: 'Successfully unshared contact group.', type: 'success', dismissOnTimeout: 2000});
                                                        $scope.updateGroupInList(updatedGroup);
                                                        $state.go('contactgroups.listgroups.listcontacts', $scope.selected);
                                                    }, function (response) {
                                                        if (response.data.startFlowURL) {
                                                            window.location.href = response.data.startFlowURL;
                                                        } else {
                                                            $rootScope.addErrorMessage('Failed to unshare contact group. (' + response.data.message + ')');
                                                        }
                                                    });
                                        };

                                        var revertGroup = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            $scope.loadGroup();
                                        };

                                        var changeViewAfterDelete = function ($scope, deletedGroup) {
                                            console.log("changeViewAfterDelete " + JSON.stringify(deletedGroup));
                                            var indexToShow = $scope.deleteGroupFromList(deletedGroup);
                                            if ($scope.groups.length === 0) {
                                                $state.go('contactgroups.create');
                                            } else {
                                                var editParams = {owner: $scope.groups[indexToShow].owner, groupId: $scope.groups[indexToShow]._id};
                                                console.log("editParams: " + JSON.stringify(editParams));
                                                $rootScope.keepAlerts = true;
                                                $state.go('^', editParams);
                                            }
                                        };

                                        var deleteGroup = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            contactService.deleteGroup($scope.selected)
                                                    .then(function (response) {
                                                        $rootScope.addSuccessMessage('Successfully deleted contact group.');
                                                        $scope.changeViewAfterDelete($scope.group);
                                                    }, function (response) {
                                                        $scope.alerts.push({msg: 'Failed to delete contact group. (' + response.data.message + ')', type: 'danger'});
                                                    });
                                        };

                                        var saveGroup = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            contactService.updateGroup($scope.selected, $scope.group)
                                                    .then(function (updatedGroup) {
                                                        $scope.alerts.push({msg: 'Successfully modified contact group.', type: 'success', dismissOnTimeout: 2000});
                                                        $scope.updateGroupInList(updatedGroup);
                                                        $state.go('^.listcontacts', $scope.selected);
                                                    }, function (response) {
                                                        if (response.status === 304) {
                                                            $rootScope.addWarningMessage('Contact group was not modified.');
                                                            $state.go('^.listcontacts', $scope.selected);
                                                        } else {
                                                            $rootScope.addErrorMessage('Failed to modify contact group. (' + response.data.message + ')');
                                                        }
                                                    });
                                        };
                                        var createGroup = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            var id = {owner: $scope.selected.owner, groupId: $scope.group._id};
                                            contactService.createGroup(id, $scope.group)
                                                    .then(function (group) {
                                                        $rootScope.addSuccessMessage('Successfully created contact group.');
                                                        $scope.updateGroupInList(group);
                                                        $rootScope.keepAlerts = true;
                                                        $state.go('^.listcontacts', $scope.selected);
                                                    }, function (response) {
                                                        $rootScope.addErrorMessage('Failed to create contact group. (' + response.data.message + ')');
                                                    });
                                        };

                                        var clearGroup = function ($scope) {
                                            $scope.group = {};
                                            $scope.loadedGroup = {};
                                            $scope.selected.groupId = null;
                                        };

                                        return {
                                            updateGroupInList: updateGroupInList,
                                            deleteGroupFromList: deleteGroupFromList,
                                            loadGroup: loadGroup,
                                            unshareGroup: unshareGroup,
                                            shareGroup: shareGroup,
                                            manageSharing: manageSharing,
                                            saveGroup: saveGroup,
                                            clearGroup: clearGroup,
                                            createGroup: createGroup,
                                            revertGroup: revertGroup,
                                            deleteGroup: deleteGroup,
                                            changeViewAfterDelete: changeViewAfterDelete
                                        };
                                    }
                                }

                            })

                            .state('contactgroups.listgroups.edit', {
                                url: '/edit:{groupId:[\\w\\-]{1,}}',
                                displayName: 'Edit Contact Group',
                                templateUrl: 'app/contactgroups/editGroup.html',
                                controller: function ($rootScope, $scope, $state, $stateParams, contactService, groupFunctions, group) {
                                    console.log("contactgroups.edit controller " + JSON.stringify($stateParams));
                                    $scope.selected.groupId = $stateParams.groupId;

                                    console.log("owner:" + $scope.selected.owner);
                                    console.log("groupId:" + $scope.selected.groupId);
                                    console.log("group: " + JSON.stringify(group));
                                    $scope.updateGroupInList(group);

                                },
                                resolve: {
                                    'group': function (contactService, $stateParams) {
                                        console.log("resolving group.edit.... ")

                                        return contactService.getGroup($stateParams);
                                    }
                                }

                            })
                            .state('contactgroups.listgroups.edit.share', {
                                url: '/share',
                                template: 'x',
                                controller: function ($scope, $stateParams, group) {
                                    console.log("contactgroups.share controller " + JSON.stringify($stateParams));
                                    $scope.shareGroup(group);
                                }
                            })
                            .state('contactgroups.listgroups.edit.unshare', {
                                url: '/unshare',
                                template: 'x',
                                controller: function ($scope, $stateParams, group) {
                                    console.log("contactgroups.unshare controller " + JSON.stringify($stateParams));
                                    $scope.unshareGroup(group);
                                }
                            })
                            .state('contactgroups.listgroups.create', {
                                templateUrl: 'app/contactgroups/createGroup.html',
                                controller: function ($rootScope, $scope, $state) {
                                    console.log("contactgroups.create controller");

                                    $rootScope.clearAlerts();

                                    $scope.cancel = function () {
                                        $state.go("^");
                                    };

                                    $scope.clearGroup();
                                }
                            })
                            ////////////////////
                            //     Contacts   //
                            ////////////////////
                            .state('contactgroups.listgroups.listcontacts', {
                                url: '/{groupId:[\\w\\-]{1,}}',
                                templateUrl: "app/contactgroups/selectContact.html",
                                controller: function ($scope, $rootScope, $stateParams, contactFunctions, contacts) {
                                    console.log("contactgroups.listgroups.listcontacts controller");
                                    $scope.selected.contactId = null;
                                    $scope.selected.groupId = $stateParams.groupId;


                                    console.log("owner:" + $scope.selected.owner);
                                    console.log("groupId:" + $scope.selected.groupId);
                                    $rootScope.bindFunctionsToCurrentScope(contactFunctions, $scope);

                                    $scope.contacts = contacts;

                                },
                                resolve: {
                                    contacts: function (contactService, $stateParams) {
                                        console.log("RESOLVING contactgroups.listgroups.listcontacts..." + JSON.stringify($stateParams));
                                        return contactService.getContactsByGroupId($stateParams);
                                    },
                                    contactFunctions: function ($rootScope, contactService, $state) {
                                        var updateContactInList = function ($scope, updatedContact) {
                                            var found = false;
                                            $scope.originalContact = angular.copy(updatedContact);
                                            $scope.contact = angular.copy(updatedContact);
                                            $scope.selected.owner = updatedContact.owner;
                                            $scope.selected.groupId = updatedContact.groupId;
                                            $scope.selected.contactId = updatedContact._id;
                                            angular.forEach($scope.contacts, function (contact, index) {
                                                if (contact._id === updatedContact._id) {
                                                    $scope.contacts[index] = angular.copy(updatedContact);
                                                    found = true;
                                                }

                                            });
                                            if (!found) {
                                                $scope.contacts.push(updatedContact);
                                            }
                                        };
                                        var deleteContactFromList = function ($scope, contactToDelete) {
                                            $scope.originalContact = {};
                                            $scope.contact = {};
                                            var newcontacts = [];
                                            var indexOfDeleted = 0;
                                            angular.forEach($scope.contacts, function (contact, index) {
                                                if (contact._id === contactToDelete._id &&
                                                        contact.owner === contactToDelete.owner) {
                                                    indexOfDeleted = index;
                                                } else {
                                                    newcontacts.push(contact);
                                                }
                                            });
                                            $scope.contacts = newcontacts;
                                            return Math.min(newcontacts.length - 1, indexOfDeleted);
                                        };

                                        var loadContact = function ($scope) {
                                            console.log("loading contact.... ");
                                            var rev = $scope.contact ? $scope.contact._rev : undefined;
                                            return contactService.getContact($scope.selected, rev)
                                                    .then(function (contact) {
                                                        $scope.updateContactInList(contact);
                                                        $state.displayName = "Edit " + contact.name;
                                                    }, function (response) {
                                                        if (response.status === 404) {
                                                            var deletedContact = {
                                                                _id: $scope.selected.contactId,
                                                                owner: $scope.selected.owner
                                                            };
                                                            $scope.changeViewAfterDelete(deletedContact);
                                                        } else if (response.status === 304) {
                                                            $scope.contact = angular.copy($scope.originalContact);
                                                            console.log("Contact has not changed.");
                                                        } else {
                                                            $rootScope.addErrorMessage("Unable to load Contact: " + response.data.message);
                                                        }
                                                    });
                                        };

                                        var revertContact = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            $scope.loadContact();
                                        };

                                        var changeViewAfterDelete = function ($scope, deletedContact) {
                                            console.log("changeViewAfterDelete " + JSON.stringify(deletedContact));
                                            var indexToShow = $scope.deleteContactFromList(deletedContact);
                                            if ($scope.contacts.length === 0) {
                                                $state.go('^.^.create');
                                            } else {
                                                var editParams = {
                                                    owner: $scope.selected.owner,
                                                    groupId: $scope.selected.groupId,
                                                    contactId: $scope.contacts[indexToShow]._id
                                                };
                                                console.log("editParams: " + JSON.stringify(editParams));
                                                $rootScope.keepAlerts = true;
                                                $state.go('^', editParams);
                                            }
                                        };

                                        var deleteContact = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            contactService.deleteContact($scope.selected)
                                                    .then(function (response) {
                                                        $rootScope.addSuccessMessage('Successfully deleted contact.');
                                                        $scope.changeViewAfterDelete($scope.contact);
                                                    }, function (response) {
                                                        $scope.alerts.push({msg: 'Failed to delete contact. (' + response.data.message + ')', type: 'danger'});
                                                    });
                                        };

                                        var saveContact = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            contactService.updateContact($scope.selected, $scope.contact)
                                                    .then(function (updatedContact) {
                                                        $scope.alerts.push({msg: 'Successfully modified contact.', type: 'success', dismissOnTimeout: 2000});
                                                        $scope.updateContactInList(updatedContact);
                                                        $state.go('^', $scope.selected);
                                                    }, function (response) {
                                                        if (response.status === 304) {
                                                            $rootScope.addWarningMessage('Contact was not modified.');
                                                            $state.go('^', $scope.selected);
                                                        } else {
                                                            $rootScope.addErrorMessage('Failed to modify contact. (' + response.data.message + ')');
                                                        }
                                                    });
                                        };
                                        var createContact = function ($scope) {
                                            $rootScope.clearAlerts(true);
                                            var id = {
                                                owner: $scope.selected.owner,
                                                groupId: $scope.selected.groupId,
                                                contactId: $scope.contact._id
                                            };
                                            contactService.createContact(id, $scope.contact)
                                                    .then(function (contact) {
                                                        $rootScope.addSuccessMessage('Successfully created contact.');
                                                        $scope.updateContactInList(contact);
                                                        $rootScope.keepAlerts = true;
                                                        $state.go('^.details', $scope.selected);
                                                    }, function (response) {
                                                        $rootScope.addErrorMessage('Failed to create contact. (' + response.data.message + ')');
                                                    });
                                        };

                                        var clearContact = function ($scope) {
                                            $scope.contact = {};
                                            $scope.loadedContact = {};
                                            $scope.selected.contactId = null;
                                        };

                                        return {
                                            updateContactInList: updateContactInList,
                                            deleteContactFromList: deleteContactFromList,
                                            loadContact: loadContact,
                                            saveContact: saveContact,
                                            clearContact: clearContact,
                                            createContact: createContact,
                                            revertContact: revertContact,
                                            deleteContact: deleteContact,
                                            changeViewAfterDelete: changeViewAfterDelete
                                        };
                                    }
                                }

                            })
                            .state('contactgroups.listgroups.listcontacts.details', {
                                url: '/{contactId:[\\w\\-]{1,}}',
                                views: {
                                    "details": {
                                        templateUrl: "app/contactgroups/contact.html",
                                        controller: function ($scope, $stateParams, contact) {
                                            $scope.clearAlerts();
                                            $scope.selected.contactId = $stateParams.contactId;

                                            console.log("detail:contactId:" + $stateParams.contactId);

                                            console.log("contact: " + JSON.stringify(contact));
                                            $scope.updateContactInList(contact);
                                        },
                                        resolve: {
                                            'contact': function (contactService, $stateParams) {
                                                console.log("loading contact " + JSON.stringify($stateParams));
                                                return contactService.getContact($stateParams);
                                            }
                                        }

                                    }
                                }

                            })
                            .state('contactgroups.listgroups.listcontacts.details.edit', {
                                url: '/edit',
                                templateUrl: 'app/contactgroups/editContact.html',
                                controller: function ($rootScope, $scope, $stateParams) {
                                    console.log("contactgroups.listgroups.listcontacts.details.edit controller " + JSON.stringify($stateParams));
                                    $scope.selected.owner = $stateParams.owner;
                                    $scope.selected.contactId = $stateParams.contactId;


                                    console.log("owner:" + $scope.selected.owner);
                                    console.log("contactId:" + $scope.selected.contactId);

                                    $rootScope.clearAlerts();

                                }

                            }).state('contactgroups.listgroups.listcontacts.create', {
                        views: {
                            details: {
                                templateUrl: 'app/contactgroups/createContact.html',
                                controller: function ($rootScope, $scope, $state) {
                                    console.log("contactgroups.listgroups.listcontacts.create controller");

                                    $rootScope.clearAlerts();

                                    $scope.cancel = function () {
                                        $state.go("^");
                                    };

                                    $scope.clearContact();
                                }

                            }
                        }
                    });
                }
        );
