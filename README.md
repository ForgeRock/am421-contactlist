# ContactList application #

FR421 sample project to demonstrate integration with OpenAM 13.

## Backend ##

Restful webservices implemented in a JavaEE application based on JAX-RS 2.0. 

### Implementation ###

Jersey and Jackson is used.

### Features ###

#### /rest/owned-groups/``uid`` ####

GET: gets all contact groups of a user

POST: creates a new contact group

#### /rest/owned-groups/``uid``/``groupId`` ####

GET: gets the contact group specified by ``uid`` and ``groupId``

DELETE: deletes the contact group specified by ``uid`` and ``groupId``

PUT: modifies the contact group specified by ``uid`` and ``groupId``

#### /rest/owned-groups/``uid``/``groupId``/contacts ####

GET: gets all contacts in a contact group specified by ``uid`` and ``groupId``

#### /rest/owned-groups/``uid``/``groupId``/contacts/``contactId`` ####

GET: gets the contact specified by ``uid``,``groupId`` and ``contactId``

DELETE: deletes the contact specified by ``uid``,``groupId`` and ``contactId``

PUT: modifies the contact specified by ``uid``,``groupId`` and ``contactId``

## Frontend ##

Javascript based frontend based on core Angular.js 1.4.9 + angular-ui-router 0.2.17

Supported url hashes:
### #/userProfiles ###
Displays all user profiles
### #/userProfiles/``uid`` ###
Displays one specific user profile
### #/contactgroups ###
Displays the contact groups owned by the current user
### #/contactgroups/``uid`` ###
Displays the contact groups owned by the user specified by ``uid``
### #/contactgroups/``uid``/``groupId`` ###
Displays the contacts in the contact contact group specified by ``uid`` and ``groupId``
### #/contactgroups/``uid``/``groupId``/edit ###
Edit the contact group specified by ``uid`` and ``groupId``
### #/contactgroups/``uid``/``groupId``/``contactId`` ###
Displays the contact specified by ``uid``,``groupId`` and ``contactId``
### #/contactgroups/``uid``/``groupId``/``contactId``/edit ###
Edit the contact specified by ``uid``,``groupId`` and ``contactId``
### #/about ###
About screen
### #/login ###
Displays login screen