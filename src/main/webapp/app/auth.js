angular
        .module('services.auth',
//TODO Ch4L1Ex3: modify dependency to services.auth.openam-min
//TODO Ch4L2Ex1: modify dependency to services.auth.openam
//TODO Ch5L1Ex3: modify dependency to services.auth.oauth2
//TODO Ch6L1Ex2: modify dependency to services.auth.uma
                ['services.auth.internal']); //selects the implementation of loginService


