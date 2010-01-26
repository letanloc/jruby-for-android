package se.kth.pascalc.JRubyAndroid;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.parser.EvalStaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.scope.ManyVarsDynamicScope;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Class to encapsulate IRBView of JRubyAndroid. Handles the JRuby runtime
 * and direct access to it via an IRB-like interface.
 * The method setUpJRuby() and class HistoryEditText are adapted from 
 * code by Jan Berkel (http://github.com/headius/ruboto-irb)
 * @author Pascal Chatterjee
 */
public class IRBView {
	private Activity parent;
	private Ruby ruby;
	private DynamicScope scope;
	private PrintStream textViewStream;
	
	private TextView irbOutput;
	private HistoryEditText irbInput;

	public IRBView(JRubyAndroid parent) {
		this.parent = parent;
		
		bindUI();
	}
	
	public Ruby getRuby() {
		return ruby;
	}
	
	private void bindUI(){
		irbInput = (HistoryEditText)parent.findViewById(R.id.irb_edittext);
		irbOutput = (TextView)parent.findViewById(R.id.irb_textview);
		irbOutput.setMovementMethod(new android.text.method.ScrollingMovementMethod());
		irbOutput.setText(">> ");
	}
	
	/**
	 * Sets up JRuby environment.
	 */
	public void setUpJRuby(){
		RubyInstanceConfig config = new RubyInstanceConfig();
		config.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
		
		/* Connect Ruby output to our TextView */
		textViewStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
                irbOutput.append(Character.toString((char)arg0));
            }
        });
        config.setOutput(textViewStream);
        
        config.setLoader(getClass().getClassLoader());
        
        /* Set up Ruby environment */
        ruby = Ruby.newInstance(config);
        // make parent Activity available in IRB
        ruby.defineGlobalConstant("Activity", JavaUtil.convertJavaToRuby(ruby, parent));
        
        ThreadContext context = ruby.getCurrentContext();
        DynamicScope currentScope = context.getCurrentScope();
        scope = new ManyVarsDynamicScope(
        		new EvalStaticScope(currentScope.getStaticScope()), currentScope);
        
        /* Connect the HistoryEditText to Ruby */
        irbInput.setLineListener(new HistoryEditText.LineListener() {                  
            public void onNewLine(String rubyCode) {                                    
                irbOutput.append(rubyCode + "\n");
                String inspected = execRuby(rubyCode);
                irbOutput.append("=> " + inspected + "\n");
                irbOutput.append(">> ");
                irbInput.setText("");               
            }
        });            
	}
	
	/**
	 * Executes the given String of Ruby code using the set-up JRuby environment.
	 * @param rubyCode Ruby Code to execute
	 * @return Returned value as a String
	 */
	public String execRuby(String rubyCode){
		try {
            String inspected = 
            	ruby.evalScriptlet(rubyCode, scope).inspect().asJavaString();
            return inspected;
        } catch (RaiseException re) {                
            re.printStackTrace(textViewStream);
        }
        return null;
	}
	
	/**
	 * EditText with history (key down, key up)
	 * @author Jan Berkel
	 */
	public static class HistoryEditText extends EditText implements
		android.view.View.OnKeyListener,
		TextView.OnEditorActionListener
	{ 
		public interface LineListener {
			void onNewLine(String s);
		}     

		private int cursor = -1;
		private List<String> history = new ArrayList<String>();
		private LineListener listener;

		public HistoryEditText(Context ctxt) {
			super(ctxt);
			initListeners();
		}

		public HistoryEditText(Context ctxt,  android.util.AttributeSet attrs) {
			super(ctxt, attrs);    
			initListeners();
		}

		public HistoryEditText(Context ctxt,  android.util.AttributeSet attrs, int defStyle) {
			super(ctxt, attrs, defStyle);
			initListeners();
		}

		private void initListeners() {
			setOnKeyListener(this);
			setOnEditorActionListener(this);
		}

		public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_NULL) {
				String line = getText().toString();
				if (line.length() == 0) return true;           
				history.add(line);        
				cursor = history.size();

				if (listener != null) {
					listener.onNewLine(line);
					return true;
				}
			}
			return false;
		}

		public void setLineListener(LineListener l) { this.listener = l; }

		public void setCursorPosition(int pos) {
			Selection.setSelection(getText(), pos);
		}

		public boolean onKey(View view, int keyCode, KeyEvent evt) {        
			if (evt.getAction() == KeyEvent.ACTION_DOWN || evt.getAction() == KeyEvent.ACTION_MULTIPLE) {

				if (cursor >= 0 && (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {                        
					if (keyCode == KeyEvent.KEYCODE_DPAD_UP ) {
						cursor -= 1;
					} else {
						cursor += 1;
					}

					if (cursor < 0)
						cursor = 0;
					else if (cursor >= history.size()) {
						cursor = history.size() - 1;
					}
					setText(history.get(cursor));
					return true;
				} 
			}
			return false;
		}
	}
}
