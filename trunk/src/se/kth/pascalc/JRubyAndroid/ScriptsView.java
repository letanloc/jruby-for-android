package se.kth.pascalc.JRubyAndroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Class to encapsulate the Script Manager view of JRubyAndroid. Handles the
 * editing and execution of scripts. The script directory is /sdcard/jruby/
 * @author Pascal Chatterjee
 */
public class ScriptsView implements OnItemClickListener, OnItemLongClickListener{
	private JRubyAndroid parent;
	
	private ListView scriptsList;
	private ArrayAdapter<String> adapter;
	
	private ArrayList<String> scripts;
	public static final String SCRIPTS_DIR = "/sdcard/jruby";
	
	public ScriptsView(JRubyAndroid parent) {
		this.parent = parent;
		
		scriptsList = (ListView)parent.findViewById(R.id.scripts_listview);
		setUpList();
	}
	
	private void setUpList(){
		scripts = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(parent,R.layout.scriptslist_entry,scripts);
		
		scanScripts();
		
		scriptsList.setAdapter(adapter);
		TextView emptyView = (TextView)parent.findViewById(R.id.scriptslist_empty);
		scriptsList.setEmptyView(emptyView);
		scriptsList.setOnItemClickListener(this);
		scriptsList.setOnItemLongClickListener(this);
	}
	
	private void scanScripts(){
		File scriptsDir = new File(SCRIPTS_DIR);
		
		/* Create directory if it doesn't exist */
		if (!scriptsDir.exists()){
			try{
				scriptsDir.mkdir();
			}
			catch(SecurityException se){
				Toast.makeText(parent, "Could not create " + SCRIPTS_DIR,Toast.LENGTH_SHORT);
				return;
			}
		}
		
		String[] scriptsArray = scriptsDir.list(new FilenameFilter() {
			public boolean accept(File dir, String fname){
				return fname.endsWith(".rb");
			}
		});

		for(int i = 0; i < scriptsArray.length; i++){
			scripts.add(scriptsArray[i]);
		}
	}
	
	public void rescanScripts(){
		File scriptsDir = new File(SCRIPTS_DIR);
		
		if (scriptsDir.exists()){
			String[] scriptsArray = scriptsDir.list(new FilenameFilter() {
				public boolean accept(File dir, String fname){
					return fname.endsWith(".rb");
				}
			});

			scripts.clear();
			for(int i = 0; i < scriptsArray.length; i++){
				scripts.add(scriptsArray[i]);
			}
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Load script into EditorView when a script is clicked.
	 */
	@Override
	public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		String fname = scripts.get(pos);
		String targetScriptPath = SCRIPTS_DIR + "/" + fname;
		try{
			BufferedReader buffy = new BufferedReader(new FileReader(targetScriptPath));
			parent.getEditorView().setSourceText("");
			String line;
			while((line = buffy.readLine()) != null){
				parent.getEditorView().appendSourceText(line + "\n");
			}
			buffy.close();
			parent.getEditorView().setFilename(fname);
			parent.switchToView(parent.EDITORVIEW);
		}
		catch(IOException e) {
			Toast.makeText(parent, "Could not open " + targetScriptPath, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Execute a script when it is long-clicked.
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos,
			long id) {
		String targetScriptPath = SCRIPTS_DIR + "/" + scripts.get(pos);
		try{
			BufferedReader buffy = new BufferedReader(new FileReader(targetScriptPath));
			StringBuilder source = new StringBuilder();
			while(true) {
				String line = buffy.readLine();
				if (line == null)
					break;
				source.append(line + "\n");
			}
			buffy.close();
			parent.getIrbView().execRuby(source.toString());
			parent.switchToView(parent.IRBVIEW);
		}
		catch(IOException e) {
			Toast.makeText(parent, "Could not open " + targetScriptPath, Toast.LENGTH_SHORT).show();
		}
		return true;
	}
}
