angular
    .module('services.selfService', ['services.auth'])
    .factory('selfService', function ($http, openamConstants, $q) {

        var selfService = {};

        var currentToken = null;

        var currentCode = null;
        /**
         * Initiates the self service process. When the REST call returned,
         * the received token is saved in the currentToken variable.
         * @param {string} processType Can be "forgottenPassword", "forgottenUsername" or "userRegistration"
         * @returns {promise} Returns a promise which is satisfied when the REST call returns.
         */
        selfService.initiate = function (processType) {

            //DONE Ch4L3Ex3: Complete the self service initiation request. Send a GET request to <serverURI>/json/selfservice/<processType>.
            var req = {
                method: 'GET',
                url: openamConstants.serverURI + '/json/selfservice/' + processType,
                headers: {
                    'Accept': 'application/json',
                    'Accept-API-Version': 'resource=1.0'
                }
            };

            currentToken = null;

            // returning with the promise
            return $http(req).then(function (response) {
                //saving received token in the currentToken variable
                currentToken = response.data.token;
                return response.data;
            });

        };
        /**
         * Submits the requirements back in to the selfservice endpoint.
         * The caller of this function should just provide the input
         * structure. The last received token (named currentToken)
         * is automatically added by this function to the submitted data.
         * This function also saves the received token
         * into the currentToken variable, which will be used at the next
         * function call.
         * @param {string} processType Can be "forgottenPassword", "forgottenUsername" or "userRegistration"
         * @param {object} input an object with the input structure to submit.
         * @returns {promise} Returns a promise which is satisfied when the REST call returns.
         */
        selfService.submitRequirements = function (processType, input) {
            //DONE Ch4L3Ex3: Complete the submitRequirements request. Send a POST request to <serverURI>/json/selfservice/<processType>?_action=submitRequirements.
            var req = {
                method: 'POST',
                url: openamConstants.serverURI + '/json/selfservice/' + processType,
                params: {
                    _action: 'submitRequirements'
                },
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Accept-API-Version': 'resource=1.0'
                },
                data: {
                    //DONE Ch4L3Ex3: Send the input structure in the body as "input". (Hint: Type input: input)
                    input: input
                }

            };

            // If there is a saved token from the last method call
            // or if it was explicitly set, we are placing it into the
            // payload as "token".
            if (currentToken) {
                req.data.token = currentToken;
            }

            if (currentCode) {
                req.data.code = currentCode;
            }
            // returning with the promise
            return $http(req).then(function (response) {
                //saving received token in the currentToken variable
                currentToken = response.data.token;
                if (response.data.requirements && response.data.requirements.code) {
                    currentCode = response.data.requirements.code;
                }
                return response.data;
            });
        };

        /**
         * Submit the auth code and the provided token by calling
         * selfService.submitRequirements. This method is important
         * because it is called when we are jumping into the middle of the
         * process, because there was an email sent out with this code,
         * which contains a link that leads to the ContactList application.
         * This link contains all the data that is needed to call this
         * function so we can continue the process from that point.
         * @param {string} processType Can be "forgottenPassword", "forgottenUsername" or "userRegistration"
         * @param {string} code the code which was in the email
         * @param {string} token the token which was in the email
         * @returns {promise} Returns a promise which is satisfied when the REST call returns.
         */
        selfService.validateCode = function (processType, code, token) {
            //replacing the currentToken with the passed token
            currentToken = token;
            return selfService.submitRequirements(processType, {code: code});
        };

        return selfService;
    });
