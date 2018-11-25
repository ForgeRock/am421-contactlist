console.info("url router **************************");
console.info("pathname: " +window.location.pathname);
var path = window.location.pathname;
var newpath = path.replace('confirm.html','');
console.info("new path: " + newpath);
var hash = window.location.hash;
var questionMark = hash.indexOf('&');
if (questionMark !== -1) {
    var prefix = hash.substring(0, questionMark);
    var allparams = hash.substr(questionMark + 1);
    console.info("allparams: " + allparams);
    var keyvaluepairs = allparams.split("&");
    console.info("keyvalues: " + keyvaluepairs);
    var newvalues = prefix + "?";
    var notFirst = false;
    $.each(keyvaluepairs, function (index,keyvaluepair) {
        var eqIndex = keyvaluepair.indexOf('=');
        var key = keyvaluepair.substring(0, eqIndex);
        var value = keyvaluepair.substr(eqIndex + 1);
        console.info("key: " + key + " , value: " + value);
        value = encodeURIComponent(value);
        if (notFirst) {
            newvalues += "&";
        } else {
            notFirst = true;
        }
        newvalues += key + "=" + value;
    });
    console.info("newvalues: " + newpath + newvalues);
    window.location.replace(newpath + newvalues);
}