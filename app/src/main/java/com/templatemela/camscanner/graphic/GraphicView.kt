package com.templatemela.camscanner.graphic

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import okio.ByteString
import java.io.IOException


class GraphicView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mCropPoints: Array<Point>? = null
    private var bitmap: ImageView? = null

    private var mActWidth = 0
    private var mActHeight = 0
    private var mActLeft = 0
    private var mActTop = 0 //
    private var withView = 0;
    private var heightView = 0;
    private var mGuideLinePaint: Paint? = null
    var mGuideLineColor = Color.GREEN // 辅助线颜色
    var mGuideLineWidth = 5f // 辅助线宽度
    var mScaleX: Float = 0f
    var mScaleY: Float = 0f// 显示的图片与实际图片缩放比
    var mDraggingPoint: Point? = null
    var mDensity = 0f
    var mMagnifierDrawable: ShapeDrawable? = null
    var mMatrixValue = FloatArray(9)
    var mMaskXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    var mPointLinePath = Path()
    var mMagnifierMatrix = Matrix()
    private var mLinePaint: Paint? = null

    // 裁剪区域, 0->LeftTop, 1->RightTop， 2->RightBottom, 3->LeftBottom

    var mEdgeMidPoints: Array<Point>? = null  //边中点


    fun drawPoints(point: Array<Point>, bitmap: ImageView) {
        mCropPoints = point
        this.bitmap = bitmap
        this.invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Draw component in here
        getViewSize()
        drawablePosition()
        onDrawCropPoint(canvas)
        //绘制选区线
        onDrawLines(canvas)
    }

    private fun getViewSize() {
        withView = this.measuredWidth
        heightView = this.measuredHeight
        Log.d("TAG", "getViewSize: ")
    }


    private fun onDrawLines(canvas: Canvas) {
        val path: Path? = resetPointPath()
        if (path != null) {
            canvas.drawPath(path, mLinePaint!!)
        }
    }


    private fun resetPointPath(): Path? {
        try {
            if (!checkPoints(mCropPoints)) {
                return null
            }
            mPointLinePath.reset()
            val lt = mCropPoints!![0]
            val rt = mCropPoints!![1]
            val rb = mCropPoints!![2]
            val lb = mCropPoints!![3]
            mPointLinePath.moveTo(getViewPointX(lt), getViewPointY(lt))
            mPointLinePath.lineTo(getViewPointX(rt), getViewPointY(rt))
            mPointLinePath.lineTo(getViewPointX(rb), getViewPointY(rb))
            mPointLinePath.lineTo(getViewPointX(lb), getViewPointY(lb))
            mPointLinePath.close()
            return mPointLinePath
        } catch (e: Exception) {
            return null
        }
    }

    fun checkPoints(points: Array<Point>?): Boolean {
        return points != null && points.size == 4 && points[0] != null && points[1] != null && points[2] != null && points[3] != null
    }

    private fun getViewPointX(point: Point): Float {
        return getViewPointX(point.x.toFloat())
    }

    private fun getViewPointX(x: Float): Float {
        return x * mScaleX + mActLeft
    }

    private fun getViewPointY(point: Point): Float {
        return getViewPointY(point.y.toFloat())
    }

    private fun getViewPointY(y: Float): Float {
        return y * mScaleY + mActTop
    }

    private fun onDrawCropPoint(canvas: Canvas) {
//        onDrawGuideLine(canvas);
        initPain()
//        points?.let {
//            for (obj in it) {
//                canvas.drawPoint(obj.x.toFloat() , obj.y.toFloat()  , mGuideLinePaint!!)
//            }
//        }
        canvas.drawPoint(0f, 100f, mGuideLinePaint!!);

    }


    private fun drawablePosition() {
        var drawable = bitmap?.drawable
        if (drawable != null && bitmap != null) {
            bitmap?.imageMatrix?.getValues(mMatrixValue)
            mScaleX = mMatrixValue.get(Matrix.MSCALE_X)
            mScaleY = mMatrixValue.get(Matrix.MSCALE_Y)
            val origW = drawable.intrinsicWidth
            val origH = drawable.intrinsicHeight
            mActWidth = Math.round(origW * mScaleX).toInt()
            mActHeight = Math.round(origH * mScaleY).toInt()
            mActLeft = (width - mActWidth) / 2
            mActTop = (height - mActHeight) / 2
        }
    }


    private fun initPain() {
        mGuideLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mGuideLinePaint!!.color = mGuideLineColor
        mGuideLinePaint!!.style = Paint.Style.FILL
        mGuideLinePaint!!.strokeWidth = mGuideLineWidth


        mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mLinePaint?.color = mGuideLineColor
        mLinePaint?.strokeWidth = mGuideLineWidth
        mLinePaint?.style = Paint.Style.STROKE
    }
}