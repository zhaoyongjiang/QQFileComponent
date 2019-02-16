package cn.xxt.commons.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.xxt.commons.R;


/**
 * @author Lu
 * @Data 2014-8-6 上午10:49:16
 */
public class XXTDialog extends Dialog {

	/** 标题控件 **/
	private TextView tvTitle;

	/** 提示语控件 **/
	private TextView tvTip;

	/** 确定按钮控件 **/
	private Button btnConfirm;

	/** 取消按钮控件 **/
	private Button btnCancel;

	private ImageView ivClose;

	private OnXxtDialogBtnClickListener listener;

	private Context context;

	public interface OnXxtDialogBtnClickListener {
		/**
		 * 确认按钮点击事件
		 */
		void onBtnConfirmClick();

		/**
		 * 取消按钮点击事件
		 */
		void onBtnCancelClick();
	}

	/**
	 *  构造函数
	 */
	public XXTDialog(Context context) {
		super(context, R.style.dialogDim);

		this.context = context;

		setContentView(R.layout.xxt_dialog);

		tvTitle = (TextView)findViewById(R.id.tv_title);
		tvTip = (TextView)findViewById(R.id.tv_tip);

		btnConfirm = (Button)findViewById(R.id.btn_confirm);
		btnCancel = (Button)findViewById(R.id.btn_cancel);

		ivClose = (ImageView)findViewById(R.id.iv_close);

		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if(listener!=null){
					listener.onBtnConfirmClick();
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if(listener!=null){
					listener.onBtnCancelClick();
				}
			}
		});

		ivClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if(listener!=null){
					listener.onBtnCancelClick();
				}
			}
		});
	}

	@Override
	public void show() {
		try {
			super.show();
		} catch (Exception e) { //捕获窗口不存在的异常
		}
	}


	/**
	 * 设置标题
	 *
	 * @param title 标题
	 */
	public void setDialogTitle(String title)
	{
		if(title!=null){
			tvTitle.setText(title);
		}
	}

	/**
	 * 设置提示语
	 *
	 * @param tip 提示语
	 */
	public void setTip(String tip)
	{
		if(tip!=null){
			tvTip.setText(tip);
		}
	}

	/**
	 * 设置带html标签的提示语
	 *
	 * @param tip 提示语
	 */
	public void setHtmlTip(String tip)
	{
		if(tip!=null){
			tvTip.setText(Html.fromHtml(tip));
		}
	}

	/**
	 * 配置确定按钮，默认显示、按钮文字为确定
	 *
	 * @param text 按钮文字
	 * @param visibility 是否隐藏等
	 */
	public void setBtnConfirm(String text,int visibility)
	{
		if(text!=null){
			btnConfirm.setText(text);
		}
		btnConfirm.setVisibility(visibility);
	}

	/**
	 * 配置取消按钮，默认不显示、按钮文字为取消
	 *
	 * @param text 按钮文字
	 * @param visibility 是否隐藏等
	 */
	public void setBtnCancel(String text,int visibility)
	{
		if(text!=null){
			btnCancel.setText(text);
		}
		btnCancel.setVisibility(visibility);
	}

	/**
	 * 设置左上角X号的显示
	 * @param visibility 是否隐藏
	 */
	public void setIvCloseVisibility(int visibility) {
		ivClose.setVisibility(visibility);
	}

	/**
	 * 设置监听器
	 *
	 * @param listener 监听器，用于处理用户点击事件
	 */
	public void setOnXxtDialogBtnClickListener(OnXxtDialogBtnClickListener listener)
	{
		if(listener!=null){
			this.listener = listener;
		}
	}

}
