# Todone
An Android task management app built with Kotlin and Firebase, featuring tag-based organization, advanced filtering, and a clean, modern interface.


![Kotlin](https://img.shields.io/badge/Kotlin-black?logo=kotlin&logoColor=7F52FF)
![Firebase](https://img.shields.io/badge/Firebase-black?logo=firebase&logoColor=red)
![Cloud Firestore](https://img.shields.io/badge/Cloud%20Firestore-black?logo=firebase&logoColor=yellow)
![Android](https://img.shields.io/badge/Android-black?logo=android&logoColor=3DDC84)

## Features
- Firebase Authentication (email/password)
- Real-time task sync with Cloud Firestore
- Tag-based organization
- Multi-criteria filtering (tags, completion status, due date)

## Setup & Installation
### Prerequisites
- Android Studio (latest version)
- Android SDK 26+ (target SDK 34)
- Firebase account

### Installation
1. Clone this repository
2. Firebase setup
   - [create a new firebase project](https://console.firebase.google.com/)
   - add ```google-services.json``` to ```app/```
   - enable email/password authentication
   - create firebase database collection "tasks" with the following structure:
   ``` 
   {
     id: string,
     title: string,
     description: string,
     date: string,        // "YYYY-MM-DD"
     tag: string,
     done: boolean,
     userId: string
     }
   ```
3. Build & run 




![Home page](images/home.png)