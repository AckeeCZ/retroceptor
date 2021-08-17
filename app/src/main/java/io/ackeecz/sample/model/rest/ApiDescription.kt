package io.ackeecz.sample.model.rest

import io.ackeecz.sample.model.SampleItem
import retrofit2.http.GET

/**
 * Api description simulating Retrofit description
 */
interface ApiDescription {

    @GET("items")
    suspend fun getData(): List<SampleItem>
}
