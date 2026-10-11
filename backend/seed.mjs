import {initializeApp,applicationDefault,getApps} from 'firebase-admin/app';
import {getFirestore,Timestamp} from 'firebase-admin/firestore';
const projectId=process.env.GCLOUD_PROJECT || 'demo-gdg-connect';
if (!process.env.FIRESTORE_EMULATOR_HOST && projectId.startsWith('demo-')) throw Error('Set GCLOUD_PROJECT to your actual Firebase project and provide Application Default Credentials.');
if (!getApps().length) initializeApp({projectId,...(!process.env.FIRESTORE_EMULATOR_HOST?{credential:applicationDefault()}: {})});
const db=getFirestore();
const midnight=new Date();midnight.setUTCHours(4,30,0,0);
const records=[
 ['android-lab','Build beautiful Android apps',7,'Android','Kotlin','GDG Mumbai','Mumbai · Community Tech Hub','Free','Explore Jetpack Compose with a hands-on app workshop. Build responsive layouts, manage state, and meet fellow Android developers. Bring a laptop with Android Studio installed.'],
 ['ai-lab','From idea to AI prototype',14,'AI','Gemini','GDG Pune','Pune · Developer Community Space','Free','Turn an idea into an AI-powered prototype. Explore responsible model integration, structured outputs, and practical product design. Basic programming experience is helpful.'],
 ['cloud-day','Cloud builders: ship your first app',21,'Cloud','Firebase','GDG Bengaluru','Bengaluru · Community Tech Hub','Ticketed · ₹299 at venue','A guided session on authentication, databases, and deploying your first cloud application. Bring a laptop and a project idea. This test event does not collect real payments.'],
 ['web-workshop','Web foundations, real connections',-7,'Web','JavaScript','GDG Mumbai','Mumbai · Community Tech Hub','Free','An introductory web workshop exploring accessible interfaces and modern JavaScript. A sample archived meetup for testing the past-events view.']
];
for(const [id,title,days,tag,stack,club,venue,price,description] of records){
 const startsAt=new Date(midnight.getTime()+days*86400000);
 const data={title,description,startsAt:Timestamp.fromDate(startsAt),endsAt:Timestamp.fromMillis(startsAt.getTime()+10800000),venue,price,hosts:['Community facilitator','Workshop mentor'],organizingClub:club,tags:[tag,stack],capacity:50,reserved:0,published:true,sample:true,zone:'Asia/Kolkata'};
 if(title.length>150||description.length>5000||data.endsAt.toMillis()<=data.startsAt.toMillis())throw Error('Invalid sample event');
 const ref=db.doc(`events/sample-${id}`);
 // Create-only: rerunning never erases bookings or resets existing capacity counters.
 try{await ref.create(data);console.log(`Created sample-${id}`);}catch(e){if(e.code===6)console.log(`Kept existing sample-${id}`);else throw e;}
}
