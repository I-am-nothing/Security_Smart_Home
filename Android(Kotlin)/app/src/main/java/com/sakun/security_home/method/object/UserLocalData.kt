package com.sakun.security_home.method.`object`

class UserLocalData(
    var userName: String,
    var fastLoginAcPassword: String,
    var biometricStatus: Boolean,
    var biometricAsk: Boolean,
    var appPauseTime: Long,
    var loginTimeout: Long,
    var androidId: String,
    var accountToken: String
)