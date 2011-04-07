package net.ahyane.javacodeviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class CodeViewerActivity extends Activity {	
	private static String TAG = "CodeViewerActivity"; 
	
	private static final int update_span_index = 1024;
	
	private static final int num_a = 97;
	private static final int num_z = 122;
	private static final int num_A = 65;
	private static final int num_Z = 90;
	private static final int num_0 = 48;
	private static final int num_9 = 57;
	private static final int num_dot = '.';
	private static final int num_underbar = '_';

	private static final int notfound_bgcolor = 0x00ffffff;
	private static final int found_bgcolor = 0xffc8c800; 
	private static final int selectedfound_bgcolor = 0xff00c8ff; 

	private boolean isNew = true;
	
    private Uri mFileURI = null;
    private StringBuffer mStringBuffer = null;
    private EditCodeView mCodeView = null;
//    private View mBlank = null;
    private ScrollView mScrollView = null;
    private LinearLayout mFindLayout = null;
    private LinearLayout mInfoLayout = null;
    private Spannable mSpan = null;
    private ArrayList<Integer> mFoundIndexList = null;
    private int mFoundIndexListPointer = 0;

    private static final int MSG_TYPE_SET_TITLE = 0;
    private static final int MSG_TYPE_SET_TEXT = 1;
    private static final int MSG_TYPE_BEGIN_SPAN = 2;
    private static final int MSG_TYPE_SPAN_KEYWORD = 3;
    private static final int MSG_TYPE_SPAN_VALUE = 4;
    private static final int MSG_TYPE_SPAN_COMMENT = 5;
    private static final int MSG_TYPE_COMPLETE_SPAN = 6;
    private static final int MSG_TYPE_PROGRESS_ON = 7;
    private static final int MSG_TYPE_PROGRESS_OFF = 8;
    private static final int MSG_TYPE_PROGRESS_SET = 9;
    private static final int MSG_TYPE_PROGRESS_BASE_INCREASE = 10;
    private static final int MSG_TYPE_FADE_OUT_INFO = 11;
    
    private static final String ENCODING_DEFAULT = "(default)";
    
    //add encoding
    private static final String sEncodingList[] = {
    	ENCODING_DEFAULT,
    	"MS949",
    	"EUC-JP",
    	"EUC-KR",
    	"US-ASCII",
    	"UTF-8",
    	"UTF-16",
    };

    //add encoding
    private static String mTextEncording = null;
    
    //add encoding
    private String getDefaultEncoding(){
    	Locale defaultLocale = Locale.getDefault();
    	
//    	if(defaultLocale.equals(Locale.KOREA)){
//    		mTextEncording = sEncodingList[1];
//    	}else if(defaultLocale.equals(Locale.KOREAN)){
//    		mTextEncording = sEncodingList[1];
//    	}else if(defaultLocale.equals(Locale.JAPAN)){
//    		mTextEncording = sEncodingList[6];
//    	}else if(defaultLocale.equals(Locale.JAPANESE)){
//    		mTextEncording = sEncodingList[6];
//    	}else if(defaultLocale.equals(Locale.CHINA)){
//    		mTextEncording = sEncodingList[7];
//    	}else if(defaultLocale.equals(Locale.CHINESE)){
//    		mTextEncording = sEncodingList[7];
//    	}else{
    		mTextEncording = sEncodingList[0];
//    	}
    	Log.i(TAG, "Default encoding is \"" + mTextEncording + "\" for " + "\"" + defaultLocale.toString() + "\"");
    	return mTextEncording;
    }
	
    Handler mHandler = new Handler(){
    	int baseValue = 0;
    	
    	@Override
		public void handleMessage(Message msg) {
    		final StyleSpan spanKeywordBold = new StyleSpan(Typeface.BOLD);
    		final ForegroundColorSpan spanKeywordColor = new ForegroundColorSpan(JavaKeyword.keywords_color);
    		final ForegroundColorSpan spanValueColor = new ForegroundColorSpan(JavaKeyword.value_color);
    		final ForegroundColorSpan spanCommentColor = new ForegroundColorSpan(JavaKeyword.comment_color);
    		
			switch(msg.what){
				case MSG_TYPE_SET_TITLE:
			        String titleText = null;
		        	//String path = mFileURI.getEncodedPath();
		        	String path = mFileURI.getPath();
		        	titleText = path.substring(path.lastIndexOf('/') + 1) + " - [" + path + "]";
		        	setTitle(titleText);
					break;
				case MSG_TYPE_SET_TEXT:
					mCodeView.setText(mStringBuffer.toString());
					break;
				case MSG_TYPE_BEGIN_SPAN:
					mSpan = (Spannable) mCodeView.getText();
					break;
				case MSG_TYPE_SPAN_KEYWORD:
					mSpan.setSpan(spanKeywordBold, msg.arg1, msg.arg2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					mSpan.setSpan(spanKeywordColor, msg.arg1, msg.arg2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					break;
				case MSG_TYPE_SPAN_VALUE:
					mSpan.setSpan(spanValueColor, msg.arg1, msg.arg2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					break;
				case MSG_TYPE_SPAN_COMMENT:
					mSpan.setSpan(spanCommentColor, msg.arg1, msg.arg2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					break;
				case MSG_TYPE_COMPLETE_SPAN:
					mCodeView.setSelection(mStringBuffer.length() - 1);
					mCodeView.setSelection(0);
					break;
				case MSG_TYPE_PROGRESS_ON:
		            setProgressBarIndeterminateVisibility(true);
					setProgressBarVisibility(true);
					mCodeView.setFocusable(false);
					mCodeView.setFocusableInTouchMode(false);
					baseValue = 0;
					break;
				case MSG_TYPE_PROGRESS_OFF:
		            setProgressBarIndeterminateVisibility(false);
					setProgressBarVisibility(false);
					mCodeView.setFocusable(true);
					mCodeView.setFocusableInTouchMode(true);
					break;
				case MSG_TYPE_PROGRESS_SET:
					if(baseValue + msg.arg1 < 10000){setProgress(baseValue + msg.arg1);}
					else{setProgress(10000);}
					break;
				case MSG_TYPE_PROGRESS_BASE_INCREASE:
					baseValue += 1000;
					setProgress(baseValue);
					break;
				case MSG_TYPE_FADE_OUT_INFO:
					Animation fadeOutAnimation = AnimationUtils.loadAnimation(CodeViewerActivity.this, R.anim.fade_out);
					fadeOutAnimation.setAnimationListener(new AnimationListener(){
							public void onAnimationEnd(Animation animation) {
								mInfoLayout.setVisibility(View.GONE);	
							}
							public void onAnimationRepeat(Animation animation) {}
							public void onAnimationStart(Animation animation) {}
						}
					);
			        mInfoLayout.setAnimation(fadeOutAnimation);
					break;
				default:
					super.handleMessage(msg);
			}
		}
    };
    
    Runnable mCodeLoaderRunnable = new Runnable(){
		synchronized public void run() {
	        //File file = new File(mFileURI.getEncodedPath());
	        File file = new File(mFileURI.getPath());
	        if(!file.exists()){
	        	return;
	        }
	        
	        //set title & title progress
            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SET_TITLE));
            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_ON));
            
            //dim streams & buffer
	        FileInputStream fileInputStream = null;
	        BufferedReader bufferedReader = null;
	        mStringBuffer = new StringBuffer("");
	        String lineBuffer = null;
	        int loadedLength = 0;
	        
	        try {
				fileInputStream = new FileInputStream(file);
				//decoder
				if(mTextEncording == null){
					mTextEncording = getDefaultEncoding();
				}
				if(mTextEncording != null && !mTextEncording.equals(ENCODING_DEFAULT)){
					bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, mTextEncording));
				}else{
					bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
				}
				boolean preloaded = false;
				
	            while(true){
	            	if(lineBuffer != null){
	            		mStringBuffer.append(lineBuffer);
		            	if((lineBuffer = bufferedReader.readLine()) == null){
		            		break;
		            	}
	            		if(lineBuffer != null){
		            		mStringBuffer.append("\n");
		            	}
	            	}else{
		            	if((lineBuffer = bufferedReader.readLine()) == null){
		            		break;
		            	}
	            	}
	        		loadedLength += lineBuffer.length();
	            	
            		if(preloaded == false){
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SET_TEXT));
			            preloaded = true;
	            		//progress increase
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_BASE_INCREASE));
			            //sleep
		            	try{Thread.sleep(200);}
		            	catch(InterruptedException e1){e1.printStackTrace();}
            		}
	            }
	            
//	            mStringBuffer = new StringBuffer(
//	            		new String(mStringBuffer.toString().getBytes("utf-8"), "ANSI")
//	            	);
	            
	            //is empty?
	            if(loadedLength > 0){
			        //set text
		            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SET_TEXT));
            		
		            //progress increase
		            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_BASE_INCREASE));
		            
	            	try{Thread.sleep(50);}
	            	catch(InterruptedException e1){e1.printStackTrace();}
	            	
			        //begin span
		            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_BEGIN_SPAN));
					
		            //set spanning
		            setCodeSpannings();

		    		//complete new span
		            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_COMPLETE_SPAN));
	            }
	            
	            //set title progress
	            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_OFF));
	            
	            if(isNew == true){
	            	isNew = false;
	            	mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_FADE_OUT_INFO));
	            }
	            
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				try{
					if(bufferedReader != null)bufferedReader.close();
					if(fileInputStream != null)fileInputStream.close();
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		}
    };
    
//    private Runnable mSpanningRunnable = new Runnable(){
//		synchronized public void run() {
//			mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_ON));
//			//set spanning
//            setCodeSpannings();
//            
//            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_OFF));
//		}
//    };
    
    private Thread mThread = null;
//    private Thread mSpanningThread = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.main);
        
        mCodeView = (EditCodeView) findViewById(R.id.main_edittext01);
//        mBlank = (View) findViewById(R.id.main_blank);
        mFindLayout = (LinearLayout) findViewById(R.id.main_findlayout);
        ImageButton prevFindButton = (ImageButton) findViewById(R.id.main_button01);
        ImageButton findokButton = (ImageButton) findViewById(R.id.main_button02);
        ImageButton nextFindButton = (ImageButton) findViewById(R.id.main_button03);
        prevFindButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				findPrevText();
			}
        });
        findokButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				resetFoundList();
			}
        });
        nextFindButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				findNextText();
			}
        });

        //scrollview
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mCodeView.setParentView(mScrollView);
        
        
        //info layout
        mInfoLayout = (LinearLayout) findViewById(R.id.main_infolayout);

        //no texteditor focus
        //((Button) findViewById(R.id.btn_none)).requestFocus();
        
        //url
        mFileURI = getIntent().getData();
        if(mFileURI != null){
            mThread = new Thread(mCodeLoaderRunnable);
            mThread.start();
        }else{
        	Toast.makeText(this, R.string.cannot_load, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		//set find function
//    	mBlank.setLayoutParams(
//    		new LinearLayout.LayoutParams(
//    			LinearLayout.LayoutParams.WRAP_CONTENT,
//    			getWindowManager().getDefaultDisplay().getHeight() / 2
//    		)
//    	);
		super.onResume();
	}

	private void showDialogFindText(){
    	final LinearLayout dialog_findtext = (LinearLayout)View.inflate(this, R.layout.dialog_findtext, null);
    	
    	new AlertDialog.Builder(this)
		.setTitle(R.string.menu_findtext)
		.setView(dialog_findtext)
		.setPositiveButton(R.string.dlg_button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText edittext_findtext = (EditText)dialog_findtext.findViewById(R.id.dialog_findtext_edittext01);
				String string_findtext = edittext_findtext.getText().toString();
				if(string_findtext.equals("")){
					Toast.makeText(CodeViewerActivity.this, R.string.toast_cannotfind_space, 4000).show();
				}else{
					int found_count = setFindTextSpanning(string_findtext);
					if(found_count == 0){
						mFindLayout.setVisibility(View.INVISIBLE);
						
						Toast.makeText(CodeViewerActivity.this, R.string.toast_notfound, 4000).show();
					}else{
						mFindLayout.setVisibility(View.VISIBLE);
						if(mFoundIndexList.get(1) + mFoundIndexList.get(0) < mStringBuffer.length()){
							mCodeView.setSelection(mFoundIndexList.get(1), mFoundIndexList.get(1) + mFoundIndexList.get(0));
						}else{
							mCodeView.setSelection(mFoundIndexList.get(1), mStringBuffer.length() - 1);
						}
						mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(1), mFoundIndexList.get(1) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						Toast.makeText(CodeViewerActivity.this, found_count + ((Context)CodeViewerActivity.this).getString(R.string.toast_found), 4000).show();
					}
				}
			}
		})
		.setNegativeButton(R.string.dlg_button_cancel, null)
		.show();	
    }
    
    private void showDialogTextSize(){
		final LinearLayout dialog_textsize = (LinearLayout)View.inflate(this, R.layout.dialog_textsize, null);
		final SeekBar scroll_textsize = (SeekBar)dialog_textsize.findViewById(R.id.dialog_textsize_seekbar01);
		final TextView textview_textsize = (TextView)dialog_textsize.findViewById(R.id.dialog_textsize_label02);
		final TextView textview_sample = (TextView)dialog_textsize.findViewById(R.id.dialog_textsize_label04);
		final int seekbar_base_value = 6;
		//initialize
		scroll_textsize.setProgress((int)mCodeView.getTextSize() - seekbar_base_value);
		textview_textsize.setText(String.valueOf((int)mCodeView.getTextSize()));
		textview_sample.setTextSize(mCodeView.getTextSize());
		scroll_textsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				textview_textsize.setText(String.valueOf(progress + seekbar_base_value));
				textview_sample.setTextSize(progress + seekbar_base_value);
			}
			public void onStartTrackingTouch(SeekBar seekBar){}
			public void onStopTrackingTouch(SeekBar seekBar){}
		});
		//behaver
		new AlertDialog.Builder(this)
		.setTitle(R.string.menu_textsize)
		.setView(dialog_textsize)
		.setPositiveButton(R.string.dlg_button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mCodeView.setTextSize(scroll_textsize.getProgress() + seekbar_base_value);
			}
		})
		.setNegativeButton(R.string.dlg_button_cancel, null)
		.show();
    }

	private void findPrevText(){
		mSpan.setSpan(new BackgroundColorSpan(found_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mFoundIndexListPointer--;
		if(mFoundIndexListPointer < 1){
			mFoundIndexListPointer = mFoundIndexList.size() - 1;
		}
		if(mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0) < mStringBuffer.length()){
			mCodeView.setSelection(mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0));
			mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}else{
			mCodeView.setSelection(mFoundIndexList.get(mFoundIndexListPointer), mStringBuffer.length() - 1);
			mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mStringBuffer.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
	}
	
	private void findNextText(){
		mSpan.setSpan(new BackgroundColorSpan(found_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mFoundIndexListPointer++;
		if(mFoundIndexListPointer > mFoundIndexList.size() - 1){
			mFoundIndexListPointer = 1;
		}
		if(mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0) < mStringBuffer.length()){
			mCodeView.setSelection(mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0));
			mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}else{
			mCodeView.setSelection(mFoundIndexList.get(mFoundIndexListPointer), mStringBuffer.length() - 1);
			mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mStringBuffer.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
    	return true;
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mFoundIndexList != null && mFoundIndexList.size() >= 2){
//			menu.findItem(R.id.findprevtext).setVisible(true);
//			menu.findItem(R.id.findnexttext).setVisible(true);
			menu.findItem(R.id.findprevtext).setVisible(false);
			menu.findItem(R.id.findnexttext).setVisible(false);
		}else{
			menu.findItem(R.id.findprevtext).setVisible(false);
			menu.findItem(R.id.findnexttext).setVisible(false);
		}
		return true;
	}
	
	private boolean isAutoWordwrap = false;
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		String newEncoding = null;

    	switch (item.getItemId()) {
    		case R.id.findtext:showDialogFindText();return true;
    		case R.id.size10:mCodeView.setTextSize(10.0f);return true;
    		case R.id.size16:mCodeView.setTextSize(16.0f);return true;
    		case R.id.size22:mCodeView.setTextSize(22.0f);return true;
    		case R.id.sizein:showDialogTextSize();return true;
//    		case R.id.findprevtext:findPrevText();return true;
//    		case R.id.findnexttext:findNextText();return true;
    		case R.id.autowordwrap:
    			{
	    			isAutoWordwrap = !isAutoWordwrap;
	    			mCodeView.setHorizontallyScrolling(!isAutoWordwrap);
	    			return true;
	    		}
    		case R.id.resetspanning:	//TODO
    			{
					resetFoundList();
					mStringBuffer = new StringBuffer(mCodeView.getText());
					if(mSpan == null){
						mSpan = (Spannable) mCodeView.getText();
					}else{
						StyleSpan[] ss = mSpan.getSpans(0, mStringBuffer.length() - 1, StyleSpan.class);
	                    for (int i = 0; i < ss.length; i++) {
	                    	mSpan.removeSpan(ss[i]);
	                    }
						ForegroundColorSpan[] fs = mSpan.getSpans(0, mStringBuffer.length() - 1, ForegroundColorSpan.class);
	                    for (int i = 0; i < fs.length; i++) {
	                    	mSpan.removeSpan(fs[i]);
	                    }
					}
					setCodeSpannings();
					return true;
    			}
    		case R.id.save:
    			{
    				if(mSaveThread != null && mSaveThread.isAlive()){
    					Toast.makeText(this, R.string.cannot_save, Toast.LENGTH_SHORT).show();
   						return true;
    				}
    				
    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    				//builder.setTitle(android.R.string.dialog_alert_title);
    				builder.setMessage(R.string.msg_save);
    				builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							//save
		    				mSaveThread = new Thread(mSaveRunnable);
		    				mSaveThread.start();
						}
    				});
    				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							
						}
    				});
    				builder.show();
    				return true;
    			}
    		
    		//add encoding
    		case R.id.encoding0:newEncoding = sEncodingList[0];break;
    		case R.id.encoding1:newEncoding = sEncodingList[1];break;
    		case R.id.encoding2:newEncoding = sEncodingList[2];break;
    		case R.id.encoding3:newEncoding = sEncodingList[3];break;
    		case R.id.encoding4:newEncoding = sEncodingList[4];break;
    		case R.id.encoding5:newEncoding = sEncodingList[5];break;
    		case R.id.encoding6:newEncoding = sEncodingList[6];break;
//    		case R.id.encoding7:newEncoding = sEncodingList[7];break;
//    		case R.id.encoding8:newEncoding = sEncodingList[8];break;
    		//case R.id.encoding9:newEncoding = sEncodingList[9];break;
    		default: return false;
    	}
    	
    	//check
    	if(newEncoding == null || newEncoding.equals(mTextEncording)){
    		return true;
    	}

    	//reload for another encoding
    	mTextEncording = newEncoding;
    		
    	if(mThread.isAlive()){
    		mThread.stop();
    		//mThread.destroy();
    	}

    	resetFoundList();
    	
    	mThread = new Thread(mCodeLoaderRunnable);
    	mThread.start();
    	
    	return true;
    }

	private void resetFoundList(){
    	//reset found list - 2011.01.19
    	if(mFoundIndexList == null)mFoundIndexList = new ArrayList<Integer>();
    	//String string = mStringBuffer.toString();
    	if(mSpan != null){
    		//mSpan.setSpan(new BackgroundColorSpan(notfound_bgcolor), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		BackgroundColorSpan[] bs = mSpan.getSpans(0, mStringBuffer.length() - 1, BackgroundColorSpan.class);
            for (int i = 0; i < bs.length; i++) {
            	mSpan.removeSpan(bs[i]);
            }
	    	mFoundIndexList.clear();
	    	mFindLayout.setVisibility(View.GONE);
    	}
    	//
	}
	
	private int setFindTextSpanning(String find_word){
		String string = mStringBuffer.toString();
		int find_word_length = find_word.length();
		int string_index = 0;
		int found_count = 0;

		int base_index = mCodeView.getSelectionStart();
		int next_count = 0;
		
		if(mFoundIndexList == null)mFoundIndexList = new ArrayList<Integer>();
    	mFoundIndexList.clear();
    	mFoundIndexList.add(find_word_length);
    	mFoundIndexListPointer = 1;
    	
		mSpan.setSpan(new BackgroundColorSpan(notfound_bgcolor), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		while(true){
			string_index = string.indexOf(find_word, string_index);
			if(string_index != -1){
				//add found index to list
				if(string_index < base_index){
					mFoundIndexList.add(string_index);
				}else{
					next_count++;
					mFoundIndexList.add(next_count, string_index);
				}
								
				//apply span
				mSpan.setSpan(new BackgroundColorSpan(found_bgcolor), string_index, string_index + find_word_length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				string_index += find_word_length;
				found_count++;
			}else{
				break;
			}
		}
		return found_count;
	}
	
	private void setCodeSpannings(){
		Log.e(TAG, "setCodeSpannings()");
		
  		int string_length = mStringBuffer.length();
		char[] string = new char[string_length];
		int updateTimeCount = string.length / update_span_index;
		boolean[] rateUpdated = new boolean[updateTimeCount];
		for(int i = 0; i < rateUpdated.length; i++){rateUpdated[i] = false;}
		float unitIndexProgressValue = (float)10000 / string.length;
		
		mStringBuffer.getChars(0, string.length, string, 0);
		
		String[] keywords = null;
		int string_index = 0;
		
		boolean startable_word = true;
		boolean started_number = false;
		boolean found_keyword = false;
		
		int span_start = 0;
		int span_end = 0;

		try{
			while(true){
				//keywords
				if(startable_word && (
				   ((int)string[string_index] >= num_a && (int)string[string_index] <= num_z)
				|| ((int)string[string_index] >= num_A && (int)string[string_index] <= num_Z)
				)){
					int keywords_index = (int)string[string_index] - num_a;
					if(keywords_index < 0 || keywords_index > JavaKeyword.keywords.length - 1){
						found_keyword = false;
					}else{
						keywords = JavaKeyword.keywords[keywords_index];
						found_keyword = false;
						for(int i = 0; i < keywords.length; i++){
							if(string_length >= string_index + keywords[i].length()){
								if(String.copyValueOf(string, string_index, keywords[i].length()).equals(keywords[i])){
									span_start = string_index;
									span_end = string_index + keywords[i].length();
									//is span_end word's end?
									
									if(span_end >= string_length || !(((int)string[span_end] >= num_a && (int)string[span_end] <= num_z)
									|| ((int)string[span_end] >= num_A && (int)string[span_end] <= num_Z)
									|| ((int)string[span_end] >= num_0 && (int)string[span_end] <= num_9)
									|| (int)string[span_end] == num_underbar
									)){
										//set span
							            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_KEYWORD, span_start, span_end));
										//
										string_index = span_end;
										found_keyword = true;
										break;
									}
								}	
							}
						}
					}
					if(found_keyword == false){
						string_index++;
					}
					startable_word = false;
				//number value start
				}else if(startable_word && (
						((int)string[string_index] >= num_0 && (int)string[string_index] <= num_9)
					 || ((int)string[string_index] == num_dot))){
					if((int)string[string_index] == num_dot && ((int)string[string_index + 1] < num_0 || (int)string[string_index + 1] > num_9)){
						//don't work
					}else{
						//set span
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_VALUE, string_index, string_index + 1));
						//
						started_number = true;
					}
					string_index++;
					startable_word = false;
				//number value continue
				}else if(started_number){
					if((((int)string[string_index] >= num_0 && (int)string[string_index] <= num_9) || (int)string[string_index] == num_dot)
					|| ((int)string[string_index] >= num_a && (int)string[string_index] <= num_z)
					|| ((int)string[string_index] >= num_A && (int)string[string_index] <= num_Z)){
						//set span
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_VALUE, string_index, string_index + 1));
						//
					}else{
						startable_word = true;
						started_number = false;
					}
					string_index++;
				//no keyword, no number value
				}else if(((int)string[string_index] < num_0 || (int)string[string_index] > num_9)
					&& ((int)string[string_index] != num_dot)
					&& ((int)string[string_index] != num_underbar)
					&& ((int)string[string_index] < num_a || (int)string[string_index] > num_z)
					&& ((int)string[string_index] < num_A || (int)string[string_index] > num_Z)){
					//single line comment - priority 1
					if(String.copyValueOf(string, string_index, JavaKeyword.comment_singleline_start.length()).equals(JavaKeyword.comment_singleline_start)){
						span_start = string_index;
						string_index += JavaKeyword.comment_singleline_start.length();
						span_end = String.valueOf(string).indexOf(JavaKeyword.comment_singleline_end, string_index);
						if(span_end != -1){
							span_end += JavaKeyword.comment_singleline_end.length();
						}else{
							span_end = string.length - 1;
						}
						//set span
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_COMMENT, span_start, span_end));
						//
						string_index = span_end;
					//multi line comment
					}else if(String.copyValueOf(string, string_index, JavaKeyword.comment_multiline_start.length()).equals(JavaKeyword.comment_multiline_start)){
						span_start = string_index;
						string_index += JavaKeyword.comment_multiline_start.length();
						span_end = String.valueOf(string).indexOf(JavaKeyword.comment_multiline_end, string_index);
						if(span_end != -1){
							span_end += JavaKeyword.comment_multiline_end.length();
						}else{
							span_end = string.length - 1;
						}
						//set span
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_COMMENT, span_start, span_end));
						//
						string_index = span_end;
					//string value
					}else if(String.copyValueOf(string, string_index, JavaKeyword.value_string.length()).equals(JavaKeyword.value_string)){
						span_start = string_index;
						string_index += JavaKeyword.value_string.length();
						
						int counts = string_index;
						do{
							span_end = String.valueOf(string).indexOf(JavaKeyword.value_string, counts);
							if(string[span_end - 1] != '\\'){
								break;
							}
							counts = span_end + 1;
						}while(true);
						
						if(span_end != -1){
							span_end += JavaKeyword.value_string.length();
						}else{
							span_end = string.length - 1;
						}
						//set span
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_VALUE, span_start, span_end));
						//
						string_index = span_end;
					//char value
					}else if(String.copyValueOf(string, string_index, JavaKeyword.value_char.length()).equals(JavaKeyword.value_char)){
						span_start = string_index;
						string_index += JavaKeyword.value_char.length();
						
						int counts = string_index;
						do{
							span_end = String.valueOf(string).indexOf(JavaKeyword.value_char, counts);
							if(string[span_end - 1] != '\\'){
								break;
							}
							counts = span_end + 1;
						}while(true);
						
						if(span_end != -1){
							span_end += JavaKeyword.value_char.length();
						}else{
							span_end = string.length - 1;
						}
						//set span
			            mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_SPAN_VALUE, span_start, span_end));
						//
						string_index = span_end;	
					}else{
						string_index++;
					}
					startable_word = true;
				//no spanning
				}else{
					string_index++;
				}
	
				//file end
				if(string_index >= string.length - 1){
					//set value progress
			        mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_SET, 10000, 0));
					break;
				}else{
					int rateIndex = string_index / update_span_index;
					if(rateIndex >= 0 && rateIndex < updateTimeCount && rateUpdated[rateIndex] == false){
						rateUpdated[rateIndex] = true;
						//set value progress
				        mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_SET, (int)(string_index * unitIndexProgressValue), 0));
						//sleep
			        	try{Thread.sleep(50);}
			        	catch(InterruptedException e1){e1.printStackTrace();}
					}
				}
			}
		}catch(Exception e){
			try{
				e.printStackTrace();
				Log.e(TAG, "Error String = " + mStringBuffer.substring(string_index));
			}catch(Exception e2){
				Log.e(TAG, "Error String is Error.");
			}
		}
        
		return;
	}
	
	private Thread mSaveThread = null;
	
	private Runnable mSaveRunnable = new Runnable(){
		synchronized public void run() {
			mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_ON));
			save();
			mHandler.sendMessage(mHandler.obtainMessage(MSG_TYPE_PROGRESS_OFF));
		}
	};
	
	private void save(){
		if(mFileURI == null)return;
		String filepath = mFileURI.getPath();

		//OutputStreamWriter
		
		FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        String string = null;
        
        try {
        	string = mCodeView.getText().toString();
        	
			fileWriter = new FileWriter(filepath);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				bufferedWriter.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
}
