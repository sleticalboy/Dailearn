package com.sleticalboy.learning.others

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ImageConvertActivityBinding
import com.sleticalboy.util.ImageUtils

/**
 * Created on 18-9-26.
 *
 * @author leebin
 */
class ImageConvertUI : BaseActivity() {

    private var imageStr: String? = null
    private var resultBitmap: Bitmap? = null
    private var mBind: ImageConvertActivityBinding? = null

    override fun layout(): View {
        // R.layout.image_convert_activity
        mBind = ImageConvertActivityBinding.inflate(layoutInflater)
        return mBind!!.root
    }

    override fun initView() {
    }

    override fun initData() {
        imageStr = ImageUtils.bitmap2StrByBase64(
            BitmapFactory.decodeResource(resources, R.drawable.btn_shutter_default)
        )
        Log.d("ImageConvertUI", "$imageStr")
        resultBitmap = ImageUtils.base64ToBitmap(imageStr!!)
    }

    fun imageToString(view: View) {
        mBind!!.tvImageStr.text = imageStr
    }

    fun stringToImage(view: View) {
        mBind!!.imageView.setImageBitmap(resultBitmap)
    }
}
