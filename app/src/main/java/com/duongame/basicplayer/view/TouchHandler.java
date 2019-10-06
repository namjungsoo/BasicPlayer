package com.duongame.basicplayer.view;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class TouchHandler {
    //region
    // Touch
    protected enum Axis {
        AXIS_X,
        AXIS_Y,
        AXIS_BOTH
    }

    Axis touchAxis = Axis.AXIS_X;

    // touch
    boolean isBeingDragged = false;
    PointF lastMotionPt = new PointF();
    private PointF initialMotionPt = new PointF();

    // configuration
    private VelocityTracker velocityTracker = null;
    private int touchSlop = 0;

    private void startDragXIfNeeded(MotionEvent ev) {
        final float x = ev.getX(0);
        final float xSignedDiff = x - initialMotionPt.x;
        final float xDiff = Math.abs(xSignedDiff);
        if (xDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    private void startDragYIfNeeded(MotionEvent ev) {
        final float y = ev.getY(0);
        final float ySignedDiff = y - initialMotionPt.y;
        final float yDiff = Math.abs(ySignedDiff);
        if (yDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    public boolean handleTouch(View v, MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastMotionPt.x = initialMotionPt.x = ev.getX(0);
                lastMotionPt.y = initialMotionPt.y = ev.getY(0);
                return true;
                //break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!isBeingDragged) {
                    if (touchAxis == Axis.AXIS_X) {
                        startDragXIfNeeded(ev);
                    } else if (touchAxis == Axis.AXIS_Y) {
                        startDragYIfNeeded(ev);
                    }
                }
                final float x = ev.getX(0);
                final float y = ev.getY(0);
                lastMotionPt.x = x;
                lastMotionPt.y = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }

                // 내가 캡쳐 했으면 true
                if (handleActionUp()) {
                    return true;
                } else {
                    v.performClick();
                }
                break;
            }
        }

        // 하위 뷰에게 전달하려면 false
        return false;
    }

    protected boolean handleActionUp() {
        return false;
    }

    //endregion
}
