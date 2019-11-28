package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText edtUsername,edtEmail,edtPassword,edtRePassword;
    Button btnRegister;
    FirebaseAuth auth;
    DatabaseReference reference;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username=edtUsername.getText().toString();
                String email=edtEmail.getText().toString();
                String password=edtPassword.getText().toString();
                String repassword=edtRePassword.getText().toString();
                if(TextUtils.isEmpty(username)||TextUtils.isEmpty(email)||TextUtils.isEmpty(password)||TextUtils.isEmpty(repassword)){
                    Toast.makeText(RegisterActivity.this,"Please insert all value",Toast.LENGTH_LONG).show();
                }else if(password.length()<6){
                    Toast.makeText(RegisterActivity.this,"Length of password must not be below 6",Toast.LENGTH_SHORT).show();
                }
                  else  if(!password.equals(repassword)){
                    Toast.makeText(RegisterActivity.this,"Password and Re-Password not match",Toast.LENGTH_SHORT).show();
                }
                  else {
                      register(username,email,password);
                }
            }
        });
    }
    public void init(){
        edtUsername=findViewById(R.id.edt_register_username);
        edtEmail=findViewById(R.id.edt_register_email);
        edtPassword=findViewById(R.id.edt_register_password);
        edtRePassword=findViewById(R.id.edt_register_repassword);
        btnRegister=findViewById(R.id.btn_register);
        auth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");

    }
    public void register(final String username, String email, String password){
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser=auth.getCurrentUser();
                            assert firebaseUser!=null;
                            String userId=firebaseUser.getUid();
                            reference= FirebaseDatabase.getInstance().getReference("Users").child(userId);
                            HashMap<String,String> hashMap=new HashMap<>();
                            hashMap.put("id",userId);
                            hashMap.put("username",username);
                            hashMap.put("imageURL","default");
                            hashMap.put("search",username.toLowerCase());
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(RegisterActivity.this,"You can not register",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
