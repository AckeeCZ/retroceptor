[![Build Status](https://travis-ci.org/AckeeCZ/ackroutine-adapter.svg?branch=master)](https://travis-ci.org/AckeeCZ/ackroutine-adapter) [ ![Download](https://api.bintray.com/packages/ackeecz/ackroutine-adapter/coroutine-adapter/images/download.svg) ](https://bintray.com/ackeecz/ackroutine-adapter/coroutine-adapter/_latestVersion)

# Coroutine OAuth Android Library
Simple coroutine extension, which makes use of Retrofit2 internal `Call<T>` to suspending function conversion to add support for OAuth and custom request modifiers.

## coroutine-adapter
### Description
- Provides a chainable abstraction with `CallFactoryInterceptor` allowing you to define custom interceptors to alter network requests behaviour.

### Dependency
```groovy
implementation 'cz.ackee.ackroutine:coroutine-adapter:x.x.x'
```

### Usage
When creating your API service, just provide `AckroutineCallAdapterFactory` with custom defined `CallChainInterceptor`s to Retrofit builder.
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
        .addCallAdapterFactory(AckroutineCallAdapterFactory(MyLoggingInterceptor()))
        .build()
        .create(ApiDescription::class.java)
```
:tada:

## coroutine-oauth
### Description
- `CoorutineOAuthManager` handles access token expiration and performs token refresh. In case of success, new credentials are stored in `SharedPreferences`. When refresh token is invalid, the optional logic provided in `onRefreshTokenFailed` is performed. With custom `ErrorChecker`, the user may customize access and refresh tokens errors validation
- `OAuthInterceptor`, which makes use of `OAuthManager` and adds `Authorization` header with access token to OkHttp requests
- `DefaultOAuthCredentials` is the default implementation of `OAuthCredentials` 

### Dependency
```groovy
implementation 'cz.ackee.ackroutine:coroutine-oauth:x.x.x'
```

### Usage
Working sample is provided in `app` module.

#### Initialization
Create a `OAuthManager` typically in API access layer (in our case, ApiInteractor):
Create necessary supporting components: `OAuthManager` and `AckroutneCallAdapterFactory` which will be provided to Retrofit to create an API Service.
Simply annotate API Service interface method with `@IgnoreAuth` to skip access token injection into request headers.
```kotlin
val oAuthManager = CoroutineOAuthManager(
        context = app,
        refreshTokenAction = { authApiDescription.refreshAccessToken(it) },
        onRefreshTokenFailed = { logouter.logout() }
    )
    
val callAdapterFactory: AckroutineCallAdapterFactory = AckroutineCallAdapterFactory(OAuthCallInterceptor(oAuthManager))
```
Use created API Service in `ApiInteractor` to perform network calls.
```kotlin
class ApiInteractor(
    private val api: ApiDescription,
    private val oAuthManager: CoroutineOAuthManager
) {
    suspend fun fetchData(): List<SampleItem> {
        return api.fetchData()
    }
}

interface ApiDescription {

    @GET("your-resource-url")
    suspend fun fetchData(): List<SampleItem>
}
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

### Logout
After logging out, you may want to remove credentials from the store.
```kotlin
suspend fun logout() {
    authDescription.logout().also {
        oAuthManager.clearCredentials()
    }
}
```