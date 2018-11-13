package com.archmage.ghostmind

import com.archmage.ghostmind.model.UrbanDeadModel

object Sandbox extends App {
    var contactsListResponse = UrbanDeadModel.loginRequest("username", "password")
//    var skills = UrbanDeadModel.request(UrbanDeadModel.baseUrl + "/skills.cgi")
    var map = UrbanDeadModel.request(UrbanDeadModel.baseUrl + "/map.cgi")
    val parsedMap = UrbanDeadModel.parseMap(map.get)
    println(parsedMap)
//    var contactsList = UrbanDeadModel.parseContactList(contactsListResponse.body)
//    println(contactsList)
}
