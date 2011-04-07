package net.ahyane.javacodeviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.EditText;

public class EditCodeView extends EditText{
	ViewGroup mViewGroup = null;
	
	public void setParentView(ViewGroup viewGroup){
		mViewGroup = viewGroup;
	}
	
	public EditCodeView(Context context) {
		super(context);
	}

	public EditCodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}	

	public EditCodeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int width = mViewGroup.getMeasuredWidth();
		int height = mViewGroup.getMeasuredHeight();
		
		if(width < w) width = w;
		if(height < h) height = h;

		setMinimumWidth(width);
		setMinimumHeight(height);
		
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
