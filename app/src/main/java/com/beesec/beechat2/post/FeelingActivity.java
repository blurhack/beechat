package com.beesec.beechat2.post;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.beesec.beechat2.R;

public class FeelingActivity  extends DialogFragment {

    public static FeelingActivity newInstance() {
        return new FeelingActivity();
    }

    CallBack callBack;
    String type = "";
    TextView title;
    EditText value;

    public void setCallBack(CallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.activity_feelings, container, false);

      //Feeling
      view.findViewById(R.id.feeling).setOnClickListener(v -> {
          view.findViewById(R.id.main).setVisibility(View.GONE);
          view.findViewById(R.id.imageView).setVisibility(View.GONE);
          view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
          view.findViewById(R.id.stickers).setVisibility(View.VISIBLE);
      });

      //Main
        view.findViewById(R.id.imageViewTwo).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.VISIBLE);
            view.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.stickers).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.GONE);
            view.findViewById(R.id.input).setVisibility(View.GONE);
        });

        //back
        view.findViewById(R.id.imageView).setOnClickListener(v -> dismiss());

        //Input
        view.findViewById(R.id.traveling).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "traveling";
            setText();
        });

        view.findViewById(R.id.watching).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "watching";
            setText();
        });

        view.findViewById(R.id.listening).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "listening";
            setText();
        });

        view.findViewById(R.id.thinking).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "thinking";
            setText();
        });

        view.findViewById(R.id.celebrating).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "celebrating";
            setText();
        });

        view.findViewById(R.id.looking).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "looking";
            setText();
        });

        view.findViewById(R.id.playing).setOnClickListener(v -> {
            view.findViewById(R.id.main).setVisibility(View.GONE);
            view.findViewById(R.id.imageView).setVisibility(View.GONE);
            view.findViewById(R.id.imageViewTwo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.input).setVisibility(View.VISIBLE);
            type = "playing";
            setText();
        });

        //EditText
         title = view.findViewById(R.id.title);
         value = view.findViewById(R.id.value);

         //Send
        view.findViewById(R.id.next).setOnClickListener(v -> {
            if (value.getText().toString().isEmpty()){
                Snackbar.make(v, "Type what are you doing", Snackbar.LENGTH_LONG).show();
            }else {
                callBack.onActionClick(type, value.getText().toString());
                dismiss();
            }
        });

        //feeling
        view.findViewById(R.id.happy).setOnClickListener(v -> {
            callBack.onActionClick("happy", "");
            dismiss();
        });
        view.findViewById(R.id.loved).setOnClickListener(v -> {
            callBack.onActionClick("loved", "");
            dismiss();
        });
        view.findViewById(R.id.sad).setOnClickListener(v -> {
            callBack.onActionClick("sad", "");
            dismiss();
        });
        view.findViewById(R.id.crying).setOnClickListener(v -> {
            callBack.onActionClick("crying", "");
            dismiss();
        });
        view.findViewById(R.id.angry).setOnClickListener(v -> {
            callBack.onActionClick("angry", "");
            dismiss();
        });
        view.findViewById(R.id.confused).setOnClickListener(v -> {
            callBack.onActionClick("confused", "");
            dismiss();
        });
        view.findViewById(R.id.broken).setOnClickListener(v -> {
            callBack.onActionClick("broken", "");
            dismiss();
        });
        view.findViewById(R.id.cool).setOnClickListener(v -> {
            callBack.onActionClick("cool", "");
            dismiss();
        });
        view.findViewById(R.id.funny).setOnClickListener(v -> {
            callBack.onActionClick("funny", "");
            dismiss();
        });
        view.findViewById(R.id.tired).setOnClickListener(v -> {
            callBack.onActionClick("tired", "");
            dismiss();
        });
        view.findViewById(R.id.shock).setOnClickListener(v -> {
            callBack.onActionClick("shock", "");
            dismiss();
        });
        view.findViewById(R.id.love).setOnClickListener(v -> {
            callBack.onActionClick("love", "");
            dismiss();
        });
        view.findViewById(R.id.sleepy).setOnClickListener(v -> {
            callBack.onActionClick("sleepy", "");
            dismiss();
        });
        view.findViewById(R.id.expressionless).setOnClickListener(v -> {
            callBack.onActionClick("expressionless", "");
            dismiss();
        });
        view.findViewById(R.id.blessed).setOnClickListener(v -> {
            callBack.onActionClick("blessed", "");
            dismiss();
        });

        return view;
    }

    @SuppressLint("SetTextI18n")
    public void setText(){

        switch (type) {
            case "traveling":
                title.setText("Where are you traveling to ?");
                value.setHint("Example :- California City");
                break;
            case "watching":
                title.setText("What are you watching ?");
                value.setHint("Example :- movie/series name");
                break;
            case "listening":
                title.setText("What are you listening ?");
                value.setHint("Example :- song name");
                break;
            case "thinking":
                title.setText("What are you thinking about ?");
                value.setHint("Example :- life/old memories");
                break;
            case "celebrating":
                title.setText("What are you celebrating ?");
                value.setHint("Example :- birthday/christmas");
                break;
            case "looking":
                title.setText("What are you looking for ?");
                value.setHint("Example :- help/advice");
                break;
            case "playing":
                title.setText("What are you playing ?");
                value.setHint("Example :- game name");
                break;
        }

    }

    public interface CallBack {
        void onActionClick(String type, String value);
    }

}
