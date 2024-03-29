package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageActivity extends AppCompatActivity {
    CircleImageView profileImage;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    Intent intent;
    Toolbar toolbar;
    ImageButton btnSend,btnSendFiles;
    EditText edtMessage;
    MessageAdapter messageAdapter;
    List<Chat> chats;
    RecyclerView recyclerView;
    ValueEventListener seenListener;
    String userid;
    StorageReference storageReference;
    String checker,image;
    Uri imageUri;
    StorageTask uploadTask;
    boolean notify,blocked,beBlocked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        init();
        notify=false;
        //block user
        blocked=false;
        //be blocked by user
        beBlocked=false;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        intent=getIntent();
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        userid=intent.getStringExtra("userid");
        final DatabaseReference charRef=FirebaseDatabase.getInstance().getReference("Blocklist")
                .child(firebaseUser.getUid())
                .child(userid);
        /*
            Check if the message have been blocked, be blocked
        */
        charRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    beBlocked= dataSnapshot.getValue(Blocklist.class).isBeBlocked();
                    blocked=dataSnapshot.getValue(Blocklist.class).isBlocked();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                String msg=edtMessage.getText().toString();
                /*
                    if current users have been blocked,they couldn't send the message
                */
                if(beBlocked){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                    builder.setMessage("Sorry! You have been blocked by this user")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Handle Ok
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Handle Cancel
                                }
                            })
                            .create();
                    builder.show();
                }
                if(!msg.equals("")&&!beBlocked){
                    sendMessage(firebaseUser.getUid(),userid,msg,"");
                }
                else{
                    Toast.makeText(MessageActivity.this,"Can't send message",Toast.LENGTH_SHORT).show();
                }
                edtMessage.setText("");
            }
        });
        //send file set up
        btnSendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]=new CharSequence[]{
                        "Images"
                };
                /*
                    Create an alert dialog to notify about choose image
                */
                AlertDialog.Builder builder=new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i==0){
                            checker="image";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select image"),1);

                        }

                    }
                });
                builder.show();
            }
        });

        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);

                }
                readMessage(firebaseUser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            imageUri=data.getData();
            if(!checker.equals("image")){

            }
            else if(checker.equals("image")){
                uploadImage();
            }
            else{
                Toast.makeText(this,"No selected",Toast.LENGTH_SHORT).show(); ;
            }

        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=MessageActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    /*
        Method upload image from device to firebase storage
    */
    private void uploadImage() {
        final ProgressDialog progress = new ProgressDialog(MessageActivity.this);
        progress.setMessage("Uploading");
        progress.show();
        if (imageUri != null) {
            final StorageReference file = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = file.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>(){
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();

                    }
                    image=file.getDownloadUrl().toString();
                    return file.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        String nUri=downloadUri.toString();
                        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String,Object> map=new HashMap<>();
                        map.put("imageURL",nUri);
                        reference.updateChildren(map);
                        progress.dismiss();
                        sendMessage(firebaseUser.getUid(),userid,"",nUri);
                    }else{
                        Toast.makeText(MessageActivity.this,"Fail",Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        }
    }
    public void init(){
        profileImage=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        toolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnSend=findViewById(R.id.btn_send);
        btnSendFiles=findViewById(R.id.btn_sendMedia);
        edtMessage=findViewById(R.id.edt_send);
        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        storageReference= FirebaseStorage.getInstance().getReference("messageImages");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_message,menu);
        return true;
    }
    /*
        display block icon/ unblock icon
    */
    public boolean onPrepareOptionsMenu(Menu menu)
    {

        MenuItem block = menu.findItem(R.id.block);
        MenuItem unblock=menu.findItem(R.id.unblock);
        if(blocked)
        {
            block.setVisible(false);
            unblock.setVisible(true);
        }
        else
        {
            block.setVisible(true);
            unblock.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.block:{
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setMessage("Do you sure to block this user ?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                doBlock();
                                Intent intent=new Intent(MessageActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Handle Cancel
                            }
                        });
                AlertDialog dialog = builder.create();

                dialog.show();
                return true;
            }
            case R.id.unblock:{
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setMessage("Do you sure to unblock this user ?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                doUnBlock();
                                Intent intent=new Intent(MessageActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Handle Cancel
                            }
                        })
                        .create();
                builder.show();
                return true;
            }
        }
        return false;
    }
    // do block user
    public void doBlock(){
        final DatabaseReference charRef=FirebaseDatabase.getInstance().getReference("Blocklist")
                .child(userid)
                .child(firebaseUser.getUid());
        charRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    charRef.child("beBlocked").setValue(true);
                    charRef.child("blocked").setValue(false);
                }
                else{
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("beBlocked",true);
                    hashMap.put("blocked",false);
                    dataSnapshot.getRef().updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference charRec=FirebaseDatabase.getInstance().getReference("Blocklist")
                .child(firebaseUser.getUid())
                .child(userid);
        charRec.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    charRec.child("blocked").setValue(true);
                    charRec.child("beBlocked").setValue(false);
                }
                else{
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("blocked",true);
                    hashMap.put("beBlocked",false);
                    dataSnapshot.getRef().updateChildren(hashMap);

                }
                blocked=true;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // do unBlock user
    public void doUnBlock(){
        final DatabaseReference charRef=FirebaseDatabase.getInstance().getReference("Blocklist")
                .child(userid)
                .child(firebaseUser.getUid());
        charRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    charRef.child("beBlocked").setValue(false);
                    charRef.child("blocked").setValue(false);
                }
                else{
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("beBlocked",false);
                    hashMap.put("blocked",false);
                    dataSnapshot.getRef().updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference charRec=FirebaseDatabase.getInstance().getReference("Blocklist")
                .child(firebaseUser.getUid())
                .child(userid);
        charRec.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    charRec.child("blocked").setValue(false);
                    charRec.child("beBlocked").setValue(false);
                }
                else{
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("blocked",false);
                    hashMap.put("beBlocked",false);
                    dataSnapshot.getRef().updateChildren(hashMap);

                }
                blocked=false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /*
        check message have been seen yet, update message status to firebase
    */
    private void seenMessage(final String userid){
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // send message
    public void sendMessage(String sender, final String receiver, String message,String imgUrl){
        
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("image",imgUrl);
        hashMap.put("isseen",false);
        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference charRef=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);
        charRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    charRef.child("id").setValue(userid);
                    charRef.child("isseen").setValue(true);
                    charRef.child("receiver").setValue(receiver);
                }
                else{
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("id",userid);
                    hashMap.put("isseen",true);
                    hashMap.put("receiver",receiver);
                    dataSnapshot.getRef().updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference charReceiver=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(receiver)
                .child(firebaseUser.getUid());
        charReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    charReceiver.child("id").setValue(firebaseUser.getUid());
                    charReceiver.child("isseen").setValue(false);
                    charReceiver.child("receiver").setValue(firebaseUser.getUid());
                }
                else{
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("id",firebaseUser.getUid());
                    hashMap.put("isseen",false);
                    hashMap.put("receiver",firebaseUser.getUid());
                    dataSnapshot.getRef().updateChildren(hashMap);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //method display message from another user
    public void readMessage(final String myid, final String userid, final String imageurl){
        chats=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid)&&chat.getSender().equals(userid)||
                            chat.getReceiver().equals(userid)&&chat.getSender().equals(myid)){
                        chats.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,chats,imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }

}
