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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
public class UserAdapter extends RecyclerView.Adapter <UserAdapter.ViewHolder>{
     class ViewHolder extends RecyclerView.ViewHolder{
         TextView tvUsername;
         ImageView profileImage;
         ImageView newMessageImage;
         private ImageView imgOn;
         private  ImageView imgOff;
         TextView tvLastMsg;
        public ViewHolder(View itemView){
            super(itemView);
            tvUsername=itemView.findViewById(R.id.tv_username);
            profileImage=itemView.findViewById(R.id.profile_image);
            imgOn=itemView.findViewById(R.id.img_on);
            imgOff=itemView.findViewById(R.id.img_off);
            tvLastMsg=itemView.findViewById(R.id.tv_last_msg);
            newMessageImage=itemView.findViewById(R.id.img_new_message);
        }
    }
    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    String lastMsg;
    public  UserAdapter(Context context,List<User> users,boolean isChat){
        this.mContext=context;
        this.mUsers=users;
        this.isChat=isChat;
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
        if(isChat){
            lastMessage(user.getId(),holder.tvLastMsg);
        }
        else{
            holder.tvLastMsg.setVisibility(View.GONE);
        }
        if(isChat){
            if(user.getStatus().equals("online")){
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            }
            else{
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);

            }
        }
        isSeenMessage(user.getId(),holder.newMessageImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference charReceiver=FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(firebaseUser.getUid()).child(user.getId());
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("isseen",true);
                charReceiver.updateChildren(hashMap);
                Intent intent=new Intent(mContext,MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });
    }
    private void isSeenMessage(final String userid,final ImageView imageView){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            final DatabaseReference seenListener=FirebaseDatabase.getInstance().getReference("Chatlist")
                    .child(firebaseUser.getUid()).child(userid);
            seenListener.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Chatlist chat=dataSnapshot.getValue(Chatlist.class);
                        if(!chat.isIsseen()){
                            imageView.setVisibility(View.VISIBLE);
                        }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
    private void lastMessage(final String userid, final TextView tvLastMsg){
        lastMsg="default";
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);
                    if(firebaseUser!=null){
                        if(chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userid)||
                        chat.getSender().equals(firebaseUser.getUid())&&chat.getReceiver().equals(userid)){
                            lastMsg=chat.getMessage();
                        }
                    }
                }
                switch (lastMsg){
                    case "default":
                        tvLastMsg.setText("No message");
                        break;
                    default:
                        tvLastMsg.setText(lastMsg);
                        break;
                }
                lastMsg="default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
