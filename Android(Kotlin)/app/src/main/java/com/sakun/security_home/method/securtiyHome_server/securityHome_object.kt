package com.sakun.security_home.method.securtiyHome_server

enum class SecurityHomeStatus(val value: Int){
    Failed(0),
    Update(2),
    Success(1)
}

enum class SecurityHomeLoginStatus(val value: Int){
    NoAccount(0),
    PasswordNotCorrect(2),
    Success(1)
}

enum class SecurityHomeDeviceLoginStatus(val value: Int){
    NewDevice(0),
    Success(1)
}

enum class SecurityHomeDeviceDetail(val value: Int){
    Fan(103),
    Light(104),
    Switch(105),
    Socket(106),
    Door(107)
}

enum class SecurityHomeNewFastLoginStep(val value: String){
    One("Please add your new fast login password"),
    Two("Please enter again your new fast login password")
}

enum class SecurityHomeChangeFastLoginStep(val value: String){
    One("Please enter your old fast login password"),
    Two("Please enter your new fast login password"),
    Three("Please enter again your new fast login password")
}

enum class Internet(val value: Int){
    NoInternet(-1)
}

class securityHome_object {
}