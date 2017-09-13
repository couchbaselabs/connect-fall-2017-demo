package com.couchbase.mobile.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.couchbase.mobile.R;

public class ArcGaugeView extends View {
    private static final String TAG = ArcGaugeView.class.getCanonicalName();

    // Default feature attributes
    public static boolean IS_ANIMATED_DEFAULT = false;
    public static boolean IS_SHADOWED_DEFAULT = false;
    public static boolean IS_ROUNDED_DEFAULT = false;

    // Default text attributes
    //TODO scale default to density?
    public static float TEXT_SIZE_DEFAULT = 200.0F;
    public static int TEXT_COLOR_DEFAULT = Color.parseColor("#222222");
    public static Typeface TYPEFACE_DEFAULT = Typeface.DEFAULT;

    // Default shadow attributes
    public static float SHADOW_DEPTH_DEFAULT = 5.0F;
    public static int SHADOW_ANGLE_DEFAULT = 0;
    public static float SHADOW_RADIUS_DEFAULT = 0;
    public static int SHADOW_COLOR_DEFAULT = 0;

    // Default animation attributes
    public static int ANIMATION_DURATION_DEFAULT = 500;
    public static Interpolator INTERPOLATOR_DEFAULT = null;

    // Default arc attributes
    public static float ARC_WIDTH_DEFAULT = 15.0F;
    public static float ARC_RADIUS_DEFAULT = 80.0F;
    public static float START_ANGLE_DEFAULT = -90.0F;
    public static float SWEEP_DEFAULT = 270.0F;
    public static int ARC_COLOR_DEFAULT = Color.parseColor("#222222");

    private static final int SCALE_TYPE_FRACTION = 0;
    private static final int SCALE_TYPE_DIMENSION = 1;

    /* --- instance variables --- */
    private boolean isAnimated = IS_ANIMATED_DEFAULT;
    private boolean isShadowed = IS_SHADOWED_DEFAULT;
    private boolean isRounded = IS_ROUNDED_DEFAULT;
    private boolean isGradient;

    private String text;
    private float textSize = TEXT_SIZE_DEFAULT;
    private int textColor = TEXT_COLOR_DEFAULT;
    private Typeface typeface = TYPEFACE_DEFAULT;

    private float shadowDepth = SHADOW_DEPTH_DEFAULT;
    private int shadowAngle = SHADOW_ANGLE_DEFAULT;
    private float shadowRadius = SHADOW_RADIUS_DEFAULT;
    private int shadowColor = SHADOW_COLOR_DEFAULT;

    private int animationDuration = ANIMATION_DURATION_DEFAULT;
    private Interpolator interpolator = INTERPOLATOR_DEFAULT;

    private float arcWidth = ARC_WIDTH_DEFAULT;
    private int arcWidthScale = SCALE_TYPE_FRACTION;
    private float arcRadius = ARC_RADIUS_DEFAULT;
    private int arcRadiusScale = SCALE_TYPE_FRACTION;
    private float startAngle = START_ANGLE_DEFAULT;
    private float sweep = SWEEP_DEFAULT;
    private int[] arcColors;

    private DisplayMetrics displayMetrics;

    private Paint arcPaint;
    private Path arcPath = new Path();
    private PathMeasure arcPathMeasure = new PathMeasure();
    private Shader arcShader;
    private Matrix arcMatrix = new Matrix();
    private TextPaint textPaint;
    private Paint paint;

    private RectF rectF = new RectF();
    private Rect rect = new Rect();

    private Position center = new Position();
    private Position textPosition = new Position();
    private float[] pos = new float[2];
    private float[] tan = new float[2];
    private float capRadius;

    /*--- helper classes ---*/
    private class Position {
        float x;
        float y;
    }

    public class Fraction {
        public final float val;

        Fraction(float val) { this.val = val; }
    }

    public class Dimension {
        public final float val;

        Dimension(float val) { this.val = val; }
    }

    /* --- constructors -- */
    public ArcGaugeView(Context context) { this(context, null, 0); }

    public ArcGaugeView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public ArcGaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        displayMetrics = context.getResources().getDisplayMetrics();

        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setPathEffect(new CornerPathEffect(0.5F));

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ArcGaugeView);

        isAnimated = styledAttrs.getBoolean(R.styleable.ArcGaugeView_agv_animated, isAnimated);
        isShadowed = styledAttrs.getBoolean(R.styleable.ArcGaugeView_agv_shadowed, isShadowed);
        setIsRounded(styledAttrs.getBoolean(R.styleable.ArcGaugeView_agv_rounded, isRounded));

        text = styledAttrs.getString(R.styleable.ArcGaugeView_agv_text);
        setTextSize(styledAttrs.getDimension(R.styleable.ArcGaugeView_agv_text_size, textSize));
        setTextColor(styledAttrs.getColor(R.styleable.ArcGaugeView_agv_text_color, textColor));
        setTypeface(styledAttrs.getString(R.styleable.ArcGaugeView_agv_typeface));

        shadowDepth = styledAttrs.getDimension(R.styleable.ArcGaugeView_agv_shadow_depth, shadowDepth);
        shadowAngle = styledAttrs.getInteger(R.styleable.ArcGaugeView_agv_shadow_angle, shadowAngle);
        shadowRadius = styledAttrs.getDimension(R.styleable.ArcGaugeView_agv_shadow_radius, shadowRadius);
        shadowColor = styledAttrs.getColor(R.styleable.ArcGaugeView_agv_shadow_color, shadowColor);

        animationDuration = styledAttrs.getInteger(R.styleable.ArcGaugeView_agv_animation_duration, animationDuration);
        int interpolatorId = styledAttrs.getResourceId(R.styleable.ArcGaugeView_agv_interpolator, 0);

        if (0 != interpolatorId) {
            interpolator = AnimationUtils.loadInterpolator(context, interpolatorId);
        }

        if (!styledAttrs.hasValue(R.styleable.ArcGaugeView_agv_arc_width)) {
            setArcWidth(new Fraction(ARC_WIDTH_DEFAULT));
        } else {
            TypedValue tv = new TypedValue();
            styledAttrs.getValue(R.styleable.ArcGaugeView_agv_arc_width, tv);

            if (tv.type == TypedValue.TYPE_DIMENSION) {
                setArcWidth(new Dimension(tv.getDimension(displayMetrics)));
            } else {
                setArcWidth(new Fraction(tv.getFraction(1, 1)));
            }
        }

        if (!styledAttrs.hasValue(R.styleable.ArcGaugeView_agv_arc_radius)) {
            setArcRadius(new Fraction(ARC_RADIUS_DEFAULT));
        } else {
            TypedValue tv = new TypedValue();
            styledAttrs.getValue(R.styleable.ArcGaugeView_agv_arc_radius, tv);

            if (tv.type == TypedValue.TYPE_DIMENSION) {
                setArcRadius(new Dimension(tv.getDimension(displayMetrics)));
            } else {
                setArcRadius(new Fraction(tv.getFraction(1, 1)));
            }
        }

        startAngle = styledAttrs.getFloat(R.styleable.ArcGaugeView_agv_start_angle, startAngle);
        sweep = styledAttrs.getFloat(R.styleable.ArcGaugeView_agv_sweep, sweep);

        TypedValue tv = getTypeValue(styledAttrs, R.styleable.ArcGaugeView_agv_color);

        if (null != tv && tv.type == TypedValue.TYPE_REFERENCE) {
            setArcColors(getReferencedArray(styledAttrs, R.styleable.ArcGaugeView_agv_color));
        } else {
            setArcColor(styledAttrs.getColor(R.styleable.ArcGaugeView_agv_color, ARC_COLOR_DEFAULT));
        }

        styledAttrs.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float edge = Math.min(getWidth(), getHeight());

        center.x = edge/2;
        center.y = edge/2;

        textPosition.x = center.x;
        textPosition.y = center.y;

        // Measure text more accurate, but only provides width.
        // Useful when more control needed, but here text is already centered.
        // textPosition.x -= textPaint.measureText(text)/2;

        textPaint.getTextBounds(text, 0, text.length(), rect);

        textPosition.y -= (rect.top - rect.bottom)/2;

        float radius = (SCALE_TYPE_FRACTION == arcRadiusScale ? arcRadius * edge / 2.0F : arcRadius);

        rectF.set(center.x - radius, center.y - radius, center.x + radius, center.y + radius);

        float width = (SCALE_TYPE_FRACTION == arcWidthScale ? arcWidth * edge : arcWidth);

        arcPaint.setStrokeWidth(width);

        capRadius = width/2;

        arcPath.reset();
        arcPath.addArc(rectF, startAngle, sweep);

        if (null == arcShader) return;

        arcPathMeasure.setPath(arcPath, false);
        arcPathMeasure.getPosTan(0, pos, tan);

        arcMatrix.reset();
        arcMatrix.setTranslate(center.x, center.y);
        float angle = -180.0F * (float)(Math.atan2(tan[0], tan[1])/Math.PI);
        arcMatrix.preRotate(angle);

        arcShader.setLocalMatrix(arcMatrix);
        arcPaint.setShader(arcShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(text, textPosition.x, textPosition.y, textPaint);
        canvas.drawPath(arcPath, arcPaint);

        if (isGradient && isRounded) {
            rectF.set(pos[0] - capRadius, pos[1] - capRadius, pos[0] + capRadius, pos[1] + capRadius);
            canvas.drawArc(rectF, startAngle, startAngle - 180.0F, true, paint);
        }
    }

    public boolean getIsAnimated() { return isAnimated; }
    public void setIsAnimated(boolean isAnimated) {
        this.isAnimated = isAnimated;

        postInvalidate();
    }

    public boolean getIsShadowed() { return isShadowed; }
    public void setIsShadowed(boolean isShadowed) {
        this.isShadowed = isShadowed;

        postInvalidate();
    }

    public boolean getIsRounded() { return isRounded; }
    public void setIsRounded(boolean isRounded) {
        this.isRounded = isRounded;

        if (isRounded) {
            arcPaint.setStrokeCap(Paint.Cap.ROUND);
            arcPaint.setStrokeJoin(Paint.Join.ROUND);
        } else {
            arcPaint.setStrokeCap(Paint.Cap.BUTT);
            arcPaint.setStrokeJoin(Paint.Join.MITER);
        }

        postInvalidate();
    }

    public String getText() { return text; }
    public void setText(String text) {
        this.text = text;

        postInvalidate();
    }

    public float getTextSize() { return textSize; }
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);

        postInvalidate();
    }

    public int getTextColor() { return textColor; }
    public void setTextColor(int textColor) {
        this.textColor = textColor;
        textPaint.setColor(textColor);

        postInvalidate();
    }

    public Typeface getTypeface() { return typeface; }
    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
        textPaint.setTypeface(typeface);

        postInvalidate();
    }
    public void setTypeface(String typeface) {
        Typeface tf;

        try {
            tf = Typeface.createFromAsset(getContext().getAssets(), typeface);
        } catch (Exception e) {
            tf = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        }

        setTypeface(tf);
    }

    public float getShadowDepth() { return shadowDepth; }
    public void setShadowDepth(float shadowDepth) {
        this.shadowDepth = shadowDepth;

        postInvalidate();
    }

    public int getShadowAngle() { return shadowAngle; }
    public void setShadowAngle(int shadowAngle) {
        this.shadowAngle = shadowAngle;

        postInvalidate();
    }

    public int getShadowColor() { return shadowColor; }
    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;

        postInvalidate();
    }

    public float getArcWidth() { return arcWidth; }
    public void setArcWidth(float val) {
        arcWidth = val;
        arcWidthScale = SCALE_TYPE_FRACTION;

        postInvalidate();
    }
    public void setArcWidth(Fraction fraction) {
        setArcWidth(fraction.val);
    }
    public void setArcWidth(Dimension dimension) {
        arcWidth = dimension.val;
        arcWidthScale = SCALE_TYPE_DIMENSION;

        postInvalidate();
    }

    public int getArcWidthScale() { return arcWidthScale; }

    public float getArcRadius() { return arcRadius; }
    public void setArcRadius(float val) {
        arcRadius = val;
        arcRadiusScale = SCALE_TYPE_FRACTION;

        postInvalidate();
    }
    public void setArcRadius(Fraction fraction) {
        setArcRadius(fraction.val);
    }
    public void setArcRadius(Dimension dimension) {
        arcRadius = dimension.val;
        arcRadiusScale = SCALE_TYPE_DIMENSION;

        postInvalidate();
    }

    public int getArcRadiusScale() { return arcRadiusScale; }

    public float getSweep() { return sweep; }
    public void setSweep(float sweep) {
        this.sweep = sweep;

        arcPath.reset();
        arcPath.addArc(rectF, startAngle, sweep);

        postInvalidate();
    }

    public int[] getArcColors() { return arcColors; }
    public void setArcColors(String[] colors) {
        int[] colorArray = new int[colors.length];

        for (int nn = 0; nn < colors.length; ++nn) {
            colorArray[nn] = Color.parseColor(colors[nn]);
        }

        setArcColors(colorArray);
    }
    public void setArcColor(int color) { setArcColors(new int[]{ color }); }
    public void setArcColors(int[] colors) {
        if (colors.length < 1) {
            Log.e(TAG, "Error setting colors: count " + colors.length);
        }

        this.arcColors = colors;

        arcPaint.setColor(colors[0]);
        paint.setColor(colors[0]);

        isGradient = false;
        arcShader = null;

        if (arcColors.length > 1) {
            isGradient = true;
            arcShader = new SweepGradient(0, 0, arcColors[0], arcColors[1]);
        }

        arcPaint.setShader(arcShader);

        postInvalidate();
    }

    private String[] getReferencedArray(TypedArray ta, int id) {
        if (!ta.hasValue(id)) return null;

        int resourceId = ta.getResourceId(id, -1);

        if (resourceId == -1) return null;

        return ta.getResources().getStringArray(resourceId);
    }

    private TypedValue getTypeValue(TypedArray ta, int id) {
        if (!ta.hasValue(id)) return null;

        TypedValue tv = new TypedValue();
        ta.getValue(id, tv);

        return tv;
    }
}