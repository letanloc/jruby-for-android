package se.kth.pascalc.JRubyAndroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Dialog that returns the file name to save a script as.
 * @author Pascal Chatterjee
 */
public class SaveFile extends Activity implements View.OnClickListener{
	private EditText filenameInput;
	private Button saveButton,cancelButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_file_dialog);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
							 WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		bindUI();
		assignActions();
	}
	
	private void bindUI(){
		filenameInput = (EditText)this.findViewById(R.id.filename_input);
		String filename = getIntent().getExtras().getString(JRubyAndroid.FILENAME);
		filenameInput.setText(filename);
		
		saveButton = (Button)this.findViewById(R.id.filename_confirm);
		cancelButton = (Button)this.findViewById(R.id.filename_cancel);
	}
	
	private void assignActions(){
		saveButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v == saveButton){
			Intent res = new Intent();
			String fname = filenameInput.getText().toString();
			if(!fname.endsWith(".rb"))
				fname += ".rb";
			res.putExtra(JRubyAndroid.FILENAME, fname);
			this.setResult(Activity.RESULT_OK, res);
			finish();
		}
		else if (v == cancelButton){
			this.setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}	
}
