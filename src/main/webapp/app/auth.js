angular
        .module('services.auth',
//DONE Ch4L1Ex3: modify dependency to services.auth.openam-min
//DONE Ch4L2Ex1: modify dependency to services.auth.openam
//DONE Ch5L1Ex3: modify dependency to services.auth.oauth2
//TODO Ch6L1Ex2: modify dependency to services.auth.uma
                ['services.auth.openam']); //selects the implementation of loginService


