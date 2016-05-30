package com.faceplusplus.apitest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.faceplusplus.api.FaceDetecter.Face;

public class FaceMask extends View {
    Paint localPaint = null;
    Face[] faceinfos = null;
    RectF rect = null;

    public FaceMask(Context context, AttributeSet atti) {
        super(context, atti);
        rect = new RectF();
        localPaint = new Paint();
        localPaint.setColor(0x7fff00b4);
        localPaint.setStrokeWidth(10);
        localPaint.setStyle(Paint.Style.STROKE);
    }

    public void setFaceInfo(Face[] faceinfos)
    {
        this.faceinfos = faceinfos;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faceinfos == null)  return;
        for (Face localFaceInfo : faceinfos) {
            rect.set(getWidth() * localFaceInfo.left, getHeight()
                    * localFaceInfo.top, getWidth() * localFaceInfo.right,
                    getHeight()
                            * localFaceInfo.bottom);
            canvas.drawRect(rect, localPaint);
        }
    }
}