package com.example.javahomework;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class SwipeRecyclerView extends RecyclerView {
    private int maxLength, mTouchSlop;
    private int xDown, yDown, xMove, yMove;

    private int curSelectPosition;
    private Scroller mScroller;

    private LinearLayout mCurItemLayout, mLastItemLayout;
    private LinearLayout mTLHidden;

    private TextView mItemContent;
    private LinearLayout mItemDelete;

    private int mHiddenWidth;
    private int mMoveWidth = 0;

    private boolean isFirst = true;
    private Context mContext;

    private OnRightClickListener mRightListener;

    public void setRightClickListener(OnRightClickListener listener) {
        this.mRightListener = listener;
    }

    public SwipeRecyclerView(Context context) {
        this(context, null);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        maxLength = ((int) (180 * context.getResources().getDisplayMetrics().density + 0.5f));
        mScroller = new Scroller(context, new LinearInterpolator(context, null));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        int x = (int) e.getX();
//        int y = (int) e.getY();
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                xDown = x;
//                yDown = y;
//                int firstPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
//                Rect itemRect = new Rect();
//                final int count = getChildCount();
//                for (int i = 0; i < count; i++) {
//                    final View child = getChildAt(i);
//                    if (child.getVisibility() == View.VISIBLE) {
//                        child.getHitRect(itemRect);
//                        if (itemRect.contains(x, y)) {
//                            curSelectPosition = firstPosition + i;
//                            break;
//                        }
//                    }
//                }
//                if (isFirst) {
//                    isFirst = false;
//                } else {
//                    if (mLastItemLayout != null && mMoveWidth > 0) {
//                        scrollRight(mLastItemLayout, (0 - mMoveWidth));
//                        mHiddenWidth = 0;
//                        mMoveWidth = 0;
//                    }
//                }
//                View item = getChildAt(curSelectPosition - firstPosition);
//                if (item != null) {
//                    TaskListAdapter.ViewHolder viewHolder = (TaskListAdapter.ViewHolder) getChildViewHolder(item);
//                    mCurItemLayout = viewHolder.tasklistItem;
//                    mTLHidden = mCurItemLayout.findViewById(R.id.tasklist_item_hidden);
//                    mItemDelete = mCurItemLayout.findViewById(R.id.tasklist_item_hidden);
//                    mItemDelete.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (mRightListener != null) {
//
//                                mRightListener.onRightClick(curSelectPosition, "");
//                            }
//                        }
//                    });
//                    mHiddenWidth = mTLHidden.getWidth();
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                xMove = x;
//                yMove = y;
//                int dx = xMove - xDown;
//                int dy = yMove - yDown;
//                if (dx < 0 && Math.abs(dx) > mTouchSlop && Math.abs(dy) < mTouchSlop) {
//                    int newScrollX = Math.abs(dx);
//                    if (mMoveWidth >= mHiddenWidth) {
//                        newScrollX = 0;
//                    } else if (mMoveWidth + newScrollX > mHiddenWidth) {
//                        newScrollX = mHiddenWidth - mMoveWidth;
//                    }
//                    scrollLeft(mCurItemLayout, newScrollX);
//                    mMoveWidth = mMoveWidth + newScrollX;
//                } else if (dx > 0) {
//                    scrollRight(mCurItemLayout, 0 - mMoveWidth);
//                    mMoveWidth = 0;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                int scrollX = mCurItemLayout.getScrollX();
//                if (mHiddenWidth > mMoveWidth) {
//                    int toX = mHiddenWidth - mMoveWidth;
//                    if (scrollX > mHiddenWidth / 2) {
//                        scrollLeft(mCurItemLayout, toX);
//                        mMoveWidth = mHiddenWidth;
//                    } else {
//                        scrollRight(mCurItemLayout, 0 - mMoveWidth);
//                        mMoveWidth = 0;
//                    }
//                }
//                mLastItemLayout = mCurItemLayout;
//                break;
//        }
        return super.onTouchEvent(e);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mCurItemLayout.scrollBy(mScroller.getCurrX(), 0);
            invalidate();
        }
    }

    private void scrollLeft(View item, int scrollX) {
        item.scrollBy(scrollX, 0);
    }

    private void scrollRight(View item, int scrollX) {
        item.scrollBy(scrollX, 0);
    }

    public interface OnRightClickListener {
        void onRightClick(int position, String id);
    }
}
