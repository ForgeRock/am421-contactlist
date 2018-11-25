angular.module('services.contact', [
    'ui.router', 'services.auth'
])
        .constant('idRegex', /\/([^\/]+)$/)

        .factory('contactService', function ($http, $q, loginService, idRegex) {
            var user = null;
            var groupsPromises = {};
            var contactsPromises = {};

            var contactService = {};

            contactService.setUser = function (newUser) {
                if (user !== newUser) {
                    groupsPromise = null;
                    user = newUser;
                    contactsPromises = {};
                }
            };

            contactService.getAllGroups = function (owner) {
                if (!owner && user.uid) {
                    owner = user.uid;
                }
                if (owner === null) {
                    return $q.reject("owner is null");
                }

                var groupsPromise = groupsPromises[owner];
                if (!groupsPromise) {
                    loginService.customizeHTTP($http);
                    groupsPromise = $http.get("rest/owned-groups/" + owner)
                            .then(function (response) {
                                return response.data;
                            });
                    groupsPromises[owner] = groupsPromise;
                }
                return groupsPromise;
            };

            contactService.createGroup = function (id, group) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    url: "rest/owned-groups/" + id.owner,
                    data: group
                };
                var groupPromise =
                        $http(req)
                        .then(function (response) {
                            var location = response.headers('Location');
                            var revision = response.headers('ETag');
                            console.log("location header: " + location);
                            console.log("ETag header: " + revision);
                            if (location !== null) {
                                var groupId = idRegex.exec(location)[1];
                                group._id = groupId;
                                group._rev = revision;
                                group.owner = id.owner;
                                return group;
                            } else {
                                throw response;
                            }
                        });

                return groupPromise;
            };


            contactService.getGroup = function (id, revision) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json'
                    },
                    url: "rest/owned-groups/" + id.owner + "/" + id.groupId
                };
                if (revision) {
                    req.headers['If-None-Match'] = revision;
                }
                var groupPromise =
                        $http(req)
                        .then(function (response) {
                            return response.data;
                        });

                return groupPromise;
            };

            contactService.updateGroup = function (id, group) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'If-Match': group._rev
                    },
                    url: "rest/owned-groups/" + id.owner + "/" + id.groupId,
                    data: group
                };
                var contactPromise =
                        $http(req)
                        .then(function (response) {
                            var updatedGroup = angular.copy(group);
                            updatedGroup._rev = response.headers('ETag');
                            return updatedGroup;
                        });

                return contactPromise;
            };

            contactService.deleteGroup = function (id) {
                var groupId = id.groupId ? id.groupId : id._id;
                loginService.customizeHTTP($http);
                var req = {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    url: "rest/owned-groups/" + id.owner + "/" + groupId
                };
                var contactPromise =
                        $http(req)
                        .then(function (response) {
                            return response.data;
                        });

                return contactPromise;
            };

            contactService.getContactsByGroupId = function (id) {
                var groupPath = id.owner + "/" + id.groupId;
                var contactsPromise = contactsPromises[groupPath];
                if (contactsPromise === undefined) {
                    loginService.customizeHTTP($http);
                    contactsPromise = $http.get("rest/owned-groups/" + groupPath + "/contacts")
                            .then(function (response) {
                                return response.data;
                            });
                    contactsPromises[groupPath] = contactsPromise;

                }
                return contactsPromise;
            };

            contactService.getContact = function (id) {
                loginService.customizeHTTP($http);
                var contactPromise =
                        $http.get("rest/owned-groups/" + id.owner + "/" + id.groupId + "/contacts/" + id.contactId)
                        .then(function (response) {
                            var contact = response.data;
                            contact.owner = id.owner;
                            contact.groupId = id.groupId;
                            return contact;
                        });

                return contactPromise;
            };

            contactService.updateContact = function (id, contact) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'If-Match': contact._rev
                    },
                    url: "rest/owned-groups/" + id.owner + "/" + id.groupId + "/contacts/" + id.contactId,
                    data: contact
                };
                var contactPromise =
                        $http(req)
                        .then(function (response) {
                            var updatedContact = angular.copy(contact);
                            updatedContact._rev = response.headers('ETag');
                            return updatedContact;
                        });

                return contactPromise;
            };

            contactService.createContact = function (id, contact) {
                loginService.customizeHTTP($http);
                var req = {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    url: "rest/owned-groups/" + id.owner + "/" + id.groupId + "/contacts",
                    data: contact
                };
                var contactPromise =
                        $http(req)
                        .then(function (response) {
                            var location = response.headers('Location');
                            var revision = response.headers('ETag');
                            console.log("location header: " + location);
                            console.log("ETag header: " + revision);
                            if (location !== null) {
                                var contactId = idRegex.exec(location)[1];
                                contact._id = contactId;
                                contact._rev = revision;
                                contact.groupId = id.groupId;
                                contact.owner = id.owner;
                                return contact;
                            } else {
                                throw response;
                            }
                        });

                return contactPromise;
            };
            
            contactService.deleteContact = function (id) {
                var contactId = id.contactId ? id.contactId : id._id;
                loginService.customizeHTTP($http);
                var req = {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    url: "rest/owned-groups/" + id.owner + "/" + id.groupId + "/contacts/" + contactId
                };
                var contactPromise =
                        $http(req)
                        .then(function (response) {
                            return response.data;
                        });

                return contactPromise;
            };            

            return contactService;
        })
        .run(function ($rootScope, contactService) {
            $rootScope.$on("loggedIn", function (event, data) {
                console.info("setting contacts.user to: " + JSON.stringify(data));
                contactService.setUser(data);
            });
        })
        ;
