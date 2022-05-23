package com.templatemela.camscanner.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdView
import com.google.api.services.vision.v1.Vision
import com.templatemela.camscanner.R
import com.templatemela.camscanner.main_utils.Constant
import com.templatemela.camscanner.utils.AdsUtils
import com.templatemela.camscanner.utils.CloudOcr
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.i18n.TextBundle


class ImageToTextActivity : BaseActivity(), View.OnClickListener {
    protected var iv_back: ImageView? = null
    protected var iv_copy_txt: ImageView? = null
    private var iv_preview_img: ImageView? = null
    protected var iv_rescan_img: ImageView? = null
    protected var iv_share_txt: ImageView? = null
    var progressDialog: ProgressDialog? = null
    var tv_ocr_txt: TextView? = null
    private var tv_title: TextView? = null
    private var adView: AdView? = null
    private var vision: Vision? = null
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_img_to_text)
        init()
        initVision()
        bindView()

    }

    private fun initVision() {
        vision = CloudOcr.getVisionBuider()?.build()
    }

    private fun init() {
        iv_back = findViewById<View>(R.id.iv_back) as ImageView
        iv_rescan_img = findViewById<View>(R.id.iv_rescan_img) as ImageView
        iv_preview_img = findViewById<View>(R.id.iv_preview_img) as ImageView
        tv_title = findViewById<View>(R.id.tv_title) as TextView
        iv_share_txt = findViewById<View>(R.id.iv_share_txt) as ImageView
        iv_copy_txt = findViewById<View>(R.id.iv_copy_txt) as ImageView
        tv_ocr_txt = findViewById<View>(R.id.tv_ocr_txt) as TextView
        adView = findViewById(R.id.adView)
        AdsUtils.showGoogleBannerAd(this, adView)


    }

    private fun bindView() {
        tv_title!!.text = intent.getStringExtra("group_name")
        iv_preview_img!!.setImageBitmap(Constant.original)
        doOCR(Constant.original)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> {
                onBackPressed()
                return
            }
            R.id.iv_copy_txt -> {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                    ClipData.newPlainText(
                        TextBundle.TEXT_ENTRY,
                        tv_ocr_txt!!.text.toString()
                    )
                )
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                return
            }
            R.id.iv_rescan_img -> {
                tv_ocr_txt!!.text = ""
                doOCR(Constant.original)
                return
            }
            R.id.iv_share_txt -> {
                val intent = Intent("android.intent.action.SEND")
                intent.type = "text/*"
                intent.putExtra("android.intent.extra.SUBJECT", "OCR Text")
                intent.putExtra("android.intent.extra.TEXT", tv_ocr_txt!!.text.toString())
                startActivity(Intent.createChooser(intent, "Share text using"))
                return
            }
            else -> return
        }
    }

    @SuppressLint("CheckResult")
    private fun doOCR(bitmap: Bitmap) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, "Processing", "Doing OCR...", true)
        } else {
            progressDialog!!.show()
        }
//                new Thread(new Runnable() {
//            @Override
//            public void run() {
//                TextRecognizer build = new TextRecognizer.Builder(getApplicationContext()).build();
//                if (!build.isOperational()) {
//                    Log.e(ImageToTextActivity.TAG, "Detector dependencies not loaded yet");
//                    return;
//                }
//                final SparseArray<TextBlock> detect = build.detect(new Frame.Builder().setBitmap(bitmap).build());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (detect.size() != 0) {
//                            StringBuilder sb = new StringBuilder();
//                            for (int i = 0; i < detect.size(); i++) {
//                                sb.append(((TextBlock) detect.valueAt(i)).getValue());
//                                sb.append(" ");
//                            }
//                            tv_ocr_txt.setText(sb.toString());
//                        } else {
//                            tv_ocr_txt.setText("No Text Found...");
//                        }
//                        progressDialog.dismiss();
//                    }
//                });
//            }
//        }).start();
//        OrcUnit.getMapText(bitmap) {
//            progressDialog?.dismiss()
//            if (it.isNullOrEmpty()) tv_ocr_txt?.setText("No Text Found...") else {
//                var mapTexts = it
//                var maxTextUnd = ""
//                var maxTextLange = ""
//                for (i in mapTexts) {
//                    if (i.key == "und") {
//                        if (i.value?.length ?: 0 > maxTextUnd.length) {
//                            maxTextUnd = i.value ?: ""
//                        }
//                    } else {
//                        if (i.value?.length ?: 0 > maxTextLange.length) {
//                            maxTextLange = i.value ?: ""
//                        }
//                    }
//                }
//                // config to tvData
//                tv_ocr_txt?.text =
//                    if (maxTextLange.isEmpty() || maxTextLange.length < maxTextUnd.length * 2 / 3) maxTextUnd else maxTextLange
//            }
//        }

        // for cloud
        // ocr
        // cuongdt
      Single.fromCallable{
          CloudOcr.convertBitmapToText(bitmap, vision)
      }
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
              tv_ocr_txt?.text = it
              progressDialog?.dismiss()
          }, {
              progressDialog?.dismiss()
          })


    }

    companion object {
        private const val TAG = "ImageToTextActivity"
    }
}