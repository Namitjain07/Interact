# Video Calling Android Application

## Contributors
- **Namit Jain** (2022315)
- **Saurav Haldar** (2022464)
- **Vipul Mishra** (2022596)
- **Satyam** (2022462)

## Overview
This is a **group video calling Android application** developed in **Android Studio** using **VideoSDK**. The app supports **real-time multi-user video conferencing, screen sharing, recording, and transcription with summary generation**.

## Features
- **Create or Join Group Video Calls**: Users can create or join multi-user meetings.
- **Real-time Audio & Video Communication**: Seamless communication using **VideoSDK**.
- **In-Call Messaging**: Text chat within the meeting.
- **Microphone & Camera Control**: Mute/unmute and toggle video.
- **Audio Device Selection**: Choose between speaker, Bluetooth, wired headset, or earpiece.
- **Screen Sharing**: Share the device screen with all participants.
- **Call Recording**: Record the audio-video stream of meetings.
- **AI-Based Transcription**: Real-time speech-to-text with post-call **summarization**.
- **Summary Storage on AWS**: Meeting recordings and summaries are uploaded to an **AWS server** for later access.
- **Network Status Monitoring**: Visual feedback for network quality.

## Technologies Used
- **Kotlin** for Android development.
- **VideoSDK** for video/audio/screen sharing.
- **Fast Android Networking** for HTTP requests.
- **Firebase Crashlytics** *(optional for error logging)*.
- **AWS S3** for secure recording and transcript storage.
- **Material Design Components** for a modern UI.

## Project Structure
```
app/
 ├── activity/
 │   ├── CreateOrJoinActivity.kt
 │   ├── GroupCallActivity.kt          # This Replaces OneToOneCallActivity
 │
 ├── fragment/
 │   ├── CreateMeetingFragment.kt
 │   ├── CreateOrJoinFragment.kt
 │   ├── JoinMeetingFragment.kt
 │
 ├── adapter/
 │   ├── AudioDeviceListAdapter.kt
 │   ├── DeviceAdapter.kt
 │   ├── LeaveOptionList.kt
 │   ├── MessageAdapter.kt
 │
 ├── listener/
 │   ├── ResponseListener.kt
 │
 ├── modal/
 │   ├── ListItem.kt
 │
 ├── utils/
 │   ├── HelperClass.kt
 │   ├── NetworkUtils.kt
 │
 ├── MainApplication.kt
 ├── RobotoFont.kt
```

## Explanation of Each Component

### Activity Files
- **CreateOrJoinActivity.kt**
  - Handles user input to either create or join a meeting.
  - Manages permissions and device setup (mic/camera toggle, audio device).

- **GroupCallActivity.kt**
  - Core activity for group video calls.
  - Manages:
    - Multi-user meeting sessions.
    - Screen sharing start/stop.
    - Call recording initiation and termination.
    - Real-time transcription.
    - Upload of recordings and meeting summaries to AWS.
    - In-call messaging and participant list.
    - BottomSheetDialogs for UI actions.

### Fragment Files
- **CreateMeetingFragment.kt**
  - Authenticates and creates a meeting using VideoSDK API.
  - Passes the token and meeting ID to `GroupCallActivity`.

- **JoinMeetingFragment.kt**
  - Lets users input and validate an existing meeting ID.
  - Navigates to `GroupCallActivity` upon validation.

- **CreateOrJoinFragment.kt**
  - Entry point fragment with options to create or join a meeting.

### Adapter Files
- **AudioDeviceListAdapter.kt**, **DeviceAdapter.kt**
  - Populate UI lists of available audio output devices.

- **LeaveOptionList.kt**
  - UI for leaving or ending the meeting session.

- **MessageAdapter.kt**
  - Adapter for in-call chat messages.

### Listener Files
- **ResponseListener.kt**
  - Interface for async API responses like token retrieval.

### Model Files
- **ListItem.kt**
  - Generic model for list views.

### Utility Files
- **HelperClass.kt**
  - Helper methods for UI actions and meeting checks.

- **NetworkUtils.kt**
  - API interaction functions:
    - `getToken()`, `createMeeting()`, `joinMeeting()`
    - `uploadToAWS()`: Uploads recordings and summaries.
    - `fetchMeetingTime()`.

### Main Application Files
- **MainApplication.kt**
  - Initializes required SDKs and services (VideoSDK, Networking).

- **RobotoFont.kt**
  - Configures custom Roboto font across the app.

## Installation & Setup

### Prerequisites
- Android Studio installed.
- Android SDK level ≥ 24 (Nougat).
- Valid **VideoSDK Token**.
- AWS S3 bucket access configured for uploads.

### Steps
#### Clone the Repository
```sh
git clone https://github.com/Namitjain07/Interact
cd Interact
```

#### Open in Android Studio
1. Launch Android Studio.
2. Select **Open an existing project** and choose the repo folder.

#### Configure API Keys
1. Register on [VideoSDK](https://app.videosdk.live/) and generate an auth token.
2. Configure **`local.properties`**:
   ```properties
   auth_token=<YOUR_VIDEOSDK_TOKEN>
   ```

#### Build & Run
1. Use a physical device/emulator.
2. Click **Run (▶)** in Android Studio.

## Usage
1. **Open the App**
2. **Create or Join a Meeting**
   - If creating, a meeting ID will be generated.
   - If joining, enter the shared meeting ID.
3. **Allow Permissions**
   - Camera, microphone, and screen sharing access.
4. **During the Call**
   - Enable/disable mic or camera.
   - Send and view messages.
   - Share your screen.
   - Start/stop recording.
   - View transcription live.
5. **After Call Ends**
   - The **recording and transcript summary** are uploaded to AWS.
   - Access logs from server dashboard or app settings (if implemented).

---

