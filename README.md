# Retroceptor Android Library
Simple library that allows you to define custom network interceptors on Retrofit layer with support for returning `Deferred` types, so you can use Kotlin `suspend` functions for making your requests.

## retroceptor-core
[ ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/retroceptor-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/retroceptor-core)

### Description
- Provides a chainable abstraction with `CallFactoryInterceptor` allowing you to define custom interceptors to alter network requests behaviour.

### Dependency
```groovy
implementation 'io.github.ackeecz:retroceptor-core:x.x.x'
```

### Usage
When creating your API service, just provide `RetroceptorCallAdapterFactory` with custom defined `CallChainInterceptor`s to Retrofit builder.

**Caution!** Do not add multiple `RetroceptorCallAdapterFactory` instances, because only the first one will be used due to Retrofit implementation.
When you want to provide multiple interceptors, just pass all of them to single `RetroceptorCallAdapterFactory` as illustrated in the example below:
```kotlin
    class MyLoggingInterceptor : CallFactoryInterceptor {

        override fun intercept(chain: CallChain): Call<*> {
            return chain.proceed(LoggingCall(chain.call))
        }
    }

    class LoggingCall<T>(private val call: Call<T>) : CallDelegate<T, T>(call) {
        override fun cloneImpl(): Call<T> = LoggingCall(call.clone())

        override fun enqueueImpl(callback: Callback<T>) {
            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    // check if error is something you want to log and proceed
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    callback.onResponse(call, response)
                }
            })
        }
    }


    val apiDescription = retrofitBuilder
        .addCallAdapterFactory(
            RetroceptorCallAdapterFactory(
                MyLoggingInterceptor(),
                AnotherCustomInterceptor()
            )
        )
        .build()
        .create(ApiDescription::class.java)
```
:tada:

## retroceptor-auth
[ ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/retroceptor-auth/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/retroceptor-auth)
### Description
This library provides easy mechanism for authentication handling, which consists of two basic parts:
1. Adding authentication metadata such as HTTP headers to all Retrofit requests
2. Automatic refresh of expired auth credentials such as access tokens

It includes support for OAuth2 flow including refresh token handling. However, you can write custom implementation if you use different type of authentication flow.

### Dependency
```groovy
implementation 'io.github.ackeecz:retroceptor-auth:x.x.x'
```

### Usage - OAuth2
Working sample with OAuth2 flow is provided in the `app` module.

#### Initialization
- Create instance of `OAuthManager`
- Add `RetroceptorCallAdapterFactory` with `OAuthRefreshCallInterceptor` to Retrofit call adapter factories
- Add OkHttp interceptor provided by `oAuthManager.provideAuthInterceptor()` function to inject `Authorization` HTTP header to your requests
- (optional) Annotate API Service interface method with `@IgnoreAuth` to skip access token injection into request headers for requests that do not require authentication (such as login endpoints).
```kotlin
val oAuthManager = OAuthManager(
        context = app,
        refreshTokenAction = { authApiDescription.refreshAccessToken(it) },
        onRefreshTokenFailed = { logouter.logout() }
    )

val authHeaderInterceptor = oAuthManager.provideAuthInterceptor()
val callAdapterFactory: RetroceptorCallAdapterFactory = RetroceptorCallAdapterFactory(OAuthCallInterceptor(oAuthManager))

val okHttpClient = OkHttpClient.Builder()
    // ...
    .addNetworkInterceptor(authHeaderInterceptor)
    .build()
val retrofit = Retrofit.Builder()
    // ...
    .client(okHttpClient)
    .addCallAdapterFactory(callAdapterFactory)
    .build()
```

#### Storing credentials
You can save OAuth credentials with `saveCredentials(credentials: OAuthCredentials)` method. You may want to do this after receiving credentials from server, e.g. after login or sign in.
```kotlin
suspend fun login(username: String, password: String): User {
    return with(api.login(ApiUsernameLoginRequest(username, password))) {
        oAuthManager.saveCredentials(credentials)
    }
}
```

#### Clearing credentials
After logging out, you may want to remove credentials from the store.
```kotlin
suspend fun logout() {
    authDescription.logout().also {
        oAuthManager.clearCredentials()
    }
}
```

#### Error handling
By default, access token is considered as expired when server returns *HTTP 401 Unauthorized* response. Refresh token is considered as expired when server returns *HTTP 400 Bad Request* or *HTTP 401 Unauthorized* responses. You can modify this behavior with providing custom `AuthErrorChecker` to `OAuthManager`.
```kotlin
// Equivalent to invalid access token in OAuth2 flow - called when network request failed
override fun invalidCredentials(t: Throwable): Boolean {
    if (t is HttpException) {
        if (t.code() == HTTP_UNAUTHORIZED) {
            return true // access token invalid
        }
    }
    return false // access token valid
}

// Equivalent to invalid refresh toke in OAuth2 flow - called when request for credentials refresh failed
override fun invalidRefreshCredentials(t: Throwable): Boolean {
    if (t is HttpException) {
        when (t.code()) {
            HTTP_BAD_REQUEST, HTTP_UNAUTHORIZED -> true // refresh token invalid
            else -> false // refresh token valid
        }
    }
    return false // refresh token valid
}
```

Please note that when you use custom `CallFactoryInterceptor` that maps exceptions on network layer, it should be added *before* your `AuthInterceptor`. In this case you need to provide custom `AuthErrorChecker` which will receive mapped exceptions.

### Usage - custom authentication flow
When using different auth flow than OAuth2, you have to write custom `AuthManager` implementation. Otherwise usage is exactly the same as when using provided `OAuthManager` .

But don't worry, it's easier than it sounds! These are necessary steps for the implementation:

1. Create custom `AuthCredentials`
2. Implement `AuthStore` for storing your credentials
3. Create OkHttp `Interceptor` for injecting authorization metadata to network requests
4. Create your custom `AuthManager<C : AuthCredentials>`

#### AuthCredentials
Simple interface for auth credentials model entity. If your credentials contains information about time of expiration, you should override `areExpired()` function.

Example:
```kotlin
data class OAuthCredentials(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long? = null
) : AuthCredentials {

    override fun areExpired(): Boolean {
        return expiresIn?.let { expiration ->
            System.currentTimeMillis() >= expiration
        } ?: false
    }
}
```

#### AuthStore
Library doesn't know how to serialize and properly store your newly created `AuthCredentials`, that's why you have to create custom `AuthStore`. You can use `SharedPreferences`, local database or any other persistency solution that you prefer.

Simplified example:
```kotlin
class OAuthStore : AuthStore<OAuthCredentials> {

    private val sp: SharedPreferences

    override val authCredentials: OAuthCredentials?
        get() = sp.getString("credentials", null)?.deserialize()

    override fun saveCredentials(credentials: OAuthCredentials) {
        sp.edit {
            putString("credentials", credentials.serialize())
        }
    }

    override fun clearCredentials() {
        sp.edit().clear().apply()
    }
```

#### OkHttp Authentication Interceptor
Custom interceptor that adds authentication metadata to your requests.

Example:
```kotlin
class OAuthHeaderInterceptor (private val authStore: AuthStore<OAuthCredentials>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val accessToken = authStore.authCredentials?.accessToken?.takeIf { it.isNotBlank() }
        accessToken?.let { token ->
            builder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
```

#### AuthManager
Wraps all concepts mentioned above to single class that you'll interact with. All you need to do is initialize base `AuthManager` with all values and return your OkHttp interceptor from `provideAuthInterceptor()` function.

Simplified example:
```kotlin
class OAuthManager internal constructor(
    oAuthStore: OAuthStore,
    refreshCredentialsAction: suspend (OAuthCredentials?) -> OAuthCredentials,
    onRefreshCredentialsFailed: (Throwable) -> Unit = {},
    errorChecker: AuthErrorChecker = DefaultAuthErrorChecker()
) : AuthManager<OAuthCredentials>(
    authStore = oAuthStore,
    refreshCredentialsAction = refreshCredentialsAction,
    onRefreshCredentialsFailed = onRefreshCredentialsFailed,
    errorChecker = errorChecker
) {

    override fun provideAuthInterceptor() = OAuthHeaderInterceptor(authStore)
}
```
