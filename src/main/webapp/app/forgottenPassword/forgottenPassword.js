angular.module('contactList.forgottenPassword',
        ['ui.router', 'services.selfService'])
        .config(
                function ($stateProvider) {

                    $stateProvider
                            .state('forgottenPassword', {
                                url: "/forgottenPassword?realm&token&code",
                                templateUrl: "app/forgottenPassword/forgottenPassword.html",
                                noAuth: true,
                                controller: function ($scope, selfService, $stateParams) {
                                    $scope.input = {};
                                    console.info("Password reset: " + window.location.hash);
                                    if (!$stateParams.token) {
                                        // No token parameter in the url -> initiating a new forgotten password flow.
                                        selfService.initiate("forgottenPassword")
                                                .then(function (response) {
                                                    $scope.status = response;
                                                });
                                    } else {
                                        // Token is received as part of the url -> interpreting this as a code validation phase.
                                        //DONE Ch4L3Ex3: call selfService.validateCode and pass "forgottenPassword" as the process type, $stateParams.code as code and $stateParams.token as the token value.
                                        //DONE Ch4L3Ex3: save the response into $scope.status (just as above with selfService.initiate)
                                        selfService.validateCode("forgottenPassword", $stateParams.code, $stateParams.token)
                                                .then(function (response) {
                                                    $scope.status = response;
                                                    console.info("confirm returned with: " + JSON.stringify(response));
                                                });                                        
                                    }
                                    $scope.message = '';
                                    $scope.error = true;
                                    $scope.proceed = function () {
                                        console.info("Proceed invoked");
                                        var requiredFields = {};
                                        angular.forEach($scope.status.requirements.required, function (fieldName) {
                                            requiredFields[fieldName] = true;
                                        });
                                        if (requiredFields.password) {
                                            if (!$scope.input.password) {
                                                $scope.message = 'Password should not be empty';
                                                $scope.error = true;
                                                return;
                                            }
                                            if ($scope.input.password.length < 8) {
                                                $scope.message = 'Password should be minimum 8 characters long';
                                                $scope.error = true;
                                                return;
                                            }
                                            if ($scope.input.password !== $scope.input.password2) {
                                                $scope.message = 'Two passwords are not identical';
                                                $scope.error = true;
                                                return;
                                            }
                                        }

                                        var promise = selfService.submitRequirements("forgottenPassword", $scope.input)
                                                .then(function (response) {
                                                    //Saving the response into the $scope.status
                                                    $scope.status = response;
                                                    if (response.status && response.status.success) {
                                                        //Self service process succeeded.
                                                        $scope.message = 'Password successfully changed';
                                                        $scope.error = false;
                                                    } else {
                                                        //The requirement successfully sent in but there is a new requirement.
                                                        $scope.message = "Success";
                                                        $scope.error = false;
                                                    }
                                                }, function (response) {
                                                    //Handling error response
                                                    $scope.message = response.data.message + " (code:" + response.data.code + ")";
                                                    $scope.error = true;
                                                });
                                        return promise;
                                    };
                                }
                            });
                });
