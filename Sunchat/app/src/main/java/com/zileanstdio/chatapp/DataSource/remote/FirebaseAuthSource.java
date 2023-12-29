package com.zileanstdio.chatapp.DataSource.remote;

import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Exceptions.PhoneNumberException;
import com.zileanstdio.chatapp.Exceptions.UserException;
import com.zileanstdio.chatapp.Exceptions.VerificationException;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Constants;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.Stringee;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FirebaseAuthSource {
    private static final String TAG = "FirebaseAuthSource";

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Inject
    public FirebaseAuthSource(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Completable checkExistedPhoneNumber(final String phoneNumber) {
        return Completable.create(emitter ->
                firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                    .document(CipherUtils.Hash.sha256(phoneNumber))
                    .get()
                    .addOnSuccessListener(command -> {
                        if(command.exists()) {
                            emitter.onError(
                                new PhoneNumberException(PhoneNumberException.ErrorType.HAS_ALREADY_EXISTED,
                                "Số điện thoại này đã tồn tại"));
                        } else {
                            emitter.onComplete();
                        }
                     })
                    .addOnFailureListener(emitter::onError));
    }

    public Completable signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        return Completable.create(emitter -> {
            firebaseAuth.signInWithCredential(phoneAuthCredential)
                    .addOnSuccessListener(command -> {
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "The verification code entered was invalid");
                        emitter.onError(new VerificationException(VerificationException.ErrorType.INVALID_CODE,
                                "The verification code entered was invalid"));
                    });
        });
    }

    public void phoneNumberVerification(String phoneNumber, FragmentActivity fragmentActivity, PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(fragmentActivity)
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public Observable<FirebaseUser> createWithEmailPasswordAuthCredential(String email, String password) {
        return Observable.create(emitter -> {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        firebaseAuth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(command -> {
                                    FirebaseUser firebaseUser = command.getUser();
                                    emitter.onNext(firebaseUser);
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG + ":createWithEmailPasswordAuthCredential", e.getMessage());
                                    emitter.onError(e);
                                });

                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG + ":createWithEmailPasswordAuthCredential", e.getMessage());
                        emitter.onError(e);
                    });
        });
    }


    public Completable updateRegisterInfo(User user) {
        return Completable.create(emitter -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            user.setCreatedAt(new Date());
            user.setOnlineStatus(false);
            if(firebaseUser != null) {
                firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                        .document(CipherUtils.Hash.sha256(user.getPhoneNumber()))
                        .set(user)
                        .addOnSuccessListener(command -> emitter.onComplete())
                        .addOnFailureListener(e -> {
                            Log.d(TAG + ":updateRegisterInfo", new NullPointerException().getMessage());
                            emitter.onError(new NullPointerException());
                        });
            } else {
                Log.d(TAG + ":updateRegisterInfo", new NullPointerException().getMessage());
                emitter.onError(new NullPointerException());
            }
        });
    }

    public Completable login(String phoneNumber, String password) {
        return Completable.create(emitter -> {
            firebaseAuth.signInWithEmailAndPassword(String.format("%s@gmail.com", phoneNumber), password)
                    .addOnSuccessListener(command -> {
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Completable checkLoginUser() {
        return Completable.create(emitter -> {
            FirebaseUser user = getCurrentFirebaseUser();

            // Kiểm tra người dùng đăng nhập
            if ((user == null) || (user.getEmail() == null)) {
                Debug.log("checkLoginUser", "UNKNOWN_USER");
                emitter.onError(new UserException(UserException.ErrorType.UNKNOWN_USER,
                        "Vui lòng đăng nhập tài khoản"));
            } else {
                // Kiểm tra tài khoản người dùng còn hợp lệ
                firebaseAuth.fetchSignInMethodsForEmail(user.getEmail()).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if (task.getResult().getSignInMethods() == null) {
                            firebaseAuth.signOut();
                            Debug.log("checkLoginUser", "UNKNOWN_USER");
                            emitter.onError(new UserException(UserException.ErrorType.UNKNOWN_USER,
                                    "Không thể xác nhận tài khoản.\nVui lòng đăng nhập lại!"));
                        } else {
                            Debug.log("checkLoginUser", "SUCCESS");
                            emitter.onComplete();
                        }
                    } else {
                        if(task.getException() != null) {
                            emitter.onError(task.getException());
                        }
                    }

                });
            }
        });
    }

    public Completable logout() {
        return Completable.create(emitter -> {
            FirebaseUser user = getCurrentFirebaseUser();

            if ((user == null) || (user.getEmail() == null)) {
                emitter.onError(new UserException(UserException.ErrorType.UNKNOWN_USER, "Đã có lỗi xảy ra!"));
            } else {
                firebaseAuth.signOut();
                emitter.onComplete();
            }
        });
    }

    public Completable updateUserName(String userName, String numberPhone) {
        return Completable.create(emitter -> firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(CipherUtils.Hash.sha256(numberPhone))
                .update("userName", userName)
                .addOnSuccessListener(command -> emitter.onComplete())
                .addOnFailureListener(emitter::onError));
    }

    public Completable changePassword(String email, String passwordOld, String passwordNew) {
        return Completable.create(emitter -> {
            FirebaseUser user = getCurrentFirebaseUser();

            AuthCredential authCredential = EmailAuthProvider.getCredential(email, passwordOld);
            user.reauthenticate(authCredential)
                    .addOnSuccessListener(command -> user.updatePassword(passwordNew)
                            .addOnSuccessListener(command1 -> emitter.onComplete())
                            .addOnFailureListener(e -> emitter.onError(new UserException(UserException.ErrorType.UNKNOWN_USER, "Không thể cập nhật mật khẩu mới")))
                    ).addOnFailureListener(e -> emitter.onError(new UserException(UserException.ErrorType.UNKNOWN_USER, "Mật khẩu không đúng")));
        });
    }

    public Completable createAccessToken(String phoneNumber) {
        return Completable.create(emitter -> {
            try {
                Algorithm signature = Algorithm.HMAC256("YWU2OWNnSGVlQm9Sd3NoRUttSmJ6NVYwU0tjaTA0OWY=");
                HashMap<String, Object> header = new HashMap<String, Object>() {{
                    put("cty", "stringee-api;v=1");
                    put("typ", "JWT");
                    put("alg", "HS256");
                }};

                String keySID = "SK.0.smkjxTV6ntui2aX64mYBDPJ81lLJzypr";
                Stringee.token = JWT.create().withHeader(header)
                        .withClaim("jti", keySID + "-" + System.currentTimeMillis())
                        .withClaim("iss", keySID)
                        .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
                        .withClaim("userId", "." + phoneNumber).sign(signature);

                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(new UserException(UserException.ErrorType.UNKNOWN_TOKEN,
                        "Không thể kết nối đến máy chủ cuộc gọi"));
            }
        });
    }
}