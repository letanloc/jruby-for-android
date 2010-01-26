package se.kth.pascalc.JRubyAndroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Dialog that displays information about JRubyAndroid.
 * @author Pascal Chatterjee
 */
public class AboutDialog extends Activity implements View.OnClickListener{
	private Button okButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_dialog);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
							 WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		bindUI();
		assignActions();
	}
	
	private void bindUI(){
		okButton = (Button)this.findViewById(R.id.about_ok);
	}
	
	private void assignActions(){
		okButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v == okButton){
			this.setResult(Activity.RESULT_OK);
			finish();
		}
	}	
}