package com.github.forrestdp.bankingapp.fragment.home

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.forrestdp.bankingapp.R
import com.github.forrestdp.bankingapp.repo.model.currencyinfo.CurrencyCode
import java.math.BigDecimal
import java.math.RoundingMode

@BindingAdapter("transactionAmountInCurrencyFormatted", "transactionCurrencyBadgeFormatted", "transactionCurrencyMultiplier")
fun TextView.setTransactionAmountFormatted(amount: String?, currencyCode: CurrencyCode?, multiplier: BigDecimal?) {
    if (amount != null && currencyCode != null && multiplier != null) {
        val char = when (currencyCode) {
            CurrencyCode.USD -> "$"
            CurrencyCode.GBP -> "£"
            CurrencyCode.EUR -> "€"
            CurrencyCode.RUB -> "₽"
        }
        text =
            this.context.getString(
                R.string.history_transaction_amount_in_currency_string,
                char,
                (amount.toBigDecimal() * multiplier).setScale(2, RoundingMode.HALF_UP).toString(),
            )
    }
}

@BindingAdapter("transactionAmountFormatted")
fun TextView.setTransactionAmountInCurrencyFormatted(amount: String?) {
    if (amount != null) {
        text = this.context.getString(R.string.history_transaction_amount_string, "$", amount)
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindImage(imageUrl: String?) {
    if (imageUrl != null) {
        val imageUri = imageUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(context)
            .load(imageUri)
            .into(this)
    }
}

@BindingAdapter("cardTypeImage")
fun ImageView.setCardTypeImage(cardType: String?) {
    if (cardType != null) {
        setImageResource(when (cardType) {
            "mastercard" -> R.drawable.ic_mastercard_icon
            "visa" -> R.drawable.ic_visa_icon
            "unionpay" -> R.drawable.ic_unionpay_icom
            else -> R.drawable.ic_blue_circle
        })
    }
}

@BindingAdapter("loadingStatus")
fun FrameLayout.setLoadingStatus(loadingStatus: LoadingStatus?) {
    when (loadingStatus) {
        LoadingStatus.LOADING -> visibility = View.GONE
        LoadingStatus.ERROR -> visibility = View.VISIBLE
        LoadingStatus.DONE -> visibility = View.GONE
    }
}

@BindingAdapter("shimmerLoadingStatus")
fun ShimmerFrameLayout.setLoadingStatus(loadingStatus: LoadingStatus?) {
    when (loadingStatus) {
        LoadingStatus.LOADING -> {
            visibility = View.VISIBLE
            startShimmer()
        }
        LoadingStatus.ERROR -> {
            visibility = View.GONE
            stopShimmer()
        }
        LoadingStatus.DONE -> {
            visibility = View.GONE
            stopShimmer()
        }
    }
}