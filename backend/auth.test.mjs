import {test} from 'node:test';
import assert from 'node:assert/strict';
import {initializeApp,deleteApp} from 'firebase/app';
import {getAuth,connectAuthEmulator,createUserWithEmailAndPassword,signInWithEmailAndPassword,signOut,sendPasswordResetEmail,sendEmailVerification} from 'firebase/auth';
test('Firebase email signup, signout, login, reset and verification',async()=>{
const app=initializeApp({projectId:'demo-gdg-connect',apiKey:'demo-key'},'auth-test');
const auth=getAuth(app);connectAuthEmulator(auth,'http://127.0.0.1:9099',{disableWarnings:true});
try{const email=`test-${Date.now()}@example.com`;const result=await createUserWithEmailAndPassword(auth,email,'Test-pass-8923');const uid=result.user.uid;await sendEmailVerification(result.user);await signOut(auth);assert.equal(auth.currentUser,null);await assert.rejects(signInWithEmailAndPassword(auth,email,'wrong-pass'));await signInWithEmailAndPassword(auth,email,'Test-pass-8923');assert.equal(auth.currentUser.uid,uid);await sendPasswordResetEmail(auth,email);}finally{await deleteApp(app);}
});
