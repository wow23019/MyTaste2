package com.example.mytaste2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailInput , passInput;
    private Button signUpBtn;
    private TextView signInText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar=getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        emailInput = findViewById(R.id.edit_email_reg);
        passInput = findViewById(R.id.edit_pass_reg);
        signInText = findViewById(R.id.sign_in_text);
        signUpBtn = findViewById(R.id.btn_signup);

        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this , LoginActivity.class));
                finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String pass = passInput.getText().toString();
                if (!email.isEmpty() || !pass.isEmpty()){
                    mAuth.createUserWithEmailAndPassword(email , pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                startActivity(new Intent(RegistrationActivity.this , LoginActivity.class));
                                finish();
                            }else{
                                Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(RegistrationActivity.this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}