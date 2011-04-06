//package net.ahyane.javacodeviewer;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.graphics.Typeface;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.text.Spannable;
//import android.text.style.BackgroundColorSpan;
//import android.text.style.ForegroundColorSpan;
//import android.text.style.StyleSpan;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.Window;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//public class JavaCodeViewerActivity extends Activity {	
//	private static final int num_a = 97;
//	private static final int num_z = 122;
//	private static final int num_A = 65;
//	private static final int num_Z = 90;
//	private static final int num_0 = 48;
//	private static final int num_9 = 57;
//	private static final int num_dot = '.';
//	private static final int num_underbar = '_';
//
//	private static final int notfound_bgcolor = 0x00ffffff;
//	private static final int found_bgcolor = 0xffffbf00; 
//	private static final int selectedfound_bgcolor = 0xff00bfff; 
//
//    private Uri mFileURI = null;
//    private StringBuffer mStringBuffer = null;
//    private EditText mCodeView = null;
//    private Spannable mSpan = null;
//    private ArrayList<Integer> mFoundIndexList = null;
//    private int mFoundIndexListPointer = 0;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        setContentView(R.layout.main);
//        
//        mCodeView = (EditText) findViewById(R.id.main_edittext01);
//
//        mFileURI = getIntent().getData();
//        File file = new File(mFileURI.getEncodedPath());
//        if(file.exists()){
//        	String titleText = null;
//        	String path = mFileURI.getEncodedPath();
//        	titleText = path.substring(path.lastIndexOf('/') + 1) + " - [" + path + "]";
//        	setTitle(titleText);
//        }else{
//        	return;
//        }
//        mStringBuffer = new StringBuffer("");
//        FileInputStream fileInputStream = null;
//        BufferedReader bufferedReader = null;
//        String lineString = null;
//
//		try {
//			fileInputStream = new FileInputStream(file);
//			bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
//            int original_length = 0;
//            while((lineString = bufferedReader.readLine()) != null){
//        		mStringBuffer.append(lineString).append("\n");
//        		original_length += lineString.length();
//            }
//            
//            if(original_length == 0){
//            	return;
//            }
//            
//			mCodeView.setText(mStringBuffer.toString());
//			//**important_start**
//			mSpan = (Spannable) mCodeView.getText();
//			setCodeSpannings();
//			//position reset
//			mCodeView.setSelection(mStringBuffer.length());
//			mCodeView.setSelection(0);
//			//**important_end**
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}finally{
//			try {
//				if(bufferedReader != null)bufferedReader.close();
//				if(fileInputStream != null)fileInputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//    }
//
//    private void showDialogFindText(){
//    	final LinearLayout dialog_findtext = (LinearLayout)View.inflate(this, R.layout.dialog_findtext, null);
//    	new AlertDialog.Builder(this)
//		.setTitle(R.string.menu_findtext)
//		.setView(dialog_findtext)
//		.setPositiveButton(R.string.dlg_button_ok, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int whichButton) {
//				EditText edittext_findtext = (EditText)dialog_findtext.findViewById(R.id.dialog_findtext_edittext01);
//				String string_findtext = edittext_findtext.getText().toString();
//				if(string_findtext.equals("")){
//					Toast.makeText(JavaCodeViewerActivity.this, R.string.toast_cannotfind_space, 4000).show();
//				}else{
//					int found_count = setFindTextSpanning(string_findtext);
//					if(found_count == 0){
//						Toast.makeText(JavaCodeViewerActivity.this, R.string.toast_notfound, 4000).show();
//					}else{
//						mCodeView.setSelection(mFoundIndexList.get(1), mFoundIndexList.get(1) + mFoundIndexList.get(0));
//						mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(1), mFoundIndexList.get(1) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//						Toast.makeText(JavaCodeViewerActivity.this, found_count + ((Context)JavaCodeViewerActivity.this).getString(R.string.toast_found), 4000).show();
//					}
//				}
//			}
//		})
//		.setNegativeButton(R.string.dlg_button_cancel, null)
//		.show();	
//    }
//    
//    private void showDialogTextSize(){
//		final LinearLayout dialog_textsize = (LinearLayout)View.inflate(this, R.layout.dialog_textsize, null);
//		final SeekBar scroll_textsize = (SeekBar)dialog_textsize.findViewById(R.id.dialog_textsize_seekbar01);
//		final TextView textview_textsize = (TextView)dialog_textsize.findViewById(R.id.dialog_textsize_label02);
//		final TextView textview_sample = (TextView)dialog_textsize.findViewById(R.id.dialog_textsize_label04);
//		final int seekbar_base_value = 6;
//		//initialize
//		scroll_textsize.setProgress((int)mCodeView.getTextSize() - seekbar_base_value);
//		textview_textsize.setText(String.valueOf((int)mCodeView.getTextSize()));
//		textview_sample.setTextSize(mCodeView.getTextSize());
//		scroll_textsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//				textview_textsize.setText(String.valueOf(progress + seekbar_base_value));
//				textview_sample.setTextSize(progress + seekbar_base_value);
//			}
//			public void onStartTrackingTouch(SeekBar seekBar){}
//			public void onStopTrackingTouch(SeekBar seekBar){}
//		});
//		//behaver
//		new AlertDialog.Builder(this)
//		.setTitle(R.string.menu_textsize)
//		.setView(dialog_textsize)
//		.setPositiveButton(R.string.dlg_button_ok, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int whichButton) {
//				mCodeView.setTextSize(scroll_textsize.getProgress() + seekbar_base_value);
//			}
//		})
//		.setNegativeButton(R.string.dlg_button_cancel, null)
//		.show();
//    }
//
//	private void findPrevText(){
//		mSpan.setSpan(new BackgroundColorSpan(found_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		mFoundIndexListPointer--;
//		if(mFoundIndexListPointer < 1){
//			mFoundIndexListPointer = mFoundIndexList.size() - 1;
//		}
//		mCodeView.setSelection(mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0));
//		mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//	}
//	
//	private void findNextText(){
//		mSpan.setSpan(new BackgroundColorSpan(found_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		mFoundIndexListPointer++;
//		if(mFoundIndexListPointer > mFoundIndexList.size() - 1){
//			mFoundIndexListPointer = 1;
//		}
//		mCodeView.setSelection(mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0));
//		mSpan.setSpan(new BackgroundColorSpan(selectedfound_bgcolor), mFoundIndexList.get(mFoundIndexListPointer), mFoundIndexList.get(mFoundIndexListPointer) + mFoundIndexList.get(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//	}
//    
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater menuInflater = getMenuInflater();
//		menuInflater.inflate(R.menu.menu, menu);
//    	return true;
//    }
//
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		if(mFoundIndexList != null && mFoundIndexList.size() > 2){
//			menu.findItem(R.id.findprevtext).setVisible(true);
//			menu.findItem(R.id.findnexttext).setVisible(true);
//		}else{
//			menu.findItem(R.id.findprevtext).setVisible(false);
//			menu.findItem(R.id.findnexttext).setVisible(false);
//		}
//		return true;
//	}
//
//	@Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch (item.getItemId()) {
//    		case R.id.findtext:showDialogFindText();break;
//    		case R.id.size10:mCodeView.setTextSize(10.0f);break;
//    		case R.id.size16:mCodeView.setTextSize(16.0f);break;
//    		case R.id.size22:mCodeView.setTextSize(22.0f);break;
//    		case R.id.sizein:showDialogTextSize();break;
//    		case R.id.findprevtext:findPrevText();break;
//    		case R.id.findnexttext:findNextText();break;
//    		
//    	}
//    	return false;
//    }
//
//	private int setFindTextSpanning(String find_word){
//		String string = mStringBuffer.toString();
//		int find_word_length = find_word.length();
//		int string_index = 0;
//		int found_count = 0;
//
//		int base_index = mCodeView.getSelectionStart();
//		int next_count = 0;
//		
//		if(mFoundIndexList == null)mFoundIndexList = new ArrayList<Integer>();
//    	mFoundIndexList.clear();
//    	mFoundIndexList.add(find_word_length);
//    	mFoundIndexListPointer = 1;
//    	
//		mSpan.setSpan(new BackgroundColorSpan(notfound_bgcolor), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		while(true){
//			string_index = string.indexOf(find_word, string_index);
//			if(string_index != -1){
//				//add found index to list
//				if(string_index < base_index){
//					mFoundIndexList.add(string_index);
//				}else{
//					next_count++;
//					mFoundIndexList.add(next_count, string_index);
//				}
//								
//				//apply span
//				mSpan.setSpan(new BackgroundColorSpan(found_bgcolor), string_index, string_index + find_word_length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				string_index += find_word_length;
//				found_count++;
//			}else{
//				break;
//			}
//		}
//		return found_count;
//	}
//	
//	private void setCodeSpannings(){
//  		int string_length = mStringBuffer.length();
//		char[] string = new char[string_length];
//		mStringBuffer.getChars(0, string.length, string, 0);
//		
//		String[] keywords = null;
//		int string_index = 0;
//		
//		boolean startable_word = true;
//		boolean started_number = false;
//		boolean found_keyword = false;
//		
//		int span_start = 0;
//		int span_end = 0;
//		
//		while(true){
//			//keywords
//			if(startable_word && (
//			   ((int)string[string_index] >= num_a && (int)string[string_index] <= num_z)
//			|| ((int)string[string_index] >= num_A && (int)string[string_index] <= num_Z)
//			)){
//				int keywords_index = (int)string[string_index] - num_a;
//				if(keywords_index < 0 || keywords_index > JavaKeyword.keywords.length - 1){
//					found_keyword = false;
//				}else{
//					keywords = JavaKeyword.keywords[keywords_index];
//					found_keyword = false;
//					for(int i = 0; i < keywords.length; i++){
//						if(String.copyValueOf(string, string_index, keywords[i].length()).equals(keywords[i])){
//							span_start = string_index;
//							span_end = string_index + keywords[i].length();
//							//is span_end word's end?
//							if((int)string[span_end] < num_a || (int)string[span_end] > num_z){
//								mSpan.setSpan(new StyleSpan(Typeface.BOLD), span_start, span_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//								mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.keywords_color), span_start, span_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//								string_index = span_end;
//								found_keyword = true;
//								break;
//							}
//						}							
//					}
//				}
//				if(found_keyword == false){
//					string_index++;
//				}
//				startable_word = false;
//			//number value start
//			}else if(startable_word && (
//					((int)string[string_index] >= num_0 && (int)string[string_index] <= num_9)
//				 || ((int)string[string_index] == num_dot))){
//				if((int)string[string_index] == num_dot && ((int)string[string_index + 1] < num_0 || (int)string[string_index + 1] > num_9)){
//					//don't work
//				}else{
//					mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.value_color), string_index, string_index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//					started_number = true;
//				}
//				string_index++;
//				startable_word = false;
//			//number value continue
//			}else if(started_number){
//				if((((int)string[string_index] >= num_0 && (int)string[string_index] <= num_9) || (int)string[string_index] == num_dot)
//				|| ((int)string[string_index] >= num_a && (int)string[string_index] <= num_z)
//				|| ((int)string[string_index] >= num_A && (int)string[string_index] <= num_Z)){
//					mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.value_color), string_index, string_index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}else{
//					startable_word = true;
//					started_number = false;
//				}
//				string_index++;
//			//no keyword, no number value
//			}else if(((int)string[string_index] < num_0 || (int)string[string_index] > num_9)
//				&& ((int)string[string_index] != num_dot)
//				&& ((int)string[string_index] != num_underbar)
//				&& ((int)string[string_index] < num_a || (int)string[string_index] > num_z)
//				&& ((int)string[string_index] < num_A || (int)string[string_index] > num_Z)){
//				//single line comment - priority 1
//				if(String.copyValueOf(string, string_index, JavaKeyword.comment_singleline_start.length()).equals(JavaKeyword.comment_singleline_start)){
//					span_start = string_index;
//					string_index += JavaKeyword.comment_singleline_start.length();
//					span_end = String.valueOf(string).indexOf(JavaKeyword.comment_singleline_end, string_index);
//					if(span_end != -1){
//						span_end += JavaKeyword.comment_singleline_end.length();
//					}else{
//						span_end = string.length - 1;
//					}
//					mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.comment_color), span_start, span_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//					string_index = span_end;
//				//multi line comment
//				}else if(String.copyValueOf(string, string_index, JavaKeyword.comment_multiline_start.length()).equals(JavaKeyword.comment_multiline_start)){
//					span_start = string_index;
//					string_index += JavaKeyword.comment_multiline_start.length();
//					span_end = String.valueOf(string).indexOf(JavaKeyword.comment_multiline_end, string_index);
//					if(span_end != -1){
//						span_end += JavaKeyword.comment_multiline_end.length();
//					}else{
//						span_end = string.length - 1;
//					}
//					mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.comment_color), span_start, span_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//					string_index = span_end;
//				//string value
//				}else if(String.copyValueOf(string, string_index, JavaKeyword.value_string.length()).equals(JavaKeyword.value_string)){
//					span_start = string_index;
//					string_index += JavaKeyword.value_string.length();
//					span_end = String.valueOf(string).indexOf(JavaKeyword.value_string, string_index);
//					if(span_end != -1){
//						span_end += JavaKeyword.value_string.length();
//					}else{
//						span_end = string.length - 1;
//					}
//					mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.value_color), span_start, span_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//					string_index = span_end;
//				//char value
//				}else if(String.copyValueOf(string, string_index, JavaKeyword.value_char.length()).equals(JavaKeyword.value_char)){
//					span_start = string_index;
//					string_index += JavaKeyword.value_char.length();
//					span_end = String.valueOf(string).indexOf(JavaKeyword.value_char, string_index);
//					if(span_end != -1){
//						span_end += JavaKeyword.value_char.length();
//					}else{
//						span_end = string.length - 1;
//					}
//					mSpan.setSpan(new ForegroundColorSpan(JavaKeyword.value_color), span_start, span_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//					string_index = span_end;	
//				}else{
//					string_index++;
//				}
//				startable_word = true;
//			//no spanning
//			}else{
//				string_index++;
//			}
//
//			//file end
//			if(string_index >= string.length - 1){
//				break;
//			}
//		}
//		return;
//	}
//}
