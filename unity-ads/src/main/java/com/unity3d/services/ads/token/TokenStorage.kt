package com.unity3d.services.ads.token

import org.json.JSONArray
import org.json.JSONException

interface TokenStorage {
    val token: String?
    val nativeGeneratedToken: Unit

    @Throws(JSONException::class)
    fun createTokens(tokens: JSONArray)
    @Throws(JSONException::class)
    fun appendTokens(tokens: JSONArray)
    fun deleteTokens()
    fun setInitToken(value: String?)
}
