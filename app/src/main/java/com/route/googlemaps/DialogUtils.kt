package com.route.googlemaps

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun showDialog(
    context: Context,
    msg: String? = null,
    positiveMsg: String? = null,
    negativeMsg: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    onNegativeClick: (() -> Unit)? = null
) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.setMessage(msg)
    alertDialog.setPositiveButton(positiveMsg) { dialog, _ ->

        onPositiveClick?.invoke()
        dialog?.dismiss()
    }
    alertDialog.setNegativeButton(negativeMsg) { dialog, _ ->

        onNegativeClick?.invoke()
        dialog?.dismiss()
    }

    alertDialog.show()
}