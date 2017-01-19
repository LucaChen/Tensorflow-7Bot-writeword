package jp.narr.tensorflowmnist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import jp.narr.tensorflowmnist.chatActivity;

public class RockerView extends ImageView {
    private chatActivity mychatActivity;
	private MyRocker rocker ;//define custom rocker
	private float current_X, current_Y;
	private boolean isRockerCreated = false;//when rocker is created, set true, else set false
	private float centerX,centerY;
	private float maxX,maxY;
	private int botX=0,botY=0;
	private int i=0;
	public RockerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RockerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    public void setContext(chatActivity myContext){
        mychatActivity=myContext;
		botX=mychatActivity.getPosition()[0];
		botY=mychatActivity.getPosition()[1];
    }



	@Override
	protected void onDraw(Canvas canvas) {
		if(!isRockerCreated){
			rocker = new MyRocker(getWidth(), getHeight());
			isRockerCreated = true;
		}
		rocker.drawSelf(canvas);
		rocker.drawLine(canvas);
		super.onDraw(canvas);
		centerX=getWidth()/2;
		centerY=getHeight()/2;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		maxX=current_X = event.getX();
		maxY=current_Y = event.getY();
		float changeX,changeY;
		if(maxX>500)
			maxX=500;
		if(maxX<100)
			maxX=100;
		if(maxY>500)
			maxY=500;
		if(maxY<100)
			maxY=100;


		changeX=maxX-centerX;
		changeY=maxY-centerY;
		botX=botX-(int)((changeX/200)*3);
		botY=botY+(int)((-changeY/200)*3);

		if(mychatActivity.moveXY(botX,botY)){

		}
		else{
			botX=botX+(int)((changeX/200)*3);
			botY=botY-(int)((-changeY/200)*3);
		}



		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			rocker.begin(current_X, current_Y);
			postInvalidate();//refresh
			break;

		case MotionEvent.ACTION_MOVE:
			rocker.update(current_X, current_Y);
			postInvalidate();//refresh
			break;

		case MotionEvent.ACTION_UP:
			rocker.reset();
			postInvalidate();//refresh
			break;

		default:
			break;
		}
		return true;
	}

}
