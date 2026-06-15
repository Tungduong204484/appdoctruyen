package com.example.appctruyn.auth;

import com.example.appctruyn.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class AuthManager {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // Đăng ký
    public static Task<User> register(String email, String password, String displayName, String role) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    
                    String uid = task.getResult().getUser().getUid();
                    User user = new User(uid, email, displayName, role);
                    
                    return db.collection("users").document(uid).set(user)
                            .continueWith(setTask -> {
                                if (!setTask.isSuccessful()) throw setTask.getException();
                                return user;
                            });
                });
    }

    // Đăng nhập
    public static Task<User> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    
                    String uid = task.getResult().getUser().getUid();
                    return db.collection("users").document(uid).get()
                            .continueWith(getTask -> {
                                if (!getTask.isSuccessful()) throw getTask.getException();
                                DocumentSnapshot doc = getTask.getResult();
                                User user = doc.toObject(User.class);
                                if (user == null) throw new Exception("Không tìm thấy thông tin người dùng");
                                return user;
                            });
                });
    }

    public static Task<User> getCurrentUserInfo() {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) return Tasks.forResult(null);
        
        return db.collection("users").document(firebaseUser.getUid()).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) return null;
                    return task.getResult().toObject(User.class);
                });
    }

    // CẬP NHẬT ẢNH DƯỚI DẠNG BASE64
    public static Task<String> updateAvatarBase64(String base64String) {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) return Tasks.forException(new Exception("Chưa đăng nhập"));
        
        return db.collection("users").document(firebaseUser.getUid())
                .update("avatarUrl", base64String)
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return base64String;
                });
    }

    // Admin: Lấy danh sách người dùng
    public static Task<List<User>> getAllUsers() {
        return db.collection("users").orderBy("createdAt").get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return task.getResult().toObjects(User.class);
                });
    }

    // Admin: Cập nhật role
    public static Task<Void> updateUserRole(String uid, String newRole) {
        return db.collection("users").document(uid).update("role", newRole);
    }

    public static void logout() {
        auth.signOut();
    }
}
