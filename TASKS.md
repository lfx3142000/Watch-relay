# ChatGPT Watch Relay — TASKS.md

## Project status

**Project:** ChatGPT Watch Relay  
**Repository:** `lfx3142000/Watch-relay`  
**Goal:** Build an Android phone app that automatically detects when ChatGPT finishes responding, sends the readable response to a Samsung Galaxy Watch, and lets the user send preset or voice/custom replies from the watch back into the same ChatGPT conversation.

**Current status:** Initial Android shell builds in CI. Notification relay, watch actions, inline reply receiver, manual share fallback, basic Accessibility monitoring, and a first-pass Accessibility command sender are in place. The sender queues preset/custom watch replies, opens ChatGPT, attempts to focus the message box, paste/set command text, and tap a detected Send control. It now includes retry/timeout handling and posts a failure notification if the command cannot be sent. This still needs real-device testing and more robust ChatGPT UI detection.

**Critical MVP decision:** The MVP must include an automatic relay loop. Manual share/copy is allowed only as a fallback/debug path, not as the primary MVP.

---

## MVP definition

The MVP is successful only when this end-to-end loop works on an Android phone paired with a Galaxy Watch:

```text
1. User starts or continues a ChatGPT conversation on the phone.
2. ChatGPT generates a response.
3. Relay app automatically detects that generation has completed.
4. Relay app automatically extracts the latest ChatGPT response text.
5. Relay app sends a watch-readable notification with the first response chunk.
6. Galaxy Watch displays the response notification.
7. User can tap More to read additional chunks.
8. User can tap preset actions such as Continue, Summarize, Shorter, or Open.
9. User can send a custom reply through notification inline reply when supported.
10. Relay app sends the selected preset command or custom reply back into the active ChatGPT conversation.
11. Relay app returns to monitoring for the next response.
```

Manual import/share/copy features are useful fallback features, but they do not satisfy the MVP by themselves.

---

## Important constraints and realities

- No OpenAI API use in the MVP.
- No backend server.
- No API keys.
- The app must work through the existing ChatGPT app or ChatGPT web UI.
- Automatic response detection and reply sending require Android Accessibility permission.
- Watch integration is through Android notifications mirrored to Galaxy Watch.
- Phone should be assumed unlocked for MVP automation reliability.
- Full lock-screen operation is a future enhancement, not MVP.
- UI automation may break if ChatGPT changes its app/web layout.
- The app must always include a fallback mode when automation fails.

---

# Phase 0 — Repository setup

## Status

Mostly complete. CI build passed after JVM target fix.

## Tasks

- [x] Create GitHub repository.
- [x] Add initial `TASKS.md`.
- [x] Revise `TASKS.md` so MVP requires automatic ChatGPT detection, watch notification, and watch reply functionality.
- [x] Add initial `README.md`.
- [x] Add `.gitignore` for Android/Kotlin project.
- [x] Create initial Android project shell.
- [x] Commit initial project shell.
- [ ] Confirm project opens in Android Studio.
- [ ] Confirm app builds locally.
- [x] Confirm app builds in GitHub Actions CI. Run `26391318581` passed.
- [x] Document normal Android build workflow in README.

## Acceptance criteria

- [x] Repository exists.
- [x] `TASKS.md` is committed.
- [x] Android app skeleton files exist.
- [x] Android app skeleton builds in CI.

---

# Phase 1 — Android app shell and required permissions

## Status

In progress.

## Completed files

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/chatgpt_accessibility_service.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/java/com/example/chatgptwatchrelay/MainActivity.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/accessibility/ChatGptAccessibilityService.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/accessibility/ChatGptCommandSender.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/notifications/NotificationHelper.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/notifications/CommandReceiver.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/notifications/ReplyReceiver.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/Chunker.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/CommandRepository.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/RelayState.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/launch/ChatGptLauncher.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/fallback/ShareReceiverActivity.kt`

## Tasks

- [x] Create Kotlin Android project structure.
- [ ] Configure Jetpack Compose. Current shell uses native Android views to reduce setup complexity.
- [x] Set minSdk to 26 or higher.
- [x] Add app name: `ChatGPT Watch Relay`.
- [x] Add notification permission handling for Android 13+.
- [x] Add Accessibility Service declaration in manifest.
- [x] Add Accessibility Service configuration XML.
- [x] Add basic onboarding controls explaining/accessing Accessibility settings.
- [x] Add button to open Android Accessibility settings.
- [ ] Add status indicator: Accessibility enabled/disabled.
- [ ] Add status indicator: notification permission enabled/disabled.
- [x] Add button: `Send Test Watch Notification`.
- [x] Add button: `Open ChatGPT`.
- [x] Add button: `Start Monitoring`.
- [x] Add button: `Stop Monitoring`.
- [ ] Add persistent local storage for app settings. Current `RelayState` is in-memory only.

---

# Phase 2 — Accessibility monitoring foundation

## Status

Started.

## Tasks

- [x] Create `ChatGptAccessibilityService.kt`.
- [x] Detect foreground app/package at a basic level.
- [x] Identify when ChatGPT app is active using package name contains `openai`.
- [x] Identify when Chrome/browser target is active using package name contains `chrome` as a basic fallback.
- [x] Capture accessibility node tree text while ChatGPT/browser is visible.
- [x] Add relay monitoring state: active/inactive.
- [x] Add pending-command handling before response monitoring.
- [ ] Add debug screen/log showing detected package, screen text count, and candidate input/send nodes.
- [ ] Add safe throttling so the service does not over-scan. Current approach uses event-driven updates and simple stability count only.
- [ ] Add error state if ChatGPT screen is not detectable.
- [ ] Add privacy warning that captured screen text is processed locally.

---

# Phase 3 — ChatGPT generation state detection

## Status

Started, but still crude.

## Tasks

- [ ] Create dedicated `ChatGptUiStateDetector.kt`.
- [ ] Define formal states: `UNKNOWN`, `IDLE`, `GENERATING`, `COMPLETE_CANDIDATE`, `COMPLETE_CONFIRMED`, `ERROR`.
- [x] Track changes in visible text over time in the Accessibility Service.
- [ ] Detect likely generating state explicitly.
- [x] Detect likely completed state after response text is stable for several accessibility events.
- [ ] Make stability delay configurable.
- [x] Avoid sending duplicate notifications for the same response using a response fingerprint.
- [x] Store a fingerprint/hash of the last notified response.
- [ ] Add debug display of current detected state.

---

# Phase 4 — Latest response extraction

## Status

Not complete. Current implementation captures all visible accessible text as a placeholder.

## Tasks

- [ ] Create `ChatGptScreenReader.kt`.
- [ ] Identify likely assistant response text from accessibility node tree.
- [ ] Prefer the most recent assistant message.
- [ ] Exclude user prompt text where possible.
- [ ] Exclude input box text.
- [ ] Exclude navigation/sidebar text.
- [ ] Normalize whitespace.
- [x] Store latest full response locally in memory.
- [x] Store response fingerprint.
- [ ] Add debug view of extracted response preview.
- [ ] Add fallback extraction strategy for Chrome web UI.
- [x] Add fallback manual share path for failures.

---

# Phase 5 — Automatic watch notification with chunks

## Status

Started.

## Tasks

- [x] Create `Chunker.kt`.
- [x] Default chunk size: 700–900 characters.
- [x] Preserve word boundaries when possible.
- [x] Store `lastFullResponse`.
- [x] Store `currentChunkIndex`.
- [x] Store `totalChunks` via chunk list size.
- [ ] Store timestamp.
- [x] Create notification channel: `ChatGPT Relay`.
- [x] Create notification title format: `ChatGPT done — 1/4`.
- [x] Create notification body with current chunk.
- [x] Add notification action: `More`.
- [x] Add notification action: `Continue`.
- [x] Add notification action: `Summarize`.
- [x] Add notification action: `Shorter`.
- [x] Add notification action: `Open`.
- [x] Add notification action: `Reply` using RemoteInput.
- [x] Add final chunk state: `End of response`.
- [x] Add fallback for short responses.

---

# Phase 6 — Preset command actions from watch

## Status

Implemented first-pass auto-send path; needs real-device testing and better diagnostics.

## Tasks

- [x] Create `CommandRepository.kt`.
- [x] Define default preset commands.
- [x] Create `CommandReceiver.kt`.
- [x] Receive notification action taps.
- [x] Map action taps to command text.
- [x] Copy command text to clipboard as fallback.
- [x] Open or return to ChatGPT.
- [x] Create `ChatGptCommandSender.kt`.
- [x] Queue preset command for Accessibility send.
- [x] Use Accessibility to find and focus a likely ChatGPT message input.
- [x] Paste command into the message input or use `ACTION_SET_TEXT` fallback.
- [x] Find and tap a likely Send control.
- [x] Restart response monitoring after send.
- [x] Add retry and timeout handling for pending command sends.
- [x] Add failure notification if command could not be sent.
- [ ] Add diagnostics for detected input/send candidates.
- [ ] Real-device test: Continue from watch sends into ChatGPT.
- [ ] Real-device test: Summarize from watch sends into ChatGPT.

---

# Phase 7 — Custom voice/text reply from watch

## Status

Implemented first-pass auto-send path; needs real-device testing.

## Tasks

- [x] Create `ReplyReceiver.kt`.
- [x] Add notification inline reply action.
- [x] Capture RemoteInput reply text.
- [x] Queue custom reply for Accessibility send.
- [x] Copy reply to clipboard as fallback.
- [x] Open or return to ChatGPT.
- [x] Use Accessibility to find and focus message input.
- [x] Paste reply text or use `ACTION_SET_TEXT` fallback.
- [x] Tap likely Send control.
- [x] Restart monitoring after send.
- [x] Add basic user-visible error if inline reply is unavailable.
- [x] Add retry/timeout fallback through shared command sender.
- [ ] Real-device test: watch shows a reply option.
- [ ] Real-device test: dictated reply is received and sent into ChatGPT.

---

# Current next step

Run normal Android Build on `main` after retry/timeout handling. If compile passes, add diagnostics for Accessibility detection: detected package name, visible text node count, likely input candidate count, and likely send button candidate count.
