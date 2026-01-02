//package com.example.viaterra
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.common.SignInButton
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import com.google.firebase.firestore.FirebaseFirestore
//
//class SignupActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore
//    private lateinit var googleSignInClient: GoogleSignInClient
//    private val RC_SIGN_IN = 1001
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_signup)
//
//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//
//        val emailField = findViewById<EditText>(R.id.etSignupEmail)
//        val usernameField = findViewById<EditText>(R.id.etSignupUsername)
//        val passwordField = findViewById<EditText>(R.id.etSignupPassword)
//        val signupBtn = findViewById<Button>(R.id.btnSignup)
//        val goToLogin = findViewById<TextView>(R.id.tvGoToLogin)
//        val googleButton = findViewById<ImageView>(R.id.btnGoogleSignUp)
//
//        // Configure Google Sign-In
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        googleSignInClient = GoogleSignIn.getClient(this, gso)
//
//        // Google Sign-In button
//        googleButton.setOnClickListener {
//            val signInIntent = googleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)
//        }
//
//        // Normal Email/Username signup
//        signupBtn.setOnClickListener {
//            val emailText = emailField.text.toString().trim()
//            val usernameText = usernameField.text.toString().trim()
//            val passText = passwordField.text.toString().trim()
//
//            if (emailText.isEmpty() || usernameText.isEmpty() || passText.isEmpty()) {
//                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Email validation
//            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
//            if (!emailPattern.matches(emailText)) {
//                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Username validation
//            val usernamePattern = Regex("^[a-z0-9_]{3,15}$")
//            if (!usernamePattern.matches(usernameText)) {
//                Toast.makeText(this, "Username must be 3-15 characters, lowercase letters, numbers, or _ only", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }
//
//            // Check if username exists
//            db.collection("Users")
//                .whereEqualTo("username", usernameText)
//                .get()
//                .addOnSuccessListener { docs ->
//                    if (!docs.isEmpty) {
//                        Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
//                    } else {
//                        // Create user in Firebase Auth
//                        auth.createUserWithEmailAndPassword(emailText, passText)
//                            .addOnCompleteListener { task ->
//                                if (task.isSuccessful) {
//                                    val user = auth.currentUser
//                                    val userId = user!!.uid
//
//                                    // Save username/email in Firestore
//                                    val userMap = hashMapOf(
//                                        "username" to usernameText,
//                                        "email" to emailText
//                                    )
//                                    db.collection("Users").document(userId).set(userMap)
//
//                                    // Send email verification
//                                    user.sendEmailVerification()
//                                        .addOnCompleteListener { verifyTask ->
//                                            if (verifyTask.isSuccessful) {
//                                                Toast.makeText(
//                                                    this,
//                                                    "Account created. Verify email before login.",
//                                                    Toast.LENGTH_LONG
//                                                ).show()
//                                                startActivity(Intent(this, LoginActivity::class.java))
//                                                finish()
//                                            } else {
//                                                Toast.makeText(this, "Failed to send verification email: ${verifyTask.exception?.message}", Toast.LENGTH_LONG).show()
//                                            }
//                                        }
//                                } else {
//                                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
//                                }
//                            }
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
//
//        goToLogin.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)!!
//                firebaseAuthWithGoogle(account)
//            } catch (e: ApiException) {
//                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
//        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser!!
//                    val userMap = hashMapOf(
//                        "username" to (user.displayName ?: "GoogleUser"),
//                        "email" to user.email
//                    )
//                    db.collection("Users").document(user.uid).set(userMap)
//                    Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show()
//                    startActivity(Intent(this, DashboardActivity::class.java))
//                    finish()
//                } else {
//                    Toast.makeText(this, "Firebase auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//    }
//
//    private fun checkPasswordStrength(password: String): Pair<Boolean, String> {
//        if (password.length < 8)
//            return Pair(false, "Password must be at least 8 characters")
//
//        if (!password.any { it.isUpperCase() })
//            return Pair(false, "Password must contain an uppercase letter")
//
//        if (!password.any { it.isLowerCase() })
//            return Pair(false, "Password must contain a lowercase letter")
//
//        if (!password.any { it.isDigit() })
//            return Pair(false, "Password must contain a number")
//
//        if (!password.any { "!@#$%^&*()_+-=[]{}|;:'\",.<>?/".contains(it) })
//            return Pair(false, "Password must contain a special character")
//
//        return Pair(true, "Strong password 💪")
//    }
//
//}

package com.example.viaterra

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailField = findViewById<EditText>(R.id.etSignupEmail)
        val usernameField = findViewById<EditText>(R.id.etSignupUsername)
        val passwordField = findViewById<EditText>(R.id.etSignupPassword)
        val signupBtn = findViewById<Button>(R.id.btnSignup)
        val goToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val googleButton = findViewById<ImageView>(R.id.btnGoogleSignUp)

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleButton.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }

        signupBtn.setOnClickListener {
            val emailText = emailField.text.toString().trim()
            val usernameText = usernameField.text.toString().trim()
            val passText = passwordField.text.toString().trim()

            if (emailText.isEmpty() || usernameText.isEmpty() || passText.isEmpty()) {
                toast("Fill all fields")
                return@setOnClickListener
            }

            // Email validation
            val emailPattern =
                Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
            if (!emailPattern.matches(emailText)) {
                toast("Enter a valid email")
                return@setOnClickListener
            }

            // Username validation
            val usernamePattern = Regex("^[a-z0-9_]{3,15}$")
            if (!usernamePattern.matches(usernameText)) {
                toast("Username must be 3–15 chars, lowercase, numbers or _")
                return@setOnClickListener
            }

            // 🔐 Password strength check
            val (isStrong, message) = checkPasswordStrength(passText)
            if (!isStrong) {
                toast(message)
                return@setOnClickListener
            }

            // Check if username already exists
            db.collection("Users")
                .whereEqualTo("username", usernameText)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        toast("Username already taken")
                    } else {
                        createUser(emailText, passText, usernameText)
                    }
                }
                .addOnFailureListener {
                    toast("Database error: ${it.message}")
                }
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun createUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email
                    )

                    db.collection("Users").document(user.uid).set(userMap)

                    user.sendEmailVerification().addOnSuccessListener {
                        toast("Account created. Verify email before login.")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    toast(task.exception?.message ?: "Signup failed")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                toast("Google sign-in failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = auth.currentUser!!
                val userMap = hashMapOf(
                    "username" to (user.displayName ?: "GoogleUser"),
                    "email" to user.email
                )
                db.collection("Users").document(user.uid).set(userMap)

                toast("Google sign-in successful")
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                toast("Firebase auth failed")
            }
        }
    }

    // 🔐 Password strength logic
    private fun checkPasswordStrength(password: String): Pair<Boolean, String> {
        if (password.length < 8)
            return Pair(false, "Password must be at least 8 characters")

        if (!password.any { it.isUpperCase() })
            return Pair(false, "Add an uppercase letter")

        if (!password.any { it.isLowerCase() })
            return Pair(false, "Add a lowercase letter")

        if (!password.any { it.isDigit() })
            return Pair(false, "Add a number")

        if (!password.any { "!@#$%^&*()_+-=[]{}|;:'\",.<>?/".contains(it) })
            return Pair(false, "Add a special character")

        return Pair(true, "Strong password")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
