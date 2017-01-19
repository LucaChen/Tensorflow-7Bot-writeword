package jp.narr.tensorflowmnist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import jp.narr.tensorflowmnist.Bluetooth.ServerOrCilent;
import jp.narr.tensorflowmnist.model.Arm7Bot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class chatActivity extends AppCompatActivity implements  OnClickListener,View.OnTouchListener {
	/** Called when the activity is first created. */
	//	private Button upButton,downButton,leftButton,rightButton;


	//  空间声明
	private static final String TAG = "chatActivity";

	private static final int PIXEL_WIDTH = 28;

	private TextView mResultText;

	private float mLastX;
	private float mLastY;

	private DrawModel mModel;
	private DrawView mDrawView;

	private PointF mTmpPiont = new PointF();

	private DigitDetector mDetector = new DigitDetector();
	private Button sendButton;
	private Button disconnectButton;
	private Button changemode;
	private Button motorButton;
	private Button catchButton,releaseButton;
	//private TextView command;
	private TextView xyz;
	private TextView vec67;
	private SeekBar moveZ;
	private SeekBar vec67X,vec67Y,vec67Z;
	private Button btn_send_1;
	private Button btn_send_2;
	private Button btn_send_3;
	int order=-1 ;




	/* 一些常量，代表服务器的名称 以及蓝牙变量 */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
	private BluetoothServerSocket mserverSocket = null;
	//private ServerThread startServerThread = null;
	private clientThread clientConnectThread = null;
	private BluetoothSocket socket = null;
	private BluetoothDevice device = null;
	private readThread mreadThread = null;;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public int move_X;
	public int move_Y;
	boolean flag=false;
	//机械手需要使用的变量及控制
	Arm7Bot arm7Bot=new Arm7Bot();



	private chatActivity mychatActivity;
	Context mContext;
	private RockerView myRock;
	private Handler updateUI;
	public Arm7Bot getArm7Bot(){
		return arm7Bot;
	}
	public int[] getPosition(){
		return arm7Bot.getPosition();
	}

	public static String bytesToHexString(byte[] bytes) {
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			String hexString = Integer.toHexString(bytes[i] & 0xFF);
			if(hexString.equals("0")){
				hexString="00";
			}
			if(hexString.length()==1){
				hexString = "0" + hexString;
			}
			result += hexString.toUpperCase()+"  ";
		}
		return result;
	}

	public void newPostion(){
		arm7Bot.newChange();
		sendMessageHandle(arm7Bot.getIK6());
		vec67.setText("x = "+vec67X.getProgress()+"\n"+"y = "+vec67Y.getProgress()+"\n"+"Z = "+vec67Z.getProgress()+"\n");
	}


	public boolean moveXY(int x,int y){
		int [] position=arm7Bot.getPosition();
		arm7Bot.getPosition()[0]=x;
		arm7Bot.getPosition()[1]=y;
		arm7Bot.change();
		if(arm7Bot.receiveIK6()==false){
			arm7Bot.getPosition()[0]=position[0];
			arm7Bot.getPosition()[1]=position[1];
			arm7Bot.change();
			myRock.setContext(mychatActivity);
			return false;
		}
		else{
			sendMessageHandle(arm7Bot.getIK6());
			//command.setText(bytesToHexString(arm7Bot.getIK6()));
			//xyz.setText("x = "+position[0]+"\n"+"y = "+position[1]+"\n"+"z = "+position[2]+"\n");
			//Log.d("Test",position[0]+"  "+position[1]+"  "+position[2]+"  ");
			//Log.d("Test",bytesToHexString(arm7Bot.getIK6()));
			return true;
		}
	}

	public boolean moveZ(int z){
		int [] position=arm7Bot.getPosition();
		arm7Bot.getPosition()[2]=z;
		arm7Bot.change();
		if(arm7Bot.receiveIK6()==false){
			arm7Bot.getPosition()[2]=position[2];
			arm7Bot.change();
			myRock.setContext(mychatActivity);
			return false;
		}
		else{
			sendMessageHandle(arm7Bot.getIK6());
			//command.setText(bytesToHexString(arm7Bot.getIK6()));
		//	xyz.setText("x = "+position[0]+"\n"+"y = "+position[1]+"\n"+"z = "+position[2]+"\n");
			Log.d("Test",position[0]+"  "+position[1]+"  "+position[2]+"  ");
			Log.d("Test",bytesToHexString(arm7Bot.getIK6()));
			return true;
		}
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_main);
		mContext = this;
		mychatActivity=this;
		boolean ret = mDetector.setup(this);
		if( !ret ) {
			Log.i(TAG, "Detector setup failed");
			return;
		}

		mModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

		mDrawView = (DrawView) findViewById(R.id.view_draw);
		mDrawView.setModel(mModel);
		mDrawView.setOnTouchListener(this);

		View detectButton = findViewById(R.id.button_detect);
		detectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDetectClicked();
			}
		});

		View clearButton = findViewById(R.id.button_clear);
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClicked();
			}
		});

		mResultText = (TextView)findViewById(R.id.text_result);
		init();
		hideNavigationBar();

	}
	private void hideNavigationBar() {
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
	}
	private void init() {
		motorButton=(Button)findViewById(R.id.btn_motor);
		motorButton.setOnClickListener(this);
		btn_send_1=(Button)findViewById(R.id.btn_send_1);
		btn_send_1.setOnClickListener(this);
		btn_send_2=(Button)findViewById(R.id.btn_send_2);
		btn_send_2.setOnClickListener(this);
		btn_send_3=(Button)findViewById(R.id.btn_send_3);
		btn_send_3.setOnClickListener(this);

//		changemode=(Button)findViewById(R.id.btn_mode2);
//		changemode.setOnClickListener(this);
	//	disconnectButton=(Button)findViewById(R.id.btn_disconnect);
	//	disconnectButton.setOnClickListener(this);
		sendButton= (Button)findViewById(jp.narr.tensorflowmnist.R.id.btn_msg_send);
		sendButton.setOnClickListener(this);
	//	releaseButton=(Button)findViewById(R.id.btn_release);
	//	releaseButton.setOnClickListener(this);
		//catchButton=(Button)findViewById(R.id.btn_catchs);
	//	catchButton.setOnClickListener(this);

//		/*****转轴方向测试*****/
//		vec67=(TextView)findViewById(R.id.vec67);
//		vec67X=(SeekBar)findViewById(R.id.vec67_x) ;
//		vec67Y=(SeekBar)findViewById(R.id.vec67_y);
//		vec67Z=(SeekBar)findViewById(R.id.vec67_z);
//		vec67Z.setMax(90);
//		vec67Y.setMax(500);
//		vec67X.setMax(500);
//		int[] temp=arm7Bot.getDirection();
//		vec67X.setProgress(temp[0]);
//		vec67Y.setProgress(temp[1]);
//		vec67Z.setProgress(temp[2]);
////		vec67X.setOnSeekBarChangeListener(this);
////		vec67Y.setOnSeekBarChangeListener(this);
////		vec67Z.setOnSeekBarChangeListener(this);
//
//
//
//		//xyz坐标及命令显示
//		xyz=(TextView)findViewById(R.id.textView_Positive);
//		//command=(TextView)findViewById(R.id.textView_Command);
//
//		//z轴移动
//		moveZ=(SeekBar)findViewById(R.id.seekBar);
//		moveZ.setMax(300);
//	//	moveZ.setOnSeekBarChangeListener(this);
//		moveZ.setProgress(arm7Bot.getPosition()[2]+50);
//		moveZ.setProgress(arm7Bot.getPosition()[2]+50);
//		//自定义移动控件myRock
//		myRock=(RockerView)findViewById(R.id.view) ;
//		myRock.setContext(this);


		//定义hangdle用来接收线程传来的传感器数据，并对数据进行处理，将其转换成16进制模式。
		 updateUI=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
//				if(msg.what==1){
//					Bundle temp=(Bundle)msg.obj;
//					int[] receiver=temp.getIntArray("receiver");
//					byte[] ttemp=new byte[receiver.length];
//					if(receiver.length==17){
//						for(int i=0;i<receiver.length;i++){
//							ttemp[i]=(byte)receiver[i];
//						}
//						arm7Bot.analysisReceived(receiver);
				//command.setText(bytesToHexString(ttemp));
				if (flag) {
					try {
						switch (order){
							case 0:{
								moveZ(92);
								Thread.sleep(3000);
								for(int i=0;i<=70;i=i+5){
									moveXY(i,175);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=175;i<=275;i=i+5){
									moveXY(70,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=70;i>=0;i=i-5){
									moveXY(i,275);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=275;i>=175;i=i-5){
									moveXY(0,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								moveZ(108);
								break;
							}
							case 1:{
								moveXY(70, 175);
								Thread.sleep(3000);
								moveZ(92);
								Thread.sleep(3000);
								for(int i=175;i<=275;i=i+5){
									moveXY(70,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(3000);
								moveZ(108);
								Thread.sleep(3000);
								moveXY(0,175);
								break;
							}
							case 2:{
								moveZ(92);
								Thread.sleep(3000);
								for(int i=0;i<=70;i=i+5){
									moveXY(i,175);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=175;i<=225;i=i+5){
									moveXY(70,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=70;i>=0;i=i-5){
									moveXY(i,225);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=225;i<=275;i=i+5){
									moveXY(0,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=0;i<=70;i=i+5){
									moveXY(i,275);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								moveZ(108);
								Thread.sleep(2000);
								moveXY(0,175);
							}
							case 3:{
								moveZ(92);
								Thread.sleep(3000);
								for(int i=0;i<=70;i=i+5){
									moveXY(i,175);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=175;i<=275;i=i+5){
									moveXY(70,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=70;i>=0;i=i-5){
									moveXY(i,275);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								moveZ(108);
								Thread.sleep(1000);
								moveXY(70,225);
								Thread.sleep(2000);
								moveZ(92);
								Thread.sleep(2000);
								for(int i=70;i>=0;i=i-5){
									moveXY(i,225);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								moveZ(100);
								Thread.sleep(2000);
								moveXY(0,175);
								break;
							}
							case 4:{
								moveZ(92);
								Thread.sleep(3000);
								for(int i=175;i<=225;i=i+5){
									moveXY(0,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=0;i<=70;i=i+5){
									moveXY(i,225);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								moveZ(108);
								Thread.sleep(1000);
								moveXY(70,175);
								Thread.sleep(2000);
								moveZ(92);
								for(int i=175;i<=275;i=i+5){
									moveXY(70,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(3000);
								moveZ(108);
								Thread.sleep(3000);
								moveXY(0,175);
								break;
							}
							case 5:{
								moveXY(70, 175);
								Thread.sleep(3000);
								moveZ(92);
								Thread.sleep(3000);
								for(int i=70;i>=0;i=i-5){
									moveXY(i,175);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=175;i<=225;i=i+5){
									moveXY(0,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=0;i<=70;i=i+5){
									moveXY(i,225);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=225;i<=275;i=i+5){
									moveXY(70,i);
									moveZ(92+(i-175)/10);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								for(int i=70;i>=0;i=i-5){
									moveXY(i,275);
									Thread.sleep(150);
								}
								Thread.sleep(2000);
								moveZ(108);
								Thread.sleep(3000);
								moveXY(0,175);
								break;
							}
						}
						flag=false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
	}



//	@Override
//	public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//		if(seekBar.getId()==moveZ.getId()){
//			int[] position=arm7Bot.getPosition();
//			progress=progress-50;
//			arm7Bot.getPosition()[2]=progress;
//			moveZ(progress);
//		}
//		else if(seekBar.getId()==vec67X.getId()){
//			int[] temp=arm7Bot.getDirection();
//			progress=progress-250;
//			arm7Bot.getDirection()[0]=progress;
//			newPostion();
//		}
//		else if(seekBar.getId()==vec67Y.getId()){
//			int[] temp=arm7Bot.getDirection();
//			progress=progress-250;
//			arm7Bot.getDirection()[1]=progress;
//			newPostion();
//		}
//		else if(seekBar.getId()==vec67Z.getId()){
//			int lengh=1000;
//			//按角度移动机械手
//			if(progress<45){
//				arm7Bot.getDirection()[0]= Math.abs((int)(lengh* Math.cos(((45-progress)* Math.PI)/180)));
//				arm7Bot.getDirection()[1]=-Math.abs((int)(lengh* Math.sin(((45-progress)* Math.PI)/180)));
//			}
//			else{
//				arm7Bot.getDirection()[0]= Math.abs((int)(lengh* Math.cos(((progress-45)* Math.PI)/180)));
//				arm7Bot.getDirection()[1]= Math.abs((int)(lengh* Math.sin(((progress-45)* Math.PI)/180)));
//			}
//			Log.d("what",arm7Bot.getDirection()[0]+"  "+arm7Bot.getDirection()[1]);
//			newPostion();
//
//		}
//	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;

		if (action == MotionEvent.ACTION_DOWN) {
			processTouchDown(event);
			return true;

		} else if (action == MotionEvent.ACTION_MOVE) {
			processTouchMove(event);
			return true;

		} else if (action == MotionEvent.ACTION_UP) {
			processTouchUp();
			return true;
		}
		return false;
	}

	private void processTouchDown(MotionEvent event) {
		mLastX = event.getX();
		mLastY = event.getY();
		mDrawView.calcPos(mLastX, mLastY, mTmpPiont);
		float lastConvX = mTmpPiont.x;
		float lastConvY = mTmpPiont.y;
		mModel.startLine(lastConvX, lastConvY);
	}

	private void processTouchMove(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		mDrawView.calcPos(x, y, mTmpPiont);
		float newConvX = mTmpPiont.x;
		float newConvY = mTmpPiont.y;
		mModel.addLineElem(newConvX, newConvY);

		mLastX = x;
		mLastY = y;
		mDrawView.invalidate();
	}

	private void processTouchUp() {
		mModel.endLine();
	}

	private void onDetectClicked() {
		int pixels[] = mDrawView.getPixelData();
		int digit = mDetector.detectDigit(pixels);
		Log.i(TAG, "digit =" + digit);
		order=digit;
		flag=true;
		mResultText.setText("Detected = " + digit);
	}



	private void onClearClicked() {
		mModel.clear();
		mDrawView.reset();
		mDrawView.invalidate();

		mResultText.setText("");
	}

	@Override
	public synchronized void onPause() {
		mDrawView.onPause();
		super.onPause();
	}

	@Override
	public synchronized void onResume() {
		/**
		 * 设置为横屏
		 */
		if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		mDrawView.onResume();
		super.onResume();

		if(Bluetooth.isOpen)
		{
			Toast.makeText(mContext, "连接已经打开，可以通信。如果要再建立连接，请先断开！", Toast.LENGTH_SHORT).show();
			return;
		}
		if(Bluetooth.serviceOrCilent==ServerOrCilent.CILENT)
		{
			String address = Bluetooth.BlueToothAddress;
			if(!address.equals("null"))
			{
				device = mBluetoothAdapter.getRemoteDevice(address);
				clientConnectThread = new clientThread();
				clientConnectThread.start();
				Bluetooth.isOpen = true;
			}
			else
			{
				Toast.makeText(mContext, "address is null !", Toast.LENGTH_SHORT).show();
			}
		}
		else if(Bluetooth.serviceOrCilent==ServerOrCilent.SERVICE)
		{
//			startServerThread = new ServerThread();
//			startServerThread.start();
			Bluetooth.isOpen = true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (Bluetooth.serviceOrCilent == ServerOrCilent.CILENT)
		{
			shutdownClient();
		}
		else if (Bluetooth.serviceOrCilent == ServerOrCilent.SERVICE)
		{
			//	shutdownServer();
		}
		Bluetooth.isOpen = false;
		Bluetooth.serviceOrCilent = ServerOrCilent.NONE;
	}
	private void messageShow(byte[] message){
		int[] position=arm7Bot.getPosition();
		//command.setText(bytesToHexString(message));
		xyz.setText("x = "+position[0]+"\n"+"y = "+position[1]+"\n"+"z = "+position[2]+"\n");
		Log.d("Test",position[0]+"  "+position[1]+"  "+position[2]+"  ");
		Log.d("Test",bytesToHexString(arm7Bot.getIK6()));
	}

	@Override
	public void onClick(View view) {
		if(view.getId()==R.id.btn_mode2){
					// TODO Auto-generated method stub
					byte[] test={(byte)0xfe,(byte)0xf5,0x02};
					sendMessageHandle(arm7Bot.getStatus());
					messageShow(test);
		}
		else if(view.getId()==R.id.btn_catch){
			arm7Bot.getIK6()[20]=0;
			arm7Bot.getIK6()[21]=0;
			int[] postion=arm7Bot.getPosition();
			moveXY(postion[0],postion[1]);
		}
		else if(view.getId()==R.id.btn_release){
			arm7Bot.getIK6()[20]=0x01;
			arm7Bot.getIK6()[21]=0x48;
			int[] postion=arm7Bot.getPosition();
			moveXY(postion[0],postion[1]);
		}
		else if(view.getId()==R.id.btn_send_1){
			//int[] postion=arm7Bot.getPosition();
			order=1;
			flag=true;
			//Toast.makeText(mContext, "Message is send!", Toast.LENGTH_SHORT).show();

		}
		else if(view.getId()==R.id.btn_send_2){
			int[] postion=arm7Bot.getPosition();
			postion[0]=0;
			postion[1]=300;
			moveXY(postion[0],postion[1]);
		}
		else if(view.getId()==R.id.btn_send_3){
			int[] postion=arm7Bot.getPosition();
			postion[0]=100;
			postion[1]=300;
			moveXY(postion[0],postion[1]);
		}
		else if(view.getId()==R.id.btn_msg_send){
			byte[] test={(byte)0xfe,(byte)0xFA,0x08,0x00,0x01,0x2F,0x00,0x64,0x08,0x00,0x08,0x00,0x09,0x44,0x01,0x48,0x01,0x48,0x01,0x48,0x01,0x48};
			arm7Bot.setIK6(test);
			sendMessageHandle(test);
			int[] temp={0,175,100};
			arm7Bot.setPosition(temp);
		//	myRock.setContext(mychatActivity);
		//	messageShow(arm7Bot.getIK6());
		}
		else if(view.getId()==R.id.btn_disconnect){
			if (Bluetooth.serviceOrCilent == ServerOrCilent.CILENT)
			{
				shutdownClient();
			}
			Bluetooth.isOpen = false;
			Bluetooth.serviceOrCilent=ServerOrCilent.NONE;
			Toast.makeText(mContext, "已断开连接！", Toast.LENGTH_SHORT).show();
		}
		else if(view.getId()==R.id.btn_motor){
			//command.setText(bytesToHexString(arm7Bot.getMotorPosition()));
			sendMessageHandle(arm7Bot.getMotorPosition());
		}


	}


	/*-------------------------------------------蓝牙相关--------------------------------------------------*/
	//开启客户端
	private class clientThread extends Thread {
		public void run() {
			try {
				//创建一个Socket连接：只需要服务器在注册时的UUID号
				// socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);
				socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

				//连接
				Message msg2 = new Message();
				msg2.obj = "请稍候，正在连接服务器:"+Bluetooth.BlueToothAddress;
				msg2.what = 0;
				//LinkDetectedHandler.sendMessage(msg2);

				socket.connect();

				Message msg = new Message();
				msg.obj = "已经连接上服务端！可以发送信息。";
				msg.what = 0;
				//LinkDetectedHandler.sendMessage(msg);
				//启动接受数据
				mreadThread = new readThread();
				mreadThread.start();
			}
			catch (IOException e)
			{
				Log.e("connect", "", e);
				Message msg = new Message();
				msg.obj = "连接服务端异常！断开连接重新试一试。";
				msg.what = 0;
				//LinkDetectedHandler.sendMessage(msg);
			}
		}
	};



	/* 停止客户端连接 */
	private void shutdownClient() {
		new Thread() {
			public void run() {
				if(clientConnectThread!=null)
				{
					clientConnectThread.interrupt();
					clientConnectThread= null;
				}
				if(mreadThread != null)
				{
					mreadThread.interrupt();
					mreadThread = null;
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					socket = null;
				}
			};
		}.start();
	}
	//发送数据
	private void sendMessageHandle(byte[] msg)
	{
		if (socket == null)
		{
			Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			OutputStream os = socket.getOutputStream();
			os.write(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//list.add(new deviceListItem(msg, false));
		//mAdapter.notifyDataSetChanged();
		//mListView.setSelection(list.size() - 1);
	}


	//读取数据
	private class readThread extends Thread {
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
			InputStream mmInStream = null;

			try {
				mmInStream = socket.getInputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (true) {
				try {
					// Read from the InputStream
					if( (bytes = mmInStream.read(buffer)) > 0 )
					{
						int[] buf_data = new int[bytes];
						for(int i=0; i<bytes; i++)
						{
							buf_data[i] = buffer[i];
						}

						Bundle data=new Bundle();
						data.putIntArray("receiver",buf_data);
						Message msg = new Message();
						msg.obj = data;
						msg.what = 1;
						updateUI.sendMessage(msg);
					}
				} catch (IOException e) {
					try {
						mmInStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}





	public class SiriListItem {
		String message;
		boolean isSiri;

		public SiriListItem(String msg, boolean siri) {
			message = msg;
			isSiri = siri;
		}
	}




	public class deviceListItem {
		String message;
		boolean isSiri;

		public deviceListItem(String msg, boolean siri) {
			message = msg;
			isSiri = siri;
		}
	}
}