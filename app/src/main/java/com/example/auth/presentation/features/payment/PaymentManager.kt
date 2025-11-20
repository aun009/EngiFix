package com.example.auth.presentation.features.payment

import android.app.Activity
import android.util.Log
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

/**
 * PaymentManager handles Razorpay payment integration
 * 
 * IMPORTANT: Replace "YOUR_RAZORPAY_KEY_ID" with your actual Razorpay Key ID
 * You can get this from: https://dashboard.razorpay.com/app/keys
 */
class PaymentManager(
    private val activity: Activity,
    private val razorpayKeyId: String = "Ra0lrZSfzfYokS" // Replace with your actual key
) : PaymentResultWithDataListener {

    companion object {
        private const val TAG = "PaymentManager"
    }

    /**
     * Start payment process for a mentorship session
     * @param amount Amount in rupees (e.g., 1399 for ₹1,399)
     * @param mentorName Name of the mentor
     * @param mentorId ID of the mentor
     * @param userName Name of the user making the payment
     * @param userEmail Email of the user
     * @param userContact Contact number of the user
     */
    fun startPayment(
        amount: Int,
        mentorName: String,
        mentorId: String,
        userName: String = "User",
        userEmail: String = "user@example.com",
        userContact: String = "+919999999999"
    ) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(razorpayKeyId)

            val options = JSONObject()
            
            // Payment details
            options.put("name", "Mentorship Session")
            options.put("description", "1:1 Mentorship with $mentorName")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#4CAF50")
            
            // Amount in paise (multiply by 100)
            options.put("currency", "INR")
            options.put("amount", amount * 100) // Convert rupees to paise
            
            // Prefill user details
            val prefill = JSONObject()
            prefill.put("email", userEmail)
            prefill.put("contact", userContact)
            prefill.put("name", userName)
            options.put("prefill", prefill)
            
            // Additional notes
            val notes = JSONObject()
            notes.put("mentor_id", mentorId)
            notes.put("mentor_name", mentorName)
            notes.put("session_type", "1:1 Mentorship")
            options.put("notes", notes)

            checkout.open(activity, options)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e)
            onPaymentError(
                PaymentError.UNKNOWN_ERROR.code,
                "Payment initialization failed: ${e.message}",
                null
            )
        }
    }

    /**
     * Handle successful payment
     */
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Log.d(TAG, "Payment successful: $razorpayPaymentId")
        
        paymentData?.let {
            val paymentId = it.paymentId
            val orderId = it.orderId
            val signature = it.signature
            
            Log.d(TAG, "Payment ID: $paymentId")
            Log.d(TAG, "Order ID: $orderId")
            Log.d(TAG, "Signature: $signature")
            
            // Here you can:
            // 1. Save payment details to your backend
            // 2. Update user's mentorship access
            // 3. Send confirmation email/notification
        }
        
        // Notify listener if set
        paymentListener?.onPaymentSuccess(razorpayPaymentId, paymentData)
    }

    /**
     * Handle payment failure
     */
    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        Log.e(TAG, "Payment failed: Code=$code, Description=$description")
        
        val errorMessage = when (code) {
            PaymentError.NETWORK_ERROR.code -> "Network error. Please check your internet connection."
            PaymentError.INVALID_OPTIONS.code -> "Invalid payment options."
            PaymentError.PAYMENT_CANCELLED.code -> "Payment was cancelled."
            PaymentError.TLS_ERROR.code -> "Security error. Please try again."
            else -> description ?: "Payment failed. Please try again."
        }
        
        // Notify listener if set
        paymentListener?.onPaymentError(code, errorMessage, paymentData)
    }

    // Listener for payment callbacks
    private var paymentListener: PaymentListener? = null

    fun setPaymentListener(listener: PaymentListener) {
        this.paymentListener = listener
    }

    /**
     * Interface for payment callbacks
     */
    interface PaymentListener {
        fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?)
        fun onPaymentError(code: Int, errorMessage: String, paymentData: PaymentData?)
    }

    /**
     * Payment error codes
     */
    enum class PaymentError(val code: Int) {
        NETWORK_ERROR(0),
        INVALID_OPTIONS(1),
        PAYMENT_CANCELLED(2),
        TLS_ERROR(3),
        UNKNOWN_ERROR(-1)
    }
}

