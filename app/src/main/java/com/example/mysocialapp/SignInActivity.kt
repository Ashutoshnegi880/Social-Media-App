package com.example.mysocialapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mysocialapp.daos.UserDao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("128623848327-14vvlnbjmfrnj5cmn6gg422gt2ur2cm0.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        signInButton.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback{result ->
            if (result.resultCode == RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }
    )
//    private fun signIn() {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {
                updateUI(firebaseUser)
            }
        }

    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null) {
            val user = com.example.mysocialapp.models.User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.photoUrl.toString())
            val userDao = UserDao()
            userDao.addUser(user)

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            signInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}