package com.example.appctruyn.auth

import com.example.appctruyn.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    // Đăng ký
    suspend fun register(email: String, password: String, displayName: String, role: String = "reader"): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Không thể tạo tài khoản")
            val user = User(uid = uid, email = email, displayName = displayName, role = role)
            db.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Đăng nhập
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Đăng nhập thất bại")
            val userDoc = db.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("Không tìm thấy thông tin")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserInfo(): User? {
        val uid = currentUser?.uid ?: return null
        return try {
            db.collection("users").document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // CẬP NHẬT ẢNH DƯỚI DẠNG BASE64 (Không cần Firebase Storage)
    suspend fun updateAvatarBase64(base64String: String): Result<String> {
        val uid = currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid).update("avatarUrl", base64String).await()
            Result.success(base64String)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin: Lấy danh sách người dùng
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = db.collection("users").orderBy("createdAt").get().await()
            Result.success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin: Cập nhật role
    suspend fun updateUserRole(uid: String, newRole: String): Result<Unit> {
        return try {
            db.collection("users").document(uid).update("role", newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() { auth.signOut() }
}
