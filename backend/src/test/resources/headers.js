function fn(auth) {
    var token = auth.token;
    var username = auth.username;
    var password = auth.password;

    if (token) {
        return {
            Authorization: 'Bearer ' + token
        };
    } else if (username && password) {
        var credentials = username + ':' + password;
        var Base64 = Java.type('java.util.Base64');
        var encoded = Base64.getEncoder().encodeToString(credentials.bytes);
        return {
            Authorization: 'Basic ' + encoded
        };
    } else {
        return {};
    }
}
