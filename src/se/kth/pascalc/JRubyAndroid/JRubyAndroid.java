package se.kth.pascalc.JRubyAndroid;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * Main Activity, sets up and controls the IRB, Editor and Script Views, 
 * as well as handling the Menu, and Intent interaction. 
 * @author Pascal Chatterjee
 */
public class JRubyAndroid extends Activity implements View.OnClickListener{
	private ViewFlipper flipper;
	private Button irbButton,editorButton,scriptsButton;
	private IRBView irbView;
	private EditorView editorView;
	private ScriptsView scriptsView;
	private final Handler handler = new Handler();
	private ProgressDialog pd;
	
	public final int IRBVIEW = 0;
	public final int EDITORVIEW = 1;
	public final int SCRIPTSVIEW = 2;
	public static final String FILENAME = "FILENAME";
	public static final int SAVE_FILE = 3;

	private static final int RESCAN_ID = Menu.FIRST;
    private static final int ABOUT_ID = Menu.FIRST + 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        bindUI();
        assignActions();
        
        irbView = new IRBView(this);
        editorView = new EditorView(this);
        scriptsView = new ScriptsView(this);
    	switchToView(IRBVIEW);
        
        setUpJRuby();
        pd = ProgressDialog.show(this, "JRuby for Android","Initialising JRuby...", 
        		true,false);
    }

	protected void setUpJRuby(){
    	Thread t = new Thread() {
    		public void run(){
    			irbView.setUpJRuby();
    	        handler.post(notifyComplete);
    		}
    	};
    	t.start();
    }
    
    protected final Runnable notifyComplete = new Runnable(){
    	public void run(){
    		pd.dismiss();
    	}
    };
    
    public IRBView getIrbView() {
		return irbView;
	}

	public EditorView getEditorView() {
		return editorView;
	}

    public ScriptsView getScriptsView() {
		return scriptsView;
	}
	
	private void bindUI(){
    	flipper = (ViewFlipper)this.findViewById(R.id.flipper);
    	
        irbButton = (Button)this.findViewById(R.id.irb_button);
        editorButton = (Button)this.findViewById(R.id.editor_button);
        scriptsButton = (Button)this.findViewById(R.id.scripts_button);
    }
    
    private void assignActions(){
    	irbButton.setOnClickListener(this);
    	editorButton.setOnClickListener(this);
    	scriptsButton.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		if (v == irbButton){
			switchToView(IRBVIEW);
		}
		else if (v == editorButton){
			switchToView(EDITORVIEW);
		}
		else if (v == scriptsButton){
			switchToView(SCRIPTSVIEW);
		}
	}
	
	public void switchToView(int view){
		flipper.setDisplayedChild(view);
		switch(view) {
			case(IRBVIEW):
				irbButton.setEnabled(false);
				editorButton.setEnabled(true);
				scriptsButton.setEnabled(true);

				irbButton.setTypeface(Typeface.DEFAULT_BOLD);
				editorButton.setTypeface(Typeface.DEFAULT);
				scriptsButton.setTypeface(Typeface.DEFAULT);
				break;
			case(EDITORVIEW):
				irbButton.setEnabled(true);
				editorButton.setEnabled(false);
				scriptsButton.setEnabled(true);

				irbButton.setTypeface(Typeface.DEFAULT);
				editorButton.setTypeface(Typeface.DEFAULT_BOLD);
				scriptsButton.setTypeface(Typeface.DEFAULT);
				break;
			case(SCRIPTSVIEW):
				irbButton.setEnabled(true);
				editorButton.setEnabled(true);
				scriptsButton.setEnabled(false);

				irbButton.setTypeface(Typeface.DEFAULT);
				editorButton.setTypeface(Typeface.DEFAULT);
				scriptsButton.setTypeface(Typeface.DEFAULT_BOLD);
				break;
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, RESCAN_ID, 0, R.string.Menu_rescan);
        menu.add(0,ABOUT_ID,1,R.string.Menu_about);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        	case RESCAN_ID:
        		scriptsView.rescanScripts();
        		return true;
        	case ABOUT_ID:
        		Intent aboutIntent = new Intent(this,
        				se.kth.pascalc.JRubyAndroid.AboutDialog.class);
        		startActivity(aboutIntent);
        		return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }

	public void requestSaveFile(String filename) {
		Intent fnameIntent = new Intent(this,se.kth.pascalc.JRubyAndroid.SaveFile.class);
		fnameIntent.putExtra(JRubyAndroid.FILENAME, filename);
		startActivityForResult(fnameIntent, JRubyAndroid.SAVE_FILE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == JRubyAndroid.SAVE_FILE){
			/* Pass on to editorView */
			editorView.onActivityResult(requestCode,resultCode,data);
		}
		else{
			/* Make activity result available to Ruby*/
			Ruby ruby = irbView.getRuby();
			ActivityResult ar = new ActivityResult(requestCode,resultCode,data);
			ruby.defineGlobalConstant("LastActivityResult",
					JavaUtil.convertJavaToRuby(ruby,ar));
		}
	}
	
	private class ActivityResult{
		public int requestCode,resultCode;
		public Intent data;
		
		public ActivityResult(int req, int res, Intent dat){
			requestCode= req;
			resultCode = res;
			data = dat;
		}
	}
}