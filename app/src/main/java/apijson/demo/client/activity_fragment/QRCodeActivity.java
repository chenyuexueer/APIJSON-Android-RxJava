/*Copyright ©2016 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package apijson.demo.client.activity_fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.zxing.encoding.EncodingHandler;

import apijson.demo.client.R;
import apijson.demo.client.model.User;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import zuo.biao.library.base.BaseActivity;
import zuo.biao.library.interfaces.OnBottomDragListener;
import zuo.biao.library.manager.CacheManager;
import zuo.biao.library.util.ImageLoaderUtil;
import zuo.biao.library.util.JSON;
import zuo.biao.library.util.Log;
import zuo.biao.library.util.StringUtil;

/**二维码界面Activity
 * @author Lemon
 */
public class QRCodeActivity extends BaseActivity implements OnBottomDragListener {
	private static final String TAG = "QRCodeActivity";

	//启动方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	/**启动这个Activity的Intent
	 * @param context
	 * @param userId
	 * @return
	 */
	public static Intent createIntent(Context context, long userId) {
		return new Intent(context, QRCodeActivity.class).
				putExtra(INTENT_ID, userId);
	}

	//启动方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Override
	public Activity getActivity() {
		return this;
	}

	private long userId = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qrcode_activity, this);

		intent = getIntent();
		userId = intent.getLongExtra(INTENT_ID, userId);

		//功能归类分区方法，必须调用<<<<<<<<<<
		initView();
		initData();
		initEvent();
		//功能归类分区方法，必须调用>>>>>>>>>>

	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	private ImageView ivQRCodeHead;
	private TextView tvQRCodeName;

	private ImageView ivQRCodeCode;
	private View pbQRCode;
	@Override
	public void initView() {//必须调用
		autoSetTitle();

		ivQRCodeHead = (ImageView) findViewById(R.id.ivQRCodeHead);
		tvQRCodeName = (TextView) findViewById(R.id.tvQRCodeName);

		ivQRCodeCode = (ImageView) findViewById(R.id.ivQRCodeCode);
		pbQRCode = findViewById(R.id.pbQRCode);
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


//	private User user;
	@Override
	public void initData() {//必须调用

//		pbQRCode.setVisibility(View.VISIBLE);
//		runThread(TAG + "initData", new Runnable() {
//
//			@Override
//			public void run() {
//
//				user = CacheManager.getInstance().get(User.class, "" + userId);
//				if (user == null) {
//					user = new User(userId);
//				}
//				runUiThread(new Runnable() {
//					@Override
//					public void run() {
//						ImageLoaderUtil.loadImage(ivQRCodeHead, user.getHead());
//						tvQRCodeName.setText(StringUtil.getTrimedString(
//								StringUtil.isNotEmpty(user.getName(), true)
//								? user.getName() : user.getPhone()));
//					}
//				});
//
//				setQRCode(user);
//			}
//		});

		//使用RxJava <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		Observable.create(new ObservableOnSubscribe<User>() {
			@Override
			public void subscribe(ObservableEmitter<User> oe) throws Exception {
				Log.d(TAG, "setQRCode  Observable.create.subscribe  >> currentThread = " + Thread.currentThread().getName());
				User user = CacheManager.getInstance().get(User.class, "" + userId);

				oe.onNext(user);

				try {//如果用onNext接收就不要用全局变量
					qRCodeBitmap = EncodingHandler.createQRCode(JSON.toJSONString(user)
							, (int) (2 * getResources().getDimension(R.dimen.qrcode_size)));
				} catch (WriterException e) {
					e.printStackTrace();
					Log.e(TAG, "initData  try {Bitmap qrcode = EncodingHandler.createQRCode(contactJson, ivQRCodeCode.getWidth());" +
							" >> } catch (WriterException e) {" + e.getMessage());
				}

				oe.onComplete();
			}
		}).observeOn(AndroidSchedulers.mainThread()).doOnNext(new Consumer<User>() {
			@Override
			public void accept(User user) throws Exception {
				Log.d(TAG, "setQRCode  Observable.doOnNext  user " + (user == null ? "=" : "!=") + " null"
						+ ">> currentThread = " + Thread.currentThread().getName());

				if (user == null) {
					user = new User(userId);
				}

				ImageLoaderUtil.loadImage(ivQRCodeHead, user.getHead());
				tvQRCodeName.setText(StringUtil.getTrimedString(
						StringUtil.isNotEmpty(user.getName(), true)
								? user.getName() : user.getPhone()));
			}
		}).observeOn(AndroidSchedulers.mainThread()).doOnComplete(new Action() {
			@Override
			public void run() throws Exception {
				Log.d(TAG, "setQRCode  Observable.doOnCompleted  qRCodeBitmap " + (qRCodeBitmap == null ? "=" : "!=") + " null"
						+ ">> currentThread = " + Thread.currentThread().getName());

				pbQRCode.setVisibility(View.GONE);
				ivQRCodeCode.setImageBitmap(qRCodeBitmap);
			}
		}).doOnSubscribe(new Consumer<Disposable>() {
			@Override
			public void accept(Disposable disposable) throws Exception {
				pbQRCode.setVisibility(View.VISIBLE);
			}
		}).subscribeOn(Schedulers.io()).subscribe();

		//使用RxJava >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	}

	private Bitmap qRCodeBitmap;//如果用onNext接收就不要用全局变量
//	protected void setQRCode(User user) {
//		if (user == null) {
//			Log.e(TAG, "setQRCode  user == null" +
//					" || StringUtil.isNotEmpty(user.getPhone(), true) == false >> return;");
//			return;
//		}
//
//		try {
//			qRCodeBitmap = EncodingHandler.createQRCode(JSON.toJSONString(user)
//					, (int) (2 * getResources().getDimension(R.dimen.qrcode_size)));
//		} catch (WriterException e) {
//			e.printStackTrace();
//			Log.e(TAG, "initData  try {Bitmap qrcode = EncodingHandler.createQRCode(contactJson, ivQRCodeCode.getWidth());" +
//					" >> } catch (WriterException e) {" + e.getMessage());
//		}
//
//		runUiThread(new Runnable() {
//			@Override
//			public void run() {
//				pbQRCode.setVisibility(View.GONE);
//				ivQRCodeCode.setImageBitmap(qRCodeBitmap);
//			}
//		});
//	}

	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//Event事件区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void initEvent() {//必须调用

	}

	//系统自带监听方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void onDragBottom(boolean rightToLeft) {
		if (rightToLeft) {

			return;
		}

		finish();
	}



	//类相关监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	@Override
	protected void onDestroy() {
		super.onDestroy();

		pbQRCode = null;
		ivQRCodeCode = null;
//		user = null;

		if (qRCodeBitmap != null) {
			if (qRCodeBitmap.isRecycled() == false) {
				qRCodeBitmap.recycle();
			}
			qRCodeBitmap = null;
		}
		if (ivQRCodeCode != null) {
			ivQRCodeCode.setImageBitmap(null);
			ivQRCodeCode = null;
		}
	}


	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}