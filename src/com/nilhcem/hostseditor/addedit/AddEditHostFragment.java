package com.nilhcem.hostseditor.addedit;

import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.Views;

import com.nilhcem.hostseditor.R;
import com.nilhcem.hostseditor.bus.event.CreatedHostEvent;
import com.nilhcem.hostseditor.core.BaseFragment;
import com.nilhcem.hostseditor.core.Host;
import com.nilhcem.hostseditor.util.InetAddresses;

public class AddEditHostFragment extends BaseFragment implements OnClickListener {
	public static final String TAG = "AddHostFragment";
	private static final Pattern HOSTNAME_INVALID_CHARS_PATTERN = Pattern.compile("^.*[#'\",\\\\]+.*$");

	private Host mInitialHost; // "edit mode" only - null for "add mode"
	private AlertDialog mErrorAlert;

	@InjectView(R.id.addEditHostIp) EditText mIp;
	@InjectView(R.id.addEditHostName) EditText mHostName;
	@InjectView(R.id.addEditComment) EditText mComment;
	@InjectView(R.id.addEditCommentLabel) TextView mCommentLabel;
	@InjectView(R.id.addEditHostButton) Button mButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_edit_host_layout, container, false);
		Views.inject(this, view);
		mButton.setOnClickListener(this);

		if (mInitialHost == null) {
			mButton.setText(R.string.add_host_title);
		} else {
			mIp.setText(mInitialHost.getIp());
			mHostName.setText(mInitialHost.getHostName());
			mButton.setText(R.string.edit_host_title);

			String comment = mInitialHost.getComment();
			if (!TextUtils.isEmpty(comment)) {
				mComment.setText(comment);
				toggleCommentVisibility();
			}
		}
		return view;
	}

	@Override
	public void onStop() {
		if (mErrorAlert != null) {
			mErrorAlert.dismiss();
		}
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addEditHostButton) {
			String ip = mIp.getText().toString();
			String hostname = mHostName.getText().toString();
			String comment = mComment.getText().toString();
			if (TextUtils.isEmpty(comment)) {
				comment = null;
			}
			int error = checkFormErrors(ip, hostname);

			if (error == 0) {
				Host edited = new Host(ip, hostname, comment, false, true);
				mBus.post(new CreatedHostEvent(mInitialHost, edited));
			} else {
				mErrorAlert = new AlertDialog.Builder(mActivity)
					.setTitle(R.string.add_edit_error_title)
					.setMessage(error)
					.setCancelable(true)
					.setNeutralButton(R.string.add_edit_error_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Do nothing
						}
					})
					.create();
				mErrorAlert.show();
			}
		}
	}

	public void setHostToEdit(Host toEdit) {
		mInitialHost = toEdit;
	}

	public boolean hasComment() {
		return mComment.getVisibility() == View.VISIBLE;
	}

	public void toggleCommentVisibility() {
		int visibility;
		if (hasComment()) {
			visibility = View.GONE;
			mComment.setText("");
		} else {
			visibility = View.VISIBLE;
		}

		mComment.setVisibility(visibility);
		mCommentLabel.setVisibility(visibility);
	}

	private int checkFormErrors(String ip, String hostname) {
		int error = 0;

		if (TextUtils.isEmpty(hostname) || HOSTNAME_INVALID_CHARS_PATTERN.matcher(hostname).matches()) {
			error = R.string.add_edit_error_hostname;
		}
		if (TextUtils.isEmpty(ip) || !InetAddresses.isInetAddress(ip)) {
			error = R.string.add_edit_error_ip;
		}
		return error;
	}
}
