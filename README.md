# Video Calling Android Application

## Contributors
- **Namit Jain** (2022315)
- **Saurav Haldar** (2022464)
- **Vipul Mishra** (2022596)
- **Satyam** (2022462)

## Overview
This is a video calling Android application developed in Android Studio using **VideoSDK** for real-time video communication. The app supports **one-to-one video calls, audio device selection, and in-call messaging**.

## Features
- **Create or Join Video Calls**: Users can create a new meeting or join an existing one.
- **Real-time Audio & Video Communication**: Facilitated using **VideoSDK**.
- **In-Call Messaging**: Users can send messages within the call.
- **Microphone & Camera Control**: Users can mute/unmute the microphone and enable/disable the camera.
- **Audio Device Selection**: Users can switch between speaker, Bluetooth, wired headset, or earpiece.
- **Network Status Monitoring**: Displays network quality indicators.

## Technologies Used
- **Kotlin** for Android development.
- **VideoSDK** for real-time video and audio communication.
- **Fast Android Networking** for API calls.
- **Firebase Crashlytics** (optional for debugging and error tracking).
- **Material Design Components** for UI elements.

## Project Structure
```
app/
 ├── activity/            # Activity files
 │   ├── CreateOrJoinActivity.kt
 │   ├── OneToOneCallActivity.kt
 │
 ├── fragment/            # Fragment files
 │   ├── CreateMeetingFragment.kt
 │   ├── CreateOrJoinFragment.kt
 │   ├── JoinMeetingFragment.kt
 │
 ├── adapter/             # RecyclerView & ListView Adapters
 │   ├── AudioDeviceListAdapter.kt
 │   ├── DeviceAdapter.kt
 │   ├── LeaveOptionList.kt
 │   ├── MessageAdapter.kt
 │
 ├── listener/            # Listener interfaces
 │   ├── ResponseListener.kt
 │
 ├── modal/               # Model classes
 │   ├── ListItem.kt
 │
 ├── utils/               # Utility classes
 │   ├── HelperClass.kt
 │   ├── NetworkUtils.kt
 │
 ├── MainApplication.kt   # Application initialization
 ├── RobotoFont.kt        # Custom font management
```

## Explanation of Each Component
### Activity Files
- **CreateOrJoinActivity.kt**
  - Manages the UI where users choose to create or join a meeting.
  - Handles permissions for camera, microphone, and internet.
  - Provides toggle buttons for enabling/disabling mic and camera before joining.
  - Uses RecyclerView to display available audio devices.

- **OneToOneCallActivity.kt**
  - Handles the video call functionality.
  - Manages VideoSDK's Meeting API.
  - Implements camera switching, audio selection,and chat.
  - Uses Snackbar for notifications and BottomSheetDialog for options.

### Fragment Files
- **CreateMeetingFragment.kt**
  - Allows users to create a new meeting.
  - Fetches an authentication token and generates a meeting ID.
  - Starts a **OneToOneCallActivity** with the generated token and meeting ID.

- **JoinMeetingFragment.kt**
  - Allows users to join an existing meeting.
  - Validates meeting ID format.
  - Starts the **OneToOneCallActivity** with the provided meeting ID and token.

- **CreateOrJoinFragment.kt**
  - Provides UI buttons for users to navigate between **CreateMeetingFragment** and **JoinMeetingFragment**.

### Adapter Files
- **AudioDeviceListAdapter.kt**
  - Displays a list of available audio devices.
  - Highlights the selected audio device.

- **DeviceAdapter.kt**
  - Adapter for displaying available audio devices in a RecyclerView.
  - Listens for user selection events.

- **LeaveOptionList.kt**
  - Displays the **Leave Meeting** and **End Meeting** options.

- **MessageAdapter.kt**
  - Handles the display of chat messages in the meeting.
  - Uses RecyclerView to manage message items dynamically.

### Listener Files
- **ResponseListener.kt**
  - Generic interface to handle API responses asynchronously.

### Model Files
- **ListItem.kt**
  - Represents a menu item with properties such as name, icon, and description.

### Utility Files
- **HelperClass.kt**
  - Provides utility functions such as:
    - Displaying snackbars for network errors.
    - Showing/hiding progress dialogs.
    - Checking participant size before allowing entry.

- **NetworkUtils.kt**
  - Manages API requests to authenticate users and manage meetings.
  - Functions:
    - `isNetworkAvailable()`: Checks internet connection.
    - `getToken()`: Fetches authentication token from the API.
    - `createMeeting()`: Creates a new meeting via API.
    - `joinMeeting()`: Validates and joins an existing meeting.
    - `fetchMeetingTime()`: Retrieves the meeting duration.

### Main Application Files
- **MainApplication.kt**
  - Initializes **VideoSDK** and **AndroidNetworking** at application startup.

- **RobotoFont.kt**
  - Loads custom **Roboto** font for UI elements.

## Installation & Setup
### Prerequisites
- Android Studio installed.
- **Minimum SDK**: 24 (Nougat).
- API Key for **VideoSDK**.

### Steps
#### Clone the Repository
```sh
git clone https://github.com/Namitjain07/Interact
cd Interact
```
#### Open the Project in Android Studio
1. Open **Android Studio** and select **Open an existing project**.

#### Add API Key
1. Make a free account on [VideoSDK](https://app.videosdk.live/) and generate a token.
2. Inside the **local.properties** file in the project, add the generated token:
   ```sh
   auth_token=<generated_token>
   ```

#### Build & Run
1. Connect a physical device or use an emulator.
2. Click **Run (▶)** in Android Studio.

## Usage
1. **Launch the App**
2. **Choose to Create or Join a Meeting**
   - If **creating**, you will be assigned a **Meeting ID**.
   - If **joining**, enter an existing **Meeting ID**.
3. **Allow Permissions** (Camera & Microphone)
4. **Start Video Call**
   - Enable/Disable camera and microphone.
   - Send messages during the call.
5. **Leave or End the Meeting** when done.

## Future Work
We are actively working on enhancing this application with more features:
- **Call Recording**: Ability to record video and audio calls for future reference.
- **Enhanced Screen Sharing**: Improved UI and seamless performance for screen sharing.
- **Group Calling**: Support for multi-participant video conferencing.
- **AI/ML-based Transcription**: Real-time transcription of conversations using AI-powered speech-to-text.
- **Emotes and Reactions**: Users can send live reactions (e.g., thumbs up, applause) during a call.

These features will further enhance the user experience and make the application more versatile.

---
### 📌 Feel free to contribute or report issues by creating a pull request or raising an issue on GitHub!

