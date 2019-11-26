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

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter <UserAdapter.ViewHolder>{
     class ViewHolder extends RecyclerView.ViewHolder{
         TextView tvUsername;
         ImageView profileImage;
        public ViewHolder(View itemView){
            super(itemView);
            tvUsername=itemView.findViewById(R.id.tv_username);
            profileImage=itemView.findViewById(R.id.profile_image);
        }
    }
    private Context mContext;
    private List<User> mUsers;
    public  UserAdapter(Context context,List<User> users){
        this.mContext=context;
        this.mUsers=users;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user=mUsers.get(position);
        holder.tvUsername.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
