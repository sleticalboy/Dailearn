package com.sleticalboy.learning.accounts

data class Contact(
  var firstName: String? = null,
  var lastName: String? = null,
  var fullName: String? = null,
  var cellPhone: String? = null,
  var homePhone: String? = null,
  var workPhone: String? = null,
  var email: String? = null
)