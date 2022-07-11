package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelWithdraw;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ALL")
public class AdapterWithdraw extends RecyclerView.Adapter<AdapterWithdraw.MyHolder>{

    final Context context;
    final List<ModelWithdraw> userList;

    public AdapterWithdraw(Context context, List<ModelWithdraw> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.withdraw_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.date.setText(getDate(Long.parseLong(userList.get(position).getId())) + " of $" + userList.get(position).getAmount());
        holder.type.setText(userList.get(position).getType());

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        TextView date,type;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            type = itemView.findViewById(R.id.type);

        }

    }
}
