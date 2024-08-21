# Camera Gallery Scanner

## Description
 - App starts the scan of Camera images upon receiving permission.
 - In Batches, images are passed to MediaPipe Face Detection, with a limited parallelism.
 - Image's with detected faces will be persisted (Only URI's and FaceInfo).
 - FaceInfo contains the face's bounding box, and Tags, which can be edited.

## Tech Stack
- Kotlin
- Jetpack Compose
- Hilt (DI)
- Coroutines
- Room (Persistence)
- MediaPipe Face Detection

## Architecture
- MVVM
- (UI <> ViewModel <> Repository <> Data Source)

## More
- To Test out with desirable performance, run in Release mode.
