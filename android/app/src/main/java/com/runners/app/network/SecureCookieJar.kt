package com.runners.app.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

class SecureCookieJar(context: Context) : CookieJar {
    private val lock = Any()

    private val prefs = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private var cache: MutableList<Cookie> = loadPersistedCookies()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        synchronized(lock) {
            for (cookie in cookies) {
                cache.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
                if (cookie.expiresAt <= now) continue
                cache.add(cookie)
            }
            persist()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        synchronized(lock) {
            val beforeSize = cache.size
            cache = cache.filterTo(mutableListOf()) { it.expiresAt > now }
            if (cache.size != beforeSize) persist()
            return cache.filter { it.matches(url) }
        }
    }

    fun clear() {
        synchronized(lock) {
            cache.clear()
            prefs.edit().remove(KEY_COOKIES_JSON).apply()
        }
    }

    private fun persist() {
        val now = System.currentTimeMillis()
        val persistentCookies = cache
            .asSequence()
            .filter { it.persistent && it.expiresAt > now }
            .map { cookieToJson(it) }
            .toList()

        val array = JSONArray()
        persistentCookies.forEach { array.put(it) }
        prefs.edit().putString(KEY_COOKIES_JSON, array.toString()).apply()
    }

    private fun loadPersistedCookies(): MutableList<Cookie> {
        val now = System.currentTimeMillis()
        val raw = prefs.getString(KEY_COOKIES_JSON, null).orEmpty()
        if (raw.isBlank()) return mutableListOf()

        val result = mutableListOf<Cookie>()
        runCatching {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val cookie = jsonToCookie(array.getJSONObject(i)) ?: continue
                if (cookie.expiresAt <= now) continue
                result.add(cookie)
            }
        }
        return result
    }

    private fun cookieToJson(cookie: Cookie): JSONObject =
        JSONObject()
            .put("name", cookie.name)
            .put("value", cookie.value)
            .put("expiresAt", cookie.expiresAt)
            .put("domain", cookie.domain)
            .put("path", cookie.path)
            .put("secure", cookie.secure)
            .put("httpOnly", cookie.httpOnly)
            .put("hostOnly", cookie.hostOnly)

    private fun jsonToCookie(json: JSONObject): Cookie? {
        val name = json.optString("name").takeIf { it.isNotBlank() } ?: return null
        val value = json.optString("value")
        val expiresAt = json.optLong("expiresAt", -1L)
        val domain = json.optString("domain").takeIf { it.isNotBlank() } ?: return null
        val path = json.optString("path").takeIf { it.isNotBlank() } ?: "/"
        val secure = json.optBoolean("secure", false)
        val httpOnly = json.optBoolean("httpOnly", false)
        val hostOnly = json.optBoolean("hostOnly", false)

        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .path(path)
            .expiresAt(expiresAt)

        if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()

        return runCatching { builder.build() }.getOrNull()
    }

    private companion object {
        private const val PREFS_NAME = "runners_secure_cookies"
        private const val KEY_COOKIES_JSON = "cookies_json"
    }
}
