package com.cozybit.simple;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

// Proximity imports

import com.cozybit.proximity.ProximityAuth;
import com.cozybit.proximity.ProximityIntent;
import com.cozybit.proximity.ProximityInterface;
import com.cozybit.proximity.ProximityManager;
import com.cozybit.proximity.ProximityNetwork;
import com.cozybit.proximity.ProximityProfile;
import com.cozybit.proximity.mint.IMintChannelListener;
import com.cozybit.proximity.mint.IMintListener;
import com.cozybit.proximity.mint.Mint;
import com.cozybit.proximity.mint.MintChannel;

public class TestActivity extends Activity {

	protected static final String TAG = "SimpleApp";
	// UI stuff
	private Context mContext;
	private Spinner mSpinner;
	/*	private Button mButton1;
	private Button mButton2;
	private Button mButton3;*/
	private Button mButton4;
	private ToggleButton mToogleButton;

	private EditText mEditText;
	private ListView mListView;
	private ArrayList<String> mList;
	private ArrayAdapter<String> mListAdapter;

	// Proximity stuff
	private ProximityManager mManager;
	private ProximityNetwork mProximityNetwork;
	private Mint mMint;
	private MintChannel mMintChannel;
	private ProximityInterface mInterface = ProximityInterface.UNKNOWN;

	private IMintListener listener = new IMintListener() {

		@Override
		public void onStarted(String nodeName, int reason) {
			Toast.makeText(mContext, "Started Mint", Toast.LENGTH_SHORT).show();
			addToMessageList ("Mint has started");
			mMintChannel = mMint.joinChannel(mMint.getPublicChannelName(), new IMintChannelListener() {

				@Override
				public void onNodeLeft(String fromNode, String fromChannel) {
					String text = "<" + new String (fromNode) + "> " + " LEFT";
					// update message list
					addToMessageList (text);
				}

				@Override
				public void onNodeJoined(String fromNode, String fromChannel) {
					String text = "<" + new String (fromNode) + "> " + " JOINED";
					// update message list
					addToMessageList (text);
				}

				@Override
				public void onFileSent(String toNode, String toChannel, String fileName,
						String hash, String fileType, String exchangeId) {}

				@Override
				public void onFileReceived(String fromNode, String fromChannel,
						String fileName, String hash, String fileType, String exchangeId,
						long fileSize, String tmpFilePath) {}

				@Override
				public void onFileNotificationReceived(String fromNode, String fromChannel,
						String fileName, String hash, String fileType, String exchangeId,
						long fileSize) {}

				@Override
				public void onFileFailed(String node, String channel, String fileName,
						String hash, String exchangeId, int reason) {}

				@Override
				public void onFileChunkSent(String toNode, String toChannel,
						String fileName, String hash, String fileType, String exchangeId,
						long fileSize, long offset, long chunkSize) {}

				@Override
				public void onFileChunkReceived(String fromNode, String fromChannel,
						String fileName, String hash, String fileType, String exchangeId,
						long fileSize, long offset) {}

				@Override
				public void onDataReceived(String fromNode, String fromChannel,
						String payloadType, byte[][] payload) {
					if (payload.length == 1) {

						// get nickname and message text from payload
						String text = "<" + fromNode + "> "
								+ new String (payload[0]);
						// update message list
						addToMessageList (text);
					}
				}
			});
		}

		@Override
		public void onNetworkLost() {}

		@Override
		public void onError(int reason) {}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ProximityIntent.PROXIMITY_NETWORK_CONNECTED)) {
				Toast.makeText(context, "Got proximity network with Interface " +
						intent.getStringExtra(ProximityIntent.EXTRA_PROXIMITY_INTERFACE_NAME), Toast.LENGTH_SHORT).show();
				startMint();
			} else if (intent.getAction().equals(ProximityIntent.PROXIMITY_MANAGER_BOUND)) {
				// Manager has connected to service
				if (mManager != null)
					getProximityNetwork();
			} else if (intent.getAction().equals(ProximityIntent.PROXIMITY_MANAGER_UNBOUND)) {
				// Manager disconnected
				if (mToogleButton.isChecked())
					mToogleButton.toggle();

				mManager = null;
			}
		}
	};

	// UI stuff...

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		mContext = getApplicationContext();
		mList = new ArrayList<String>();

		mListAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mList);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mListAdapter);
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mListView.setStackFromBottom(true);

		mEditText = (EditText) findViewById(R.id.chat_text);


		mSpinner = (Spinner) findViewById(R.id.spinner1);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.networks, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mSpinner.setAdapter(adapter);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				switch (pos) {
				case 0:
					// Infra
					mInterface = ProximityInterface.INFRASTRUCTURE;
					break;
				case 1:
					// WFD
					mInterface = ProximityInterface.WIFI_DIRECT;
					break;
				case 2:
					// Mesh
					mInterface = ProximityInterface.MESH;
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				mInterface = ProximityInterface.UNKNOWN;
			}
		});

		mToogleButton = (ToggleButton) findViewById(R.id.toggleButton1);
		mToogleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Log.d(TAG, "Enabled");
					// disable spinner
					mSpinner.setEnabled(false);
					getProximityManager();

					// Only if we are already bound
					if (mManager.isBound())
						getProximityNetwork();

				} else {
					Log.d(TAG, "Disabled");
					if (mProximityNetwork != null)
						mProximityNetwork.releaseProximityNetwork();
					// Clear the messages
					clearMessageList();
					// enable spinner
					mSpinner.setEnabled(true);

				}
			}
		});

		mButton4 = (Button) findViewById(R.id.button4);

		mButton4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
	}

	public void addToMessageList(String text) {
		if (mToogleButton.isChecked()) {
			mList.add(text);
			mListAdapter.notifyDataSetChanged();
		}
	}

	public void clearMessageList() {
		mList.clear();
		mListAdapter.notifyDataSetChanged();
	}

	protected byte[][] createPayload(String data) {
		byte[][] payload = new byte[1][];
		payload[0] = data.getBytes();
		return payload;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

	// Proximity code

	public void sendMessage() {
		if (mMintChannel != null ) {
			mMintChannel.sendData(null, "message/mint", createPayload(
					mEditText.getText().toString()));
		}
		mEditText.getText().clear();
	}

	protected void startMint() {
		if (mProximityNetwork != null) {
			Toast.makeText(mContext, "Starting Mint", Toast.LENGTH_SHORT).show();
			mMint = mProximityNetwork.startMint(listener);
		}
	}

	protected void getProximityNetwork() {
		if (mManager != null) {
			Toast.makeText(mContext, "Getting ProximityNetwork", Toast.LENGTH_SHORT).show();
			try {
				ProximityAuth auth = ProximityAuth.PLAIN;
				mProximityNetwork = mManager.getProximityNetwork(
						mInterface,
						auth,
						ProximityProfile.DEFAULT);

				IntentFilter filter = new IntentFilter(ProximityIntent.PROXIMITY_NETWORK_CONNECTED);
				filter.addAction(ProximityIntent.PROXIMITY_NETWORK_DISCONNECTED);
				filter.addAction(ProximityIntent.PROXIMITY_NETWORK_FAILED);
				mProximityNetwork.registerReceiver(mReceiver, filter);
			} catch (IllegalStateException e) {
				Toast.makeText(mContext, "Error getting ProximityNetwork", Toast.LENGTH_SHORT).show();
			} catch (RemoteException e) {
				Toast.makeText(mContext, "Service doesn't respond", Toast.LENGTH_SHORT).show();
			}
			Toast.makeText(mContext, "Got ProximityNetwork", Toast.LENGTH_SHORT).show();
		}
	}

	protected void getProximityManager() {
		if (mManager == null) {
			Toast.makeText(mContext, "Getting ProximityManager", Toast.LENGTH_SHORT).show();

			mManager = new ProximityManager(mContext);

			IntentFilter filter = new IntentFilter(ProximityIntent.PROXIMITY_MANAGER_BOUND);
			filter.addAction(ProximityIntent.PROXIMITY_MANAGER_UNBOUND);

			registerReceiver(mReceiver, filter);
		} else
			Toast.makeText(mContext, "reusing ProximityManager", Toast.LENGTH_SHORT).show();
	}
}
