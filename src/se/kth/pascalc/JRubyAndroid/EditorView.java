package se.kth.pascalc.JRubyAndroid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class to encapsulate the Editor View of JRubyAndroid. Handles the saving 
 * and execution of JRuby source code.
 * @author Pascal Chatterjee
 */
public class EditorView implements View.OnClickListener{
	private JRubyAndroid parent;

	private EditText sourceEditor;
	private Button runButton;
	private Button saveButton;
	private TextView fnameTextView;
	
	public EditorView(JRubyAndroid parent){
		this.parent = parent;
		
		bindUI();
		assignActions();

		setFilename("untitled.rb");
	}
	
	public String getFilename() {
		return fnameTextView.getText().toString();
	}

	public void setFilename(String filename) {
		fnameTextView.setText(filename);
	}

	private void bindUI(){
		sourceEditor = (EditText)parent.findViewById(R.id.source_editor);
		runButton = (Button)parent.findViewById(R.id.run_source_button);
		saveButton = (Button)parent.findViewById(R.id.save_source_button);
		fnameTextView = (TextView)parent.findViewById(R.id.fname_textview);
	}
	
	private void assignActions(){
		runButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
	}
	
	public String getSourceText(){
		return sourceEditor.getText().toString();
	}
	
	public void setSourceText(String text){
		sourceEditor.setText(text);
	}
	
	public void appendSourceText(String text){
		sourceEditor.append(text);
	}

	@Override
	public void onClick(View v) {
		if (v == runButton){
			String source = sourceEditor.getText().toString();
			if((source != null) && !(source.equals(""))){
				this.parent.getIrbView().execRuby(source);
				parent.switchToView(parent.IRBVIEW);
			}
		}
		else if (v == saveButton){
			parent.requestSaveFile(getFilename());
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == JRubyAndroid.SAVE_FILE){
			switch(resultCode){
				case Activity.RESULT_OK:
					String newFilename = data.getExtras().getString(JRubyAndroid.FILENAME);
					String fullPath = ScriptsView.SCRIPTS_DIR + "/" + newFilename;
					setFilename(newFilename);
					saveFile(fullPath);
					break;
				case Activity.RESULT_CANCELED:
					break;
			}
		}
	}

	private void saveFile(String fullPath) {
		try{
			BufferedWriter buffy = new BufferedWriter(new FileWriter(fullPath));
			String sourceCode = sourceEditor.getText().toString();
			buffy.write(sourceCode);
			buffy.close();
			parent.getScriptsView().rescanScripts();
			Toast.makeText(parent, "Saved " + getFilename(),Toast.LENGTH_SHORT).show();
		}
		catch(IOException e) {
			Toast.makeText(parent, "Could not write " + fullPath, Toast.LENGTH_SHORT).show();
		}
	}
}
