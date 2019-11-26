package com.example.chatapp2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvMessage;
        ImageView profileImage;
        public ViewHolder(View itemView){
            super(itemView);
            tvMessage=itemView.findViewById(R.id.tv_show_message);
            profileImage=itemView.findViewById(R.id.profile_image);
        }
    }
    public  static final int MSG_TYPE_LEFT=0;
    public  static final int MSG_TYPE_RIGHT=1;
    private Context mContext;
    private List<Chat> mChat;
    String imgageurl;
    FirebaseUser firebaseUser;
    public  MessageAdapter(Context context,List<Chat> chat,String imageurl){
        this.mContext=context;
        this.mChat=chat;
        this.imgageurl=imageurl;
    }
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else{
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
         Chat chat=mChat.get(position);
         holder.tvMessage.setText(chat.getMessage());
         if(imgageurl.equals("default")){
             holder.profileImage.setImageResource(R.mipmap.ic_launcher);
         }
         else{
             Glide.with(mContext).load(imgageurl).into(holder.profileImage);

         }

    }



    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }

        return MSG_TYPE_LEFT;
    }
}
