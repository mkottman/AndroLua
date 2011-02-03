package sk.kottman.androlua;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener,
		OnLongClickListener {
	private final static int LISTEN_PORT = 3333;

	Button execute;
	EditText source;
	TextView status;
	LuaState L;
	
	Handler handler;
	ServerThread serverThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		execute = (Button) findViewById(R.id.executeBtn);
		execute.setOnClickListener(this);

		source = (EditText) findViewById(R.id.source);
		source.setOnLongClickListener(this);

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
					StringBuilder sb = new StringBuilder();
					for (int i = 2; i <= L.getTop(); i++) {
						int type = L.type(i);
						String val = L.toString(i);
						if (val == null)
							val = L.typeName(type);
						sb.append(val);
						sb.append("\t");
					}
					sb.append("\n");
					status.append(sb.toString());
					return 0;
				}
			};
			print.register("print");
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
					String line = null;
					while (!stopped && (line = in.readLine()) != null) {
						final String s = line;
						handler.post(new Runnable() {
							@Override
							public void run() {
								source.append(s + "\n");
							}
						});
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

	@Override
	public void onClick(View view) {
		String src = source.getText().toString();

		status.setText("");
		L.setTop(0);
		int ok = L.LloadString(src);
		if (ok == 0) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			ok = L.pcall(0, 0, -2);
			if (ok == 0) {
				status.append("Finished succesfully");
				return;
			}
		}

		String reason = errorReason(ok);
		status.setText(reason + ": " + L.toString(-1));
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

	@Override
	public boolean onLongClick(View view) {
		source.setText("");
		return true;
	}
}