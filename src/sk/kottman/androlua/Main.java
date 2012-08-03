package sk.kottman.androlua;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.*;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener,
		OnLongClickListener {
	private final static int LISTEN_PORT = 3333;

	Button execute;
	
	// public so we can play with these from Lua
	public EditText source;
	public TextView status;
	public LuaState L;
	
	final StringBuilder output = new StringBuilder();

	Handler handler;
	ServerThread serverThread;
	
	private static byte[] readAll(InputStream input) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		execute = (Button) findViewById(R.id.executeBtn);
		execute.setOnClickListener(this);

		source = (EditText) findViewById(R.id.source);
		source.setOnLongClickListener(this);
		source.setText("require 'import'\nprint(Math:sin(2.3))\n");

		status = (TextView) findViewById(R.id.statusText);
		status.setMovementMethod(ScrollingMovementMethod.getInstance());

		handler = new Handler();

		L = LuaStateFactory.newLuaState();
		L.openLibs();

		try {
			L.pushJavaObject(this);
			L.setGlobal("activity");

			JavaFunction print = new JavaFunction(L) {
				@Override
				public int execute() throws LuaException {
					for (int i = 2; i <= L.getTop(); i++) {
						int type = L.type(i);
						String stype = L.typeName(type);
						String val = null;
						if (stype.equals("userdata")) {
							Object obj = L.toJavaObject(i);
							if (obj != null)
								val = obj.toString();
						} else if (stype.equals("boolean")) {
							val = L.toBoolean(i) ? "true" : "false";
						} else {
							val = L.toString(i);
						}
						if (val == null)
							val = stype;						
						output.append(val);
						output.append("\t");
					}
					output.append("\n");					
					return 0;
				}
			};
			print.register("print");

			JavaFunction assetLoader = new JavaFunction(L) {
				@Override
				public int execute() throws LuaException {
					String name = L.toString(-1);

					AssetManager am = getAssets();
					try {
						InputStream is = am.open(name + ".lua");
						byte[] bytes = readAll(is);
						L.LloadBuffer(bytes, name);
						return 1;
					} catch (Exception e) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(os));
						L.pushString("Cannot load module "+name+":\n"+os.toString());
						return 1;
					}
				}
			};
			
			L.getGlobal("package");            // package
			L.getField(-1, "loaders");         // package loaders
			int nLoaders = L.objLen(-1);       // package loaders
			
			L.pushJavaFunction(assetLoader);   // package loaders loader
			L.rawSetI(-2, nLoaders + 1);       // package loaders
			L.pop(1);                          // package
						
			L.getField(-1, "path");            // package path
			String customPath = getFilesDir() + "/?.lua";
			L.pushString(";" + customPath);    // package path custom
			L.concat(2);                       // package pathCustom
			L.setField(-2, "path");            // package
			L.pop(1);
		} catch (Exception e) {
			status.setText("Cannot override print");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		serverThread = new ServerThread();
		serverThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		serverThread.stopped = true;
	}

	private class ServerThread extends Thread {
		public boolean stopped;

		@Override
		public void run() {
			stopped = false;
			try {
				ServerSocket server = new ServerSocket(LISTEN_PORT);
				show("Server started on port " + LISTEN_PORT);
				while (!stopped) {
					Socket client = server.accept();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(client.getInputStream()));
					final PrintWriter out = new PrintWriter(client.getOutputStream());
					String line = null;
					while (!stopped && (line = in.readLine()) != null) {
						final String s = line.replace('\001', '\n');
						if (s.startsWith("--mod:")) {
							int i1 = s.indexOf(':'), i2 = s.indexOf('\n');
							String mod = s.substring(i1+1,i2); 
							String file = getFilesDir()+"/"+mod.replace('.', '/')+".lua";
							FileWriter fw = new FileWriter(file);
							fw.write(s);
							fw.close();	
							// package.loaded[mod] = nil
							L.getGlobal("package");
							L.getField(-1, "loaded");
							L.pushNil();
							L.setField(-2, mod);
							out.println("wrote " + file + "\n");
							out.flush();
						} else {
							handler.post(new Runnable() {
								public void run() {
									String res = safeEvalLua(s);
									res = res.replace('\n', '\001');
									out.println(res);
									out.flush();
								}
							});
						}
					}
				}
				server.close();
			} catch (Exception e) {
				show(e.toString());
			}
		}

		private void show(final String s) {
			handler.post(new Runnable() {
				public void run() {
					status.setText(s);
				}
			});
		}
	}	

	String safeEvalLua(String src) {
		String res = null;	
		try {
			res = evalLua(src);
		} catch(LuaException e) {
			res = e.getMessage()+"\n";
		}
		return res;		
	}
	
	String evalLua(String src) throws LuaException {
		L.setTop(0);
		int ok = L.LloadString(src);
		if (ok == 0) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			ok = L.pcall(0, 0, -2);
			if (ok == 0) {				
				String res = output.toString();
				output.setLength(0);
				return res;
			}
		}
		throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
		//return null;		
		
	}

	public void onClick(View view) {
		String src = source.getText().toString();
		status.setText("");
		try {
			String res = evalLua(src);
			status.append(res);
			status.append("Finished succesfully");
		} catch(LuaException e) {			
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();			
		}

	}

	private String errorReason(int error) {
		switch (error) {
		case 4:
			return "Out of memory";
		case 3:
			return "Syntax error";
		case 2:
			return "Runtime error";
		case 1:
			return "Yield error";
		}
		return "Unknown error " + error;
	}

	public boolean onLongClick(View view) {
		source.setText("");
		return true;
	}
}