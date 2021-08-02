package com.hhuc.smartdoor.modules.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hhuc.smartdoor.R;
import com.hhuc.smartdoor.ui.account.Account;

public class ChangePwdDialog extends DialogFragment implements View.OnClickListener {
    private EditText OriginPwd;
    private EditText NewPwd;
    private EditText RepeatPwd;
    private Button btn;
    private ImageView iv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_changepwd, null);
        iv = view.findViewById(R.id.login_iv);
        OriginPwd = view.findViewById(R.id.login_et1);
        NewPwd = view.findViewById(R.id.login_et2);
        RepeatPwd = view.findViewById(R.id.login_et3);
        btn = view.findViewById(R.id.login_btn);
        iv.setOnClickListener(this);
        btn.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_btn) {
            if (getTargetFragment() == null) {
                return;
            }
            if (OriginPwd.getText().toString().isEmpty())
                Toast.makeText(getActivity(), "原密码不能为空!", Toast.LENGTH_SHORT).show();
            else if (NewPwd.getText().toString().isEmpty())
                Toast.makeText(getActivity(), "新密码不能为空!", Toast.LENGTH_SHORT).show();
            else if (RepeatPwd.getText().toString().isEmpty())
                Toast.makeText(getActivity(), "重复新密码不能为空!", Toast.LENGTH_SHORT).show();
            else if (!NewPwd.getText().toString().equals(RepeatPwd.getText().toString()))
                Toast.makeText(getActivity(), "两次输入新密码不一致！", Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent();
                intent.putExtra("OriginPassword", OriginPwd.getText().toString());
                intent.putExtra("NewPassword", NewPwd.getText().toString());
                getTargetFragment().onActivityResult(Account.REQUEST_CODE, Activity.RESULT_OK, intent);
            }
        } else if (v.getId() == R.id.login_iv) {
            dismiss();
        }
    }

}
