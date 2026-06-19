package com.example.ui

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object RatingManager {
    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    Log.d("RatingManager", "Review flow completed")
                }
            } else {
                Log.e("RatingManager", "There was some problem, log or handle the error code.")
            }
        }
    }
}
