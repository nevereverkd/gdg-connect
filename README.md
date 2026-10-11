# GDG Connect — native Android

Kotlin + Jetpack Compose Material 3. Android 8.0 (API 26) or newer.
Firebase Authentication + Cloud Firestore Standard provide the backend.

## Connected Firebase project

Project: `gdg-connect-sv-20261011` ([Firebase Console](https://console.firebase.google.com/project/gdg-connect-sv-20261011/overview)).
Android package: `com.gdg.connect`. Firestore `(default)`: **STANDARD**, **asia-south1 (Mumbai)**.
Email/password authentication and Firestore Security Rules are deployed. `app/google-services.json` contains the Android client configuration, not an administrator credential.
Four clearly labelled sample events let you explore upcoming/past events and bookings immediately. Replace them with organizer-verified event details before promoting the app publicly.

## Implemented

- Real Firebase email/password registration, login, persistent sessions, password reset, email verification, sign-out.
- Live upcoming/past event feed; instant, case-insensitive search over visible event title, venue, technologies, and club.
- Event descriptions, dates in event time zone, venue, price, hosts, directions, sharing.
- Atomic seat reservations and cancellations. Server rules reject overbooking, past-event registration, duplicate booking, counter tampering, and access to another user's schedule.
- Personal schedule, calendar insertion, profile email status, persistent system/light/dark preference.
- Firestore offline cache for event browsing; online confirmation required for reservations.
- Loading, empty, error, and offline states; lazy event rendering and stable item keys.

Ticketed events show the organizer's price. There is no payment processing in the PRD or this app: reservations do not represent payment. Calendar and directions open compatible installed Android apps.

## Reproduce backend setup in another project

1. Authenticate using the Firebase tool's login flow. Never include account access tokens in the app or source.
2. Create the chosen new Firebase project, then a STANDARD `(default)` Firestore database in `asia-south1`.
3. Register the Android app package `com.gdg.connect`.
4. Retrieve its `google-services.json` using the Firebase MCP `firebase_get_sdk_config` tool or `npx -y firebase-tools@latest apps:sdkconfig ANDROID <app-id> --project <project-id>` and save it to `app/google-services.json`. The Gradle plugin is applied when the file exists.
5. Deploy `firebase.json` authentication configuration and `firestore.rules` using `npx -y firebase-tools@latest deploy --only auth,firestore --project <project-id>`.
6. Publish event documents through a trusted Admin SDK environment or Firebase Console. Clients cannot create events or promote themselves to administrators.
7. Rebuild the APK and verify against the actual Firebase project.

## Build

Open this folder in Android Studio, using JDK 21 (the bundled JBR works) and Android SDK 35. Gradle compiles bytecode for Java 17. Dependencies are pinned; network access is needed on the first build.

```
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleRelease
```

The release build enables R8 and resource shrinking. This development project signs sideload builds with the local Android debug certificate. For store distribution and a durable update identity, configure a dedicated release keystore; keep its password and private key out of source control.

## Local backend verification

```
cd backend
npm ci
cd ..
npx -y firebase-tools@latest emulators:exec --only auth,firestore --project demo-gdg-connect 'npm --prefix backend test'
```

The tests check authorization, schema restrictions, duplicate and past bookings, atomic cancellation, authentication operations, and concurrent attempts at the last seat.

For Android emulator testing:

```
npx -y firebase-tools@latest emulators:start --only auth,firestore --project demo-gdg-connect
FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 GCLOUD_PROJECT=demo-gdg-connect node backend/seed.mjs
./gradlew -PfirebaseEmulators=true :app:assembleDebug
```

The emulator-only debug APK connects to `10.0.2.2`. This switch is guarded by `BuildConfig.DEBUG`; production builds never connect to the emulator. Seeded events are marked as samples and never presented as real scheduled meetups. Seeding is create-only, so rerunning preserves reservations.

## Event publishing contract

`events/{eventId}` uses: `title`, `description`, `venue`, `price`, `organizingClub`, `zone` (IANA time zone), `hosts` and `tags` string arrays; `startsAt`/`endsAt` Firestore timestamps; `capacity` integer 1..100000; `reserved` integer initially 0; `published` boolean; `sample` boolean.

Use short IDs up to 128 characters. Keep text concise (title <=150, description <=5000, venue <=250, hosts/tags <=10 items). Validate timestamps and zone names before publishing. Keep past and booked events published to preserve schedule history. Do not manually reset reservation counts or delete booked events.

The feed streams published event documents and searches them locally to meet the PRD's instant filtering requirement. This is suitable for a community hub; a very large multi-city catalog should add paging and a dedicated indexed search service before broad rollout.

## Security boundaries

Email stays in Firebase Auth. Users can read only their own reservation documents. Event writes are limited to exactly one seat count change paired atomically with that user's reservation. Rules are a tested prototype, not an independent security audit. Appearance is device-local. Account data is excluded from Android cloud/device backup.

No Cloud Functions billing plan is necessary for the current feature scope: Firestore transactions and deployed Security Rules enforce backend booking logic.

## Android device tests

`FlowTest` exercises signup, search, upcoming/past tabs, booking, activity recreation, cancellation, profile, theme, sign-out/sign-in, and offline behavior against local Firebase emulators. Select this class explicitly when running the emulator build.

`LiveFlowTest` uses instrumentation arguments `testEmail` and `testPassword` for a temporary Firebase user, and checks live sign-in, event loading, reservation and cancellation. Build `:app:assembleDebugAndroidTest` and select `com.gdg.connect.LiveFlowTest`. The optimized release is checked separately through Android UI interactions because minification removes APIs needed by the Compose test runner. Delete the temporary user after testing. Credentials must never be placed in the source or the APK.

Dependency references: [Firebase Android release notes](https://firebase.google.com/support/release-notes/android), [Kotlin/R8 compatibility](https://developer.android.com/build/kotlin-support). R8 is explicitly aligned with Kotlin 2.4.
# GDG-CONNECT
~By Bhaveshh & Sachin 
