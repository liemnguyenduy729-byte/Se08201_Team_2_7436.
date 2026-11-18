const {onCall, HttpsError} = require('firebase-functions/v2/https');
const {defineSecret} = require('firebase-functions/params');
const {initializeApp} = require('firebase-admin/app');
const {getFirestore, FieldValue} = require('firebase-admin/firestore');
initializeApp();
const key = defineSecret('AI_API_KEY');
exports.askMentor = onCall({secrets:[key],timeoutSeconds:90,maxInstances:5}, async request => {
  if(!request.auth) throw new HttpsError('unauthenticated','Please sign in.');
  const question = request.data?.question;
  if(typeof question !== 'string' || !question.trim() || question.length > 4000) throw new HttpsError('invalid-argument','Question must contain 1–4000 characters.');
  const endpoint=process.env.AI_ENDPOINT; const model=process.env.AI_MODEL;
  if(!endpoint || !model || !endpoint.startsWith('https://')) throw new HttpsError('failed-precondition','AI provider is not configured.');
  const db=getFirestore(); const rate=db.collection('_rateLimits').doc(request.auth.uid);
  await db.runTransaction(async tx => {
    const snap=await tx.get(rate); const last=snap.data()?.lastRequest || 0;
    if(Date.now()-last<10000) throw new HttpsError('resource-exhausted','Please wait 10 seconds.');
    tx.set(rate,{lastRequest:Date.now()});
  });
  let answer;
  try {
    const response=await fetch(endpoint,{method:'POST',headers:{Authorization:'Bearer '+key.value(),'Content-Type':'application/json'},body:JSON.stringify({model,messages:[{role:'system',content:'You are AI Study Mentor. Explain academic questions step by step in the student language. Encourage understanding, acknowledge uncertainty, and do not claim sources you have not checked.'},{role:'user',content:question.trim()}],max_tokens:1600}),signal:AbortSignal.timeout(60000)});
    if(!response.ok) throw new Error('Provider unavailable');
    const data=await response.json(); answer=data.choices?.[0]?.message?.content;
    if(typeof answer!=='string' || !answer.trim() || answer.length>50000) throw new Error('Invalid provider response');
  } catch { throw new HttpsError('unavailable','AI service unavailable. Please retry.'); }
  try { await db.collection('learningHistory').add({userId:request.auth.uid,question:question.trim(),answer,createdAt:FieldValue.serverTimestamp()}); }
  catch { throw new HttpsError('internal','Could not save learning history. Please retry.'); }
  return {answer};
});
