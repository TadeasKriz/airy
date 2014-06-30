package com.mirasmithy.airy;

/**
 * Copyright 2014 Miras Absar
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import java.util.ArrayList;

public class Airy implements OnTouchListener {

	private static final int DURATION = 300;
	private static final int THRESHOLD_DP = 48;

	public static final int TAP = 0;
	public static final int SWIPE_UP = 1;
	public static final int SWIPE_DOWN = 2;
	public static final int SWIPE_LEFT = 3;
	public static final int SWIPE_RIGHT = 4;
	public static final int TWO_FINGER_TAP = 5;
	public static final int TWO_FINGER_SWIPE_UP = 6;
	public static final int TWO_FINGER_SWIPE_DOWN = 7;
	public static final int TWO_FINGER_SWIPE_LEFT = 8;
	public static final int TWO_FINGER_SWIPE_RIGHT = 9;
	public static final int TWO_FINGER_PINCH_IN = 10;
	public static final int TWO_FINGER_PINCH_OUT = 11;

	private float mThresholdPx;

	private ArrayList<Airy.Pointer> mPointers;

	public Airy(Activity pActivity) {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		pActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		float mDisplayDensity = mDisplayMetrics.density;

		mThresholdPx = THRESHOLD_DP * mDisplayDensity;
	}

	public Airy(float pDisplayDensity) {
		mThresholdPx = THRESHOLD_DP * pDisplayDensity;
	}

	@Override
	public boolean onTouch(View pView, MotionEvent pMotionEvent) {
		int mAction = pMotionEvent.getActionMasked();
		int mActionIndex = pMotionEvent.getActionIndex();
		int mPointerId = pMotionEvent.getPointerId(mActionIndex);
		long mEventTime = pMotionEvent.getEventTime();
		float mX = pMotionEvent.getX(mActionIndex);
		float mY = pMotionEvent.getY(mActionIndex);

		switch (mAction) {
			case MotionEvent.ACTION_DOWN:
				if (mPointers == null) {
					mPointers = new ArrayList<Airy.Pointer>();
				} else {
					if (!mPointers.isEmpty()) {
						mPointers.clear();
					}
				}

				mPointers.add(new Airy.Pointer(mPointerId,
											   mEventTime,
											   mX, mY));
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				mPointers.add(new Airy.Pointer(mPointerId,
											   mEventTime,
											   mX, mY));
				break;
			case MotionEvent.ACTION_POINTER_UP:
				for (int i = 0 ; i < mPointers.size(); i++) {
					if (mPointers.get(i).getId() == mPointerId) {
						mPointers.get(i).setUpTime(mEventTime);
						mPointers.get(i).setUpX(mX);
						mPointers.get(i).setUpY(mY);
						break;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				for (int i = 0 ; i < mPointers.size(); i++) {
					if (mPointers.get(i).getId() == mPointerId) {
						mPointers.get(i).setUpTime(mEventTime);
						mPointers.get(i).setUpX(mX);
						mPointers.get(i).setUpY(mY);
						break;
					}
				}

				process(pView);
				break;
		}
		return true;
	}

	public void process(View pView) {
		int mTotalPointerCount = mPointers.size();

		if (mTotalPointerCount == 1) {
			Airy.Pointer mPointerI = mPointers.get(0);

			if (mPointerI.getUpTime() - mPointerI.getDownTime() <= DURATION) {
				float mPointerIUpThres = mPointerI.getDownY() - mThresholdPx;
				float mPointerIDownThres = mPointerI.getDownY() + mThresholdPx;
				float mPointerILeftThres = mPointerI.getDownX() - mThresholdPx;
				float mPointerIRightThres = mPointerI.getDownX() + mThresholdPx;

				if (mPointerI.getUpX() > mPointerILeftThres &&
					mPointerI.getUpX() < mPointerIRightThres &&
					mPointerI.getUpY() > mPointerIUpThres &&
					mPointerI.getUpY() < mPointerIDownThres) {

					onGesture(pView, TAP);
				}

				if (mPointerI.getUpX() > mPointerILeftThres &&
					mPointerI.getUpX() < mPointerIRightThres &&
					mPointerI.getUpY() <= mPointerIUpThres) {

					onGesture(pView, SWIPE_UP);
				}

				if (mPointerI.getUpX() > mPointerILeftThres &&
					mPointerI.getUpX() < mPointerIRightThres &&
					mPointerI.getUpY() >= mPointerIDownThres) {

					onGesture(pView, SWIPE_DOWN);
				}

				if (mPointerI.getUpX() <= mPointerILeftThres &&
					mPointerI.getUpY() > mPointerIUpThres &&
					mPointerI.getUpY() < mPointerIDownThres) {

					onGesture(pView, SWIPE_LEFT);
				}

				if (mPointerI.getUpX() >= mPointerIRightThres &&
					mPointerI.getUpY() > mPointerIUpThres &&
					mPointerI.getUpY() < mPointerIDownThres) {

					onGesture(pView, SWIPE_RIGHT);
				}
			}
		}

		if (mTotalPointerCount == 2) {
			Airy.Pointer mPointerI = mPointers.get(0);
			Airy.Pointer mPointerII = mPointers.get(1);

			if (mPointerI.getUpTime() - mPointerI.getDownTime() <= DURATION &&
				mPointerII.getUpTime() - mPointerII.getDownTime() <= DURATION) {

				float mPointerIUpThres = mPointerI.getDownY() - mThresholdPx;
				float mPointerIDownThres = mPointerI.getDownY() + mThresholdPx;
				float mPointerILeftThres = mPointerI.getDownX() - mThresholdPx;
				float mPointerIRightThres = mPointerI.getDownX() + mThresholdPx;

				float mPointerIIUpThres = mPointerII.getDownY() - mThresholdPx;
				float mPointerIIDownThres = mPointerII.getDownY() + mThresholdPx;
				float mPointerIILeftThres = mPointerII.getDownX() - mThresholdPx;
				float mPointerIIRightThres = mPointerII.getDownX() + mThresholdPx;

				double mDownDistance = Math.sqrt(Math.pow(mPointerI.getDownX() - mPointerII.getDownX(), 2) +
												 Math.pow(mPointerI.getDownY() - mPointerII.getDownY(), 2));
				double mUpDistance = Math.sqrt(Math.pow(mPointerI.getUpX() - mPointerII.getUpX(), 2) +
											   Math.pow(mPointerI.getUpY() - mPointerII.getUpY(), 2));

				if (mPointerI.getUpX() > mPointerILeftThres &&
					mPointerI.getUpX() < mPointerIRightThres &&
					mPointerI.getUpY() > mPointerIUpThres &&
					mPointerI.getUpY() < mPointerIDownThres &&
					mPointerII.getUpX() > mPointerIILeftThres &&
					mPointerII.getUpX() < mPointerIIRightThres &&
					mPointerII.getUpY() > mPointerIIUpThres &&
					mPointerII.getUpY() < mPointerIIDownThres) {

					onGesture(pView, TWO_FINGER_TAP);
				}

				if (mPointerI.getUpX() > mPointerILeftThres &&
					mPointerI.getUpX() < mPointerIRightThres &&
					mPointerI.getUpY() <= mPointerIUpThres &&
					mPointerII.getUpX() > mPointerIILeftThres &&
					mPointerII.getUpX() < mPointerIIRightThres &&
					mPointerII.getUpY() <= mPointerIIUpThres) {

					onGesture(pView, TWO_FINGER_SWIPE_UP);
					return;
				}

				if (mPointerI.getUpX() > mPointerILeftThres &&
					mPointerI.getUpX() < mPointerIRightThres &&
					mPointerI.getUpY() >= mPointerIDownThres &&
					mPointerII.getUpX() > mPointerIILeftThres &&
					mPointerII.getUpX() < mPointerIIRightThres &&
					mPointerII.getUpY() >= mPointerIIDownThres) {

					onGesture(pView, TWO_FINGER_SWIPE_DOWN);
					return;
				}

				if (mPointerI.getUpX() <= mPointerILeftThres &&
					mPointerI.getUpY() > mPointerIUpThres &&
					mPointerI.getUpY() < mPointerIDownThres &&
					mPointerII.getUpX() <= mPointerIILeftThres &&
					mPointerII.getUpY() > mPointerIIUpThres &&
					mPointerII.getUpY() < mPointerIIDownThres) {

					onGesture(pView, TWO_FINGER_SWIPE_LEFT);
					return;
				}

				if (mPointerI.getUpX() >= mPointerIRightThres &&
					mPointerI.getUpY() > mPointerIUpThres &&
					mPointerI.getUpY() < mPointerIDownThres &&
					mPointerII.getUpX() >= mPointerIIRightThres &&
					mPointerII.getUpY() > mPointerIIUpThres &&
					mPointerII.getUpY() < mPointerIIDownThres) {

					onGesture(pView, TWO_FINGER_SWIPE_RIGHT);
					return;
				}

				if (mUpDistance <= mDownDistance - mThresholdPx) {
					onGesture(pView, TWO_FINGER_PINCH_IN);
				}

				if (mUpDistance >= mDownDistance + mThresholdPx) {
					onGesture(pView, TWO_FINGER_PINCH_OUT);
				}
			}
		}
	}

	public void onGesture(View pView, int pGestureId) {}

	public class Pointer {

		private int mId;

		private long mDownTime;
		private float mDownX;
		private float mDownY;

		private long mUpTime;
		private float mUpX;
		private float mUpY;

		public Pointer(int pId) {
			mId = pId;
		}

		public Pointer(int pId,
					   long pDownTime,
					   float pDownX, float pDownY) {

			mId = pId;
			mDownTime = pDownTime;
			mDownX = pDownX;
			mDownY = pDownY;
		}

		public int getId() {
			return mId;
		}

		public void setDownTime(long pDownTime) {
			mDownTime = pDownTime;
		}

		public long getDownTime() {
			return mDownTime;
		}

		public void setDownX(float pDownX) {
			mDownX = pDownX;
		}

		public float getDownX() {
			return mDownX;
		}

		public void setDownY(float pDownY) {
			mDownY = pDownY;
		}

		public float getDownY() {
			return mDownY;
		}

		public void setUpTime(long pUpTime) {
			mUpTime = pUpTime;
		}

		public long getUpTime() {
			return mUpTime;
		}

		public void setUpX(float pUpX) {
			mUpX = pUpX;
		}

		public float getUpX() {
			return mUpX;
		}

		public void setUpY(float pUpY) {
			mUpY = pUpY;
		}

		public float getUpY() {
			return mUpY;
		}

	}

}
