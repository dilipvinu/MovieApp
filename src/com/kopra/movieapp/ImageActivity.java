package com.kopra.movieapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.android.volley.toolbox.ImageLoader;
import com.kopra.movieapp.net.VolleyManager;
import com.kopra.movieapp.widget.CacheImageView;

public class ImageActivity extends Activity {

	private static final int ANIM_DURATION = 500;
	
	private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    
    private FrameLayout mRootView;
	private ColorDrawable mBackground;
	private CacheImageView mImageView;
	private ProgressBar mProgressView;
	
	private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;

    private String mImageUrl;
    private boolean mLoading;
    
    private ImageLoader mImageLoader;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		
		mRootView = (FrameLayout) findViewById(R.id.rootView);
		mImageView = (CacheImageView) findViewById(R.id.image);
		mImageView.setOnLoadListener(onLoad);
		mProgressView = (ProgressBar) findViewById(R.id.progress);
		
		mBackground = new ColorDrawable(Color.BLACK);
        mRootView.setBackgroundDrawable(mBackground);
		
		Bundle extras = getIntent().getExtras();
		final int thumbnailTop = extras.getInt("top");
		final int thumbnailLeft = extras.getInt("left");
		final int thumbnailWidth = extras.getInt("width");
		final int thumbnailHeight = extras.getInt("height");
		final Bitmap thumb = (Bitmap) extras.getParcelable("bitmap");
		
		mImageUrl = extras.getString("url");
		
		BitmapDrawable drawable = new BitmapDrawable(thumb);
		mImageView.setImageDrawable(drawable);
		mImageView.setDefaultImageDrawable(drawable);
		
		mImageLoader = VolleyManager.getInstance(this).getImageLoader();
		
		if (savedInstanceState == null) {
			ViewTreeObserver observer = mImageView.getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    mImageView.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];
                    
                    // Scale factors to make the large version the same size as the thumbnail
                    mWidthScale = (float) thumbnailWidth / mImageView.getWidth();
                    mHeightScale = (float) thumbnailHeight / mImageView.getHeight();
    
                    runEnterAnimation();
                    
                    return true;
                }
            });
		} else {
			mLoading = savedInstanceState.getBoolean("loading");
			if (mLoading) {
				mProgressView.setVisibility(View.VISIBLE);
			}
			mImageView.setImageUrl(mImageUrl, mImageLoader);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("loading", mLoading);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		runExitAnimation(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}
	
	private void runEnterAnimation() {
		final long duration = ANIM_DURATION;
        
        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        mImageView.setPivotX(0);
        mImageView.setPivotY(0);
        mImageView.setScaleX(mWidthScale);
        mImageView.setScaleY(mHeightScale);
        mImageView.setTranslationX(mLeftDelta);
        mImageView.setTranslationY(mTopDelta);
        
        // Animate scale and translation to go from thumbnail to full size
        ViewPropertyAnimator animator = mImageView.animate();
        animator.setListener(new AnimatorListenerAdapter() {
        	@Override
			public void onAnimationEnd(Animator animation) {
        		new Handler().post(new Runnable() {
					@Override
					public void run() {
						mLoading = true;
						mProgressView.setVisibility(View.VISIBLE);
						mImageView.setImageUrl(mImageUrl, mImageLoader);
					}
				});
			}
		});
        animator.setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator);
        
        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();
	}
	
	private void runExitAnimation(final Runnable endAction) {
		final long duration = ANIM_DURATION;
        
		mProgressView.setVisibility(View.GONE);
		
        // No need to set initial values for the reverse animation; the image is at the
        // starting size/location that we want to start from. Just animate to the
        // thumbnail size/location that we retrieved earlier 
        
        // Animate image back to thumbnail size/location
        ViewPropertyAnimator animator = mImageView.animate();
        animator.setListener(new AnimatorListenerAdapter() {
        	@Override
			public void onAnimationEnd(Animator animation) {
				new Handler().post(endAction);
			}
		});
        animator.setDuration(duration).
                scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta);
        
        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();
	}
	
	private CacheImageView.OnLoadListener onLoad = new CacheImageView.OnLoadListener() {
		@Override
		public void onLoad(boolean success) {
			mProgressView.setVisibility(View.GONE);
			mLoading = false;
		}
	};
}
