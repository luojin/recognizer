package edu.cmu.pocketsphinx.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.pocketsphinx.WordModel;
import edu.cmu.pocketsphinx.demo.R;

/*
 * Created by luo on 2015/12/14.
 */
public class DiffColorTextView extends View {
    private int mCorrectColor;
    private int mWrongColor;
    private int mTextSize;
    
    private Paint mPaint;
    private Rect mBound;

    private List<WordModel> mWordList = new ArrayList<>();
    private StringBuffer mStringBuf = new StringBuffer();

    public DiffColorTextView(Context context) {
        this(context,null);
    }

    public DiffColorTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiffColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DiffColorTextView,
                defStyleAttr, 0
        ));
    }

    private void initAttrs(TypedArray a){
        try {
            mCorrectColor = a.getColor(R.styleable.DiffColorTextView_correctColor, Color.BLACK);
            mWrongColor = a.getColor(R.styleable.DiffColorTextView_wrongColor,Color.BLACK);
            mTextSize = (int) a.getDimension(R.styleable.DiffColorTextView_textSize,10.0f);
        }finally {
            a.recycle();
        }

        initData();
    }

    private void initData(){
        mPaint = new Paint();
        mPaint.setColor(mCorrectColor);
        mPaint.setTextSize(mTextSize);

        mBound = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(canvas!=null){
            if(mWordList==null)
                return;

            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            mStringBuf.delete(0,mStringBuf.length());

            int baseY = (int) Math.abs(fontMetrics.top+fontMetrics.bottom);
            int baseX = 0;

            for (WordModel item:mWordList) {
                if(item.isCorrect()){
                    mPaint.setColor(mCorrectColor);
                }else{
                    mPaint.setColor(mWrongColor);
                }
                canvas.drawText(item.getWord(),
                        baseX,
                        baseY,
                        mPaint);
                mStringBuf.append(item.getWord() + " ");
                baseX = (int) mPaint.measureText(mStringBuf.toString());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        mStringBuf.delete(0,mStringBuf.length());
        for(WordModel item : mWordList){
            mStringBuf.append(item.getWord()+" ");
        }
        mPaint.getTextBounds(mStringBuf.toString(), 0, mStringBuf.toString().length(), mBound);
        mPaint.measureText(mStringBuf.toString());

        int width = 0;
        int height = 0;

        /**
         * 设置宽度
         */
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (specMode)
        {
            case MeasureSpec.EXACTLY:// 明确指定了
                width = getPaddingLeft() + getPaddingRight() + specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                width = getPaddingLeft() + getPaddingRight() + mBound.width();
                break;
        }

        /**
         * 设置高度
         */
        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode)
        {
            case MeasureSpec.EXACTLY:// 明确指定了
                height = getPaddingTop() + getPaddingBottom() + specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                height = getPaddingTop() + getPaddingBottom() + mBound.height();
                break;
        }

        setMeasuredDimension(width, height);
    }


    //---public api
    public void setText(List<WordModel> l){
        mWordList=l;

        if(mWordList==null)
            return;

        requestLayout();
        postInvalidate();
    }
}
