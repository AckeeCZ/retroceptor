[![Build Status](https://travis-ci.org/AckeeCZ/ackroutine-adapter.svg?branch=master)](https://travis-ci.org/AckeeCZ/ackroutine-adapter) [ ![Download](https://api.bintray.com/packages/ackeecz/ackroutine-adapter/coroutine-adapter/images/download.svg) ](https://bintray.com/ackeecz/ackroutine-adapter/coroutine-adapter/_latestVersion)

# Coroutine OAuth Android Library
Simple coroutine extension, that adds support to Retrofit2 based projects which uses OAuth2 authentication.

## coroutine-oauth
### Description
- `CoorutineOAuthManager` handles access token expiration and performs token refresh. In case of success, new credentials are stored in `SharedPreferences`. When refresh token is invalid, the optional logic provided in `onRefreshTokenFailed` is performed. With custom `ErrorChecker`, the user may customize access and refresh tokens errors validation
- `OAuthInterceptor`, which is provided by `CoroutineOAuthManager` adds `Authorization` header with access token to OkHttp requests
- `DefaultOAuthCredentials` is the default implementation of `OAuthCredentials` 

### Dependency
```groovy
implementation 'cz.ackee.ackroutine:coroutine-oauth:x.x.x'
```

### Usage
Working sample is provided in `app` module.

#### Initialization
Create a `CoroutineOAuthManager` typically in API access layer (in our case, ApiInteractorImpl):
Create necessary supporting components: `CoroutineOAuthManager` and `AckroutneCallAdapterFactory` which will be provided to Retrofit to create an API Service.
Simply annotate API Service interface method with `@IgnoreAuth` to skip access token injection into request headers.
```kotlin
val oAuthManager = CoroutineOAuthManager(
        context = app,
        refreshTokenAction = { authApiDescription.refreshAccessToken(it) },
        onRefreshTokenFailed = { logouter.logout() }
    )
    
val callAdapterFactory: AckroutineCallAdapterFactory = AckroutineCallAdapterFactory(OAuthCallInterceptor(oAuthManager))
```
Use created api service in `ApiInteractor` to perform network calls.
```kotlin
class ApiInteractor(
    private val oAuthManager: CoroutineOAuthManager,
    private val apiDescription: ApiDescription
) {
    suspend fun getData(): List<SampleItem> {
        return apiDescription.getData().await()
    }
}
```

#### Storing credentials
You can save OAuth credentials with `saveCredentials(credentials: OAuthCredentials)` method. You may want to do this after receiving credentials from server, e.g. after login or sign in.
```kotlin
suspend fun login(name: String, password: String): OAuthCredentials {
    return authApiDescription.login(name, password).await().also {
        oAuthManager.saveCredentials(it)
    }
}  
```

### Logout
After logging out, you may want to remove credentials from the store.
```kotlin
suspend fun logout() {
    authDescription.logout().await().also {
        oAuthManager.clearCredentials()
    }
}
```

## coroutine-adapter
### Description
- Provides `Deferred` job wrapping for Retrofit `Call`s.
- Provides a chainable abstraction with `CallFactoryInterceptor` allowing you to define custom interceptors to alter network requests behaviour.

### Dependency
```groovy
implementation 'cz.ackee.ackroutine:coroutine-adapter:x.x.x'
```

### Usage
When creating your API service, just provide `AckroutineCallAdapterFactory` with custom defined `CallChainInterceptor`s to Retrofit builder.
```kotlin
    class MyLoggingInterceptor: CallFactoryInterceptor {
        override fun intercept(chain: CallChain): Deferred<*> {
            // Log details of chain.call.request()
            // ...
            return chain.proceed(chain.call)
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
- `CoroutineOAuthManager` wraps `Deferred` jobs and checks access token expiration before 

### Dependency
```groovy
implementation 'cz.ackee.ackroutine:coroutine-oauth:x.x.x'
```
