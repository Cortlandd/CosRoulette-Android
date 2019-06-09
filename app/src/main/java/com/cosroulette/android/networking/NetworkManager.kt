package com.cosroulette.android.networking

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result

object NetworkManager {

    const val BASE_URL = "https://cosroulette.herokuapp.com"

    /**
     *
     * Method used to submit content
     *
     * @param videoId: The videoId of the content submitted.
     * @param category: The category of the associated submitted content.
     *
     */
    fun submissionApproval(videoId: String, category: String): Int {

        val statusCode: Int

        val apiUrl = "$BASE_URL/api/v1/submission_approval/$videoId/$category"

        val (request, response, result) = Fuel.post(apiUrl).response()

        when (result) {

            is Result.Success -> {
                statusCode = response.statusCode
            }

            is Result.Failure -> {
                statusCode = response.statusCode
            }

        }

        return statusCode

    }

}