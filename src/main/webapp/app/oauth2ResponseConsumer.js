function getParamsFromFragment() {
    var params = {};
    var postBody = location.hash.substring(1);
    var regex = /([^&=]+)=([^&]*)/g, m;

    while (m = regex.exec(postBody)) {
        params[decodeURIComponent(m[1])] = decodeURIComponent(m[2]);
    }

    return params;
}

function parseIdToken(idToken) {
    
    idToken = idToken.split(/\./);

    var header = JSON.parse(atob(idToken[0]));
    var payload = JSON.parse(atob(idToken[1]));
    var signature = idToken[2];
    
    var parsedIdToken = {
        header : header,
        payload : payload,
        signature : signature
    };
    
    return parsedIdToken;
}

$(document).ready(function () {
    var params = getParamsFromFragment();
    var idToken = params["id_token"];
    var parsedIdToken = parseIdToken(idToken);
    
    console.info("params: " + JSON.stringify(params));
    
    //oauth2RequestParameters is saved in oauth2.js
    console.info("localStorage.oauth2RequestParameters: " + localStorage.oauth2RequestParameters);
    console.info("parsedIdToken: " + JSON.stringify(parsedIdToken));
    if (localStorage.oauth2RequestParameters !== undefined) {
        var oauth2RequestParameters = JSON.parse(localStorage.oauth2RequestParameters);
        if (params.access_token !== undefined
                && parsedIdToken.payload.nonce === oauth2RequestParameters.nonce
                && params.state === oauth2RequestParameters.state) {
            console.info("pathname: " + window.location.pathname);
            var path = window.location.pathname;
            var newpath = path.replace('oauth2ResponseConsumer.html', '');
            console.info("new path: " + newpath);
            //TODO Ch5L1Ex3: Complete the redirection path with the access_token extracted above.
            //TODO Ch5L1Ex3: Hint: Use encodeURIComponent to URLEncode params.access_token
            window.location.replace(newpath + "#/login?tokenId=" + encodeURIComponent(params.access_token));
            window.location.replace(newpath + "#/login?tokenId=");
        }
    }

});